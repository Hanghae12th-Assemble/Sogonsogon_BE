package com.sparta.sogonsogon.noti.service;

import com.amazonaws.services.kms.model.NotFoundException;

import com.sparta.sogonsogon.member.entity.Member;
import com.sparta.sogonsogon.noti.dto.NotificationResponseDto;
import com.sparta.sogonsogon.noti.entity.Notification;
import com.sparta.sogonsogon.noti.repository.EmitterRepository;
import com.sparta.sogonsogon.noti.repository.NotificationRepository;
import com.sparta.sogonsogon.noti.util.AlarmType;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Setter
public class NotificationService {
//    private final ObjectMapper objectMapper;

    private final EmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;
//    private final MemberRepository memberRepository;

    //DEFAULT_TIMEOUT을 기본값으로 설정
    private static final Long DEFAULT_TIMEOUT = 60 * 60 * 10000L;
    private final DataSource dataSource;

    @Transactional
    public SseEmitter subscribe(UserDetailsImpl userDetails) {
        String emitterId = makeTimeIncludeId(userDetails.getUser().getId());
        emitterRepository.deleteAllEmitterStartWithId(String.valueOf(userDetails.getUser().getId()));
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));
        log.info("SSE 연결 됬지롱");

        emitter.onCompletion(() -> {
            emitterRepository.deleteById(emitterId);
        });
        //시간이 만료된 경우 자동으로 레포지토리에서 삭제하고 클라이언트에서 재요청을 보낸다.
        emitter.onTimeout(() -> {
            // ping 메시지를 15분마다 전송
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    emitter.send(SseEmitter.event().name("ping").data(""));
                } catch (IOException e) {
                    log.error("Failed to send ping event", e);
                }
            }, 0, 15, TimeUnit.MINUTES);
            emitterRepository.deleteById(emitterId);
        });
        emitter.onError((e) -> emitterRepository.deleteById(emitterId));
//        //Dummy 데이터를 보내 503에러 방지. (SseEmitter 유효시간 동안 어느 데이터도 전송되지 않으면 503에러 발생)
//        String eventId = makeTimeIncludeId(userDetails.getUser().getId());
//        sendNotification(emitter, eventId, emitterId, "EventStream Created. [userId=" + userDetails.getUser().getId() + "]");

        return emitter;
    }

    private String makeTimeIncludeId(Long memberId) {
        return memberId + "_" + System.currentTimeMillis();
    }

    @Transactional
    public void send(Member receiver, AlarmType alarmType, String message, String senderMembername, String senderNickname, String senderProfileImageUrl) {
        Connection con = null;
        try {
            con = dataSource.getConnection();
            // 데이터소스를 통해 데이터베이스와의 연결을 설정하고 Connection 객체를 생성합니다.
            con.setAutoCommit(false);
            // 트랜잭션을 수동으로 관리하기 위해 자동 커밋 기능을 false로 설정합니다.

            Notification notification = notificationRepository.save(createNotification(receiver, alarmType, message, senderMembername, senderNickname, senderProfileImageUrl));
            String receiverId = String.valueOf(receiver.getId());
            String eventId = receiverId + "_" + System.currentTimeMillis();

            // Emitter는 유저가 로그인하면 바로 구독할 것이다. 그럼 Admin 의 Emitter도 있을듯 본인 것만 보내기 때문에 노상관
            Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByMemberId(receiverId);
            emitters.forEach(
                    (key, emitter) -> {
                        emitterRepository.saveEventCache(key, notification);
                        sendNotification(emitter, eventId, key, NotificationResponseDto.create(notification));
                    }
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            // finally: try 블록에서 사용한 자원들을 정리하는 블록
            // Connection 객체가 정상적으로 닫히지 않은 경우에 대비하여 커넥션 풀에 반환하는 코드가 여기에 작성
            if (con != null) {
                // Connection 객체가 null이 아니면, 즉 연결이 정상적으로 이루어졌으면 다음 코드를 실행
                try {
                    con.setAutoCommit(true);
                    // 트랜잭션이 완료되면 자동 커밋 기능을 true로 설정합니다.
                    con.close();
                    // Connection 객체를 반환하여 커넥션 풀에 반환
                } catch (SQLException e) {

                }
            }
        }
    }

    public void sendNotification(SseEmitter emitter, String eventId, String emitterId, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(eventId)
                    .data(data));
        } catch (IOException exception) {
            emitterRepository.deleteById(emitterId);
        }

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
    @Transactional
    public List<NotificationResponseDto> getAllNotifications(Long memberId) {

        List<Notification> notifications;
        notifications = notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(memberId);
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
//
//    private String convertToJson(Member sender, Notification notification) {
//        String jsonResult = "";
//
//        NotificationResponseDto notificationResponseDto = NotificationResponseDto.of(notification, sender.getImage());
//
//        try {
//            jsonResult = objectMapper.writeValueAsString(notificationResponseDto);
//        } catch (JsonProcessingException e) {
//            throw new IllegalArgumentException("찾을 수 없음");
//        }
//
//        return jsonResult;
//    }




}
