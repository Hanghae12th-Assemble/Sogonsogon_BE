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
import com.sparta.sogonsogon.noti.util.DataSourceConfig;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
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
    //DEFAULT_TIMEOUT을 기본값으로 설정
    private static final Long DEFAULT_TIMEOUT = 60 * 60 * 10000L;
    private final DataSource dataSource;

    @Transactional
    public SseEmitter subscribe(UserDetailsImpl userDetails) {
        String emitterId = makeTimeIncludeId(userDetails.getUser().getId());
        emitterRepository.deleteAllEmitterStartWithId(String.valueOf(userDetails.getUser().getId()));
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        emitter.onCompletion(() -> { // 클라이언트와의 연결이 종료되었을 때 호출
            emitterRepository.deleteById(emitterId);
        });
        emitter.onTimeout(() -> { // SseEmitter의 유효시간이 지났을 때 호출
            log.info("SSE 연결 Timeout");
            emitterRepository.deleteById(emitterId);
        });
        emitter.onError((e) -> emitterRepository.deleteById(emitterId));

        String eventId = makeTimeIncludeId(userDetails.getUser().getId());
        sendNotification(emitter, eventId, emitterId, "EventStream Created. [userId=" + userDetails.getUser().getId() + "]");

        return emitter;
    }
    private String makeTimeIncludeId(Long memberId) {
        return memberId + "_" + System.currentTimeMillis();
    }
    @Transactional
    public void send(Member receiver, AlarmType alarmType, String message, String senderMembername, String senderNickname, String senderProfileImageUrl) {
        try (Connection con = DataSourceUtils.getConnection(dataSource)) {
            con.setAutoCommit(false);
            Notification notification = notificationRepository.save(createNotification(receiver, alarmType, message, senderMembername, senderNickname, senderProfileImageUrl));
            String receiverId = String.valueOf(receiver.getId());
            String eventId = receiverId + "_" + System.currentTimeMillis();
            Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByMemberId(receiverId);
            emitters.forEach(
                    (key, emitter) -> {
                        emitterRepository.saveEventCache(key, notification);
                        sendNotification(emitter, eventId, key, NotificationResponseDto.create(notification));
                    }
            );
            con.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
//    private boolean hasLostData(String lastEventId) {
//        return !lastEventId.isEmpty();
//    }
//    private void sendLostData(String lastEventId, Long memberId, String emitterId, SseEmitter emitter) {
//        Map<String, Object> eventCaches = emitterRepository.findAllEventCacheStartWithByMemberId(String.valueOf(memberId));
//
//        eventCaches.entrySet().stream()
//                .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
//                .forEach(entry -> sendNotification(emitter, entry.getKey(), emitterId, entry.getValue()));
//    }
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

        List<Notification> notifications;

        try (Connection con = DataSourceUtils.getConnection(dataSource)) {
            notifications = notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(memberId);
            log.info("알림 전체 조회했어");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

//    // 클라이언트 타임아웃 처리
//    private void onClientDisconnect(SseEmitter emitter, String type) {
//        try {
//            emitter.send(SseEmitter.event().name(type).data("Client" + type).id(String.valueOf(UUID.randomUUID())));
//            emitter.complete();
//        } catch (IOException e) {
//            log.error("Failed to send" + type + "event to client", e);
//        }
//    }
//
//    // 처리 된 건이고, 읽은 알림은 삭제 (1일 경과한 데이터)
//    @Transactional
//    public void deleteOldNotification() {
//        List<Notification> notifications = notificationRepository.findOldNotification();
//
//        log.info("총 " + notifications.size() + " 건의 알림 삭제");
//        for(Notification notification : notifications){
//            notificationRepository.deleteById(notification.getId());
//        }
//    }
}
