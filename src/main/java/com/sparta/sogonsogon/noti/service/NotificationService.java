package com.sparta.sogonsogon.noti.service;

import com.amazonaws.services.kms.model.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.sogonsogon.member.entity.Member;
import com.sparta.sogonsogon.member.repository.MemberRepository;
import com.sparta.sogonsogon.noti.dto.NotificationResponseDto;
import com.sparta.sogonsogon.noti.entity.Notification;
import com.sparta.sogonsogon.noti.repository.EmitterRepository;
import com.sparta.sogonsogon.noti.repository.NotificationRepository;
import com.sparta.sogonsogon.noti.util.AlarmType;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Setter
public class NotificationService {

    private final EmitterRepository emitterRepository ;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    private final MemberRepository memberRepository;
    //DEFAULT_TIMEOUT을 기본값으로 설정
    private static final Long DEFAULT_TIMEOUT = 60 * 60 * 10000L;


    // Hikari Pool Dead Lock 해결책: connection pool 추가
    // NotificationService 클래스에서 커넥션 풀을 사용하여 데이터베이스 연결을 가져온다
    // DataSource 빈을 NotificationService에서 주입받아 사용하면 커넥션 풀이 적용된 코드가 됩니다.
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;


    @PostConstruct
    public void init() {
        jdbcTemplate.setDataSource(dataSource);
    }

    @Transactional
    public SseEmitter subscribe(UserDetailsImpl userDetails) {
        //메서드는 makeTimeIncludeId 메서드를 사용하여 emitterId를 생성하고, 이전에 생성된 emitterId를 모두 삭제합니다.
        String emitterId = makeTimeIncludeId(userDetails.getUser().getId());
        log.info("현재 시간 값을 포함한 유니크한 ID를 생성");
        emitterRepository.deleteAllEmitterStartWithId(String.valueOf(userDetails.getUser().getId()));
        log.info("이전에 생성된 모든 emitter 객체를 삭제하고 ");

        // 다음 SseEmitter 객체를 생성하고, emitterRepository를 사용하여 데이터베이스에 저장
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));
        log.info("새로운 emitter 객체를 생성 및 저장");

        emitter.onCompletion(() -> { // 클라이언트와의 연결이 종료되었을 때 호출
            log.info("SSE 연결 Complete");
            emitterRepository.deleteById(emitterId);
//            onClientDisconnect(emitter, "Compeletion");
        });

        emitter.onTimeout(() -> { // SseEmitter의 유효시간이 지났을 때 호출
            log.info("SSE 연결 Timeout");
            emitterRepository.deleteById(emitterId);
//            onClientDisconnect(emitter, "Timeout");
        });

        // SseEmitter에서 오류가 발생했을 때 호출
        emitter.onError((e) -> emitterRepository.deleteById(emitterId));

        //Dummy 데이터를 보내 503에러 방지. (SseEmitter 유효시간 동안 어느 데이터도 전송되지 않으면 503에러 발생)
        String eventId = makeTimeIncludeId(userDetails.getUser().getId());
        sendNotification(emitter, eventId, emitterId, "EventStream Created. [userId=" + userDetails.getUser().getId() + "]");

//        // 클라이언트가 미수신한 Event 목록이 존재할 경우 전송하여 Event 유실을 예방한다.
//        if (hasLostData(lastEventId)) {
//            sendLostData(lastEventId, userDetails.getUser().getId(), emitterId, emitter);
//        }

        return emitter;
    }

    private String makeTimeIncludeId(Long memberId) {
        return memberId + "_" + System.currentTimeMillis();
    }

    @Transactional
    public void send(Member receiver, AlarmType alarmType, String message, String senderMembername, String senderNickname, String senderProfileImageUrl) {
        //send() 메서드는 Member 객체와 AlarmType 열거형, 알림 메시지(String)와 알림 상태(Boolean) 값을 인자로 받아 기능을 구현한다.
        Notification notification = notificationRepository.save(createNotification(receiver, alarmType, message,senderMembername,senderNickname,senderProfileImageUrl));

        // Notification 객체의 수신자 ID를 추출하고,
        String receiverId = String.valueOf(receiver.getId());
        // 현재 시간을 포함한 고유한 eventId를 생성한다.
        String eventId = receiverId + "_" + System.currentTimeMillis();

        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByMemberId(receiverId);
        emitters.forEach(
                (key, emitter) -> {
                    emitterRepository.saveEventCache(key, notification);
                    sendNotification(emitter, eventId, key, NotificationResponseDto.create(notification));
                }
        );
    }

    public void sendNotification(SseEmitter emitter, String eventId, String emitterId, Object data) {
        try {
            log.info("eventId : " + eventId);
            log.info("data" + data);
            emitter.send(SseEmitter.event()
                    .id(eventId)
                    .data(data));
        } catch (IOException exception) {
            log.info("예외 발생해서 emitter 삭제됨");
            emitterRepository.deleteById(emitterId);
        }
    }


    private boolean hasLostData(String lastEventId) {
        return !lastEventId.isEmpty();
    }

    private void sendLostData(String lastEventId, Long memberId, String emitterId, SseEmitter emitter) {
        Map<String, Object> eventCaches = emitterRepository.findAllEventCacheStartWithByMemberId(String.valueOf(memberId));

        eventCaches.entrySet().stream()
                .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                .forEach(entry -> sendNotification(emitter, entry.getKey(), emitterId, entry.getValue()));
    }




    private Notification createNotification(Member receiver, AlarmType alarmType, String message, String senderMembername, String senderNickname, String senderProfileImageUrl) {
        Notification notification = new Notification();
        notification.setReceiver(receiver);
        notification.setAlarmType(alarmType);
        notification.setMessage(message);
        notification.setSenderMembername(senderMembername);
        notification.setSenderNickname(senderNickname);
        notification.setSenderProfileImageUrl(senderProfileImageUrl);
        return notificationRepository.save(notification);
    }


    //받은 알림 전체 조회
    public List<NotificationResponseDto> getAllNotifications(Long memberId) {

        List<Notification> notifications = notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(memberId);
        log.info("알림 전체 조회했어");
        return notifications.stream()
                .map(NotificationResponseDto::create)
                .collect(Collectors.toList());

    }


    // 특정 회원이 받은 알림을 확인했다는 것을 서비스에 알리는 기능
    @Transactional
    public NotificationResponseDto confirmNotification(Member member, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(
                () -> new NotFoundException("Notification not found"));

        // 확인한 유저가 알림을 받은 대상자가 아니라면 예외 발생
        if (!notification.getReceiver().getId().equals(member.getId())) {
            throw new IllegalArgumentException("접근권한이 없습니다. ");
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
        return new NotificationResponseDto(notification);
    }

    // 선택된 알림 삭제
    @Transactional
    public void deleteNotification(Long notificationId, Member member) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(
                () -> new NotFoundException("Notification not found"));

        // 확인한 유저가 알림을 받은 대상자가 아니라면 예외 발생
        if (!notification.getReceiver().getId().equals(member.getId())) {
            throw new IllegalArgumentException("접근권한이 없습니다. ");
        }
        notificationRepository.deleteById(notificationId);

    }



    // 클라이언트 타임아웃 처리
    private void onClientDisconnect(SseEmitter emitter, String type) {
        try {
            emitter.send(SseEmitter.event().name(type).data("Client" + type).id(String.valueOf(UUID.randomUUID())));
            emitter.complete();
        } catch (IOException e) {
            log.error("Failed to send" + type + "event to client", e);
        }
    }

    // 처리 된 건이고, 읽은 알림은 삭제 (1일 경과한 데이터)
    @Transactional
    public void deleteOldNotification() {
        List<Notification> notifications = notificationRepository.findOldNotification();

        log.info("총 " + notifications.size() + " 건의 알림 삭제");
        for(Notification notification : notifications){
            notificationRepository.deleteById(notification.getId());
        }
    }
}
