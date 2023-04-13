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

    private final EmitterRepository emitterRepository ;
    private final NotificationRepository notificationRepository;
    //DEFAULT_TIMEOUT을 기본값으로 설정
    private static final Long DEFAULT_TIMEOUT = 15 * 60 * 10000L; //15분
    private final DataSource dataSource;

    @Transactional
    public SseEmitter subscribe(UserDetailsImpl userDetails) {
        String emitterId = makeTimeIncludeId(userDetails.getUser().getId());
        emitterRepository.deleteAllEmitterStartWithId(String.valueOf(userDetails.getUser().getId()));
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        emitter.onCompletion(() -> { // 클라이언트와의 연결이 종료되었을 때 호출
            try {
                emitterRepository.deleteById(emitterId);
            } catch (Exception e) {
                log.error("Failed to delete emitter with id: {}", emitterId, e);
            }
        });
        emitter.onTimeout(() -> {
            // ping 메시지를 20분마다 전송
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    emitter.send(SseEmitter.event().name("ping").data(""));
                } catch (IOException e) {
                    log.error("Failed to send ping event", e);
                    emitterRepository.deleteById(emitterId);
                }
            }, 0, 20, TimeUnit.MINUTES);
            try {
                emitterRepository.deleteById(emitterId);
            } catch (Exception e) {
                log.error("Failed to delete emitter with id: {}", emitterId, e);
            }
        });
        emitter.onError((e) -> {
            emitterRepository.deleteById(emitterId);
            log.error("SSE emitter error occurred: {}", e.getMessage());
        });

        return emitter;
    }
    private String makeTimeIncludeId(Long memberId) {
        return memberId + "_" + System.currentTimeMillis();
    }
    @Transactional
    public void send(Member receiver, AlarmType alarmType, String message, String senderMembername, String senderNickname, String senderProfileImageUrl) {
        log.info("알림 보냈다. ");
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByMemberId(String.valueOf(receiver.getId()));
        emitters.forEach((key, emitter) -> {
            try {
                emitter.send(SseEmitter.event().data(NotificationResponseDto.create(createNotification(receiver, alarmType, message, senderMembername, senderNickname, senderProfileImageUrl))));
            } catch (IOException e) {
                emitterRepository.deleteById(key);
            }
        });
    }


//    public void sendNotification(SseEmitter emitter, String eventId, String emitterId, Object data) {
//        try {
//            log.info("eventId : " + eventId);
//            log.info("data" + data);
//            emitter.send(SseEmitter.event()
//                    .id(eventId)
//                    .data(data));
//        } catch (IOException exception) {
//            log.info("예외 발생해서 emitter 삭제됨");
//            emitterRepository.deleteById(emitterId);
//        }
//
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
//    @Transactional
//    public List<NotificationResponseDto> getAllNotifications(Long memberId) {
//
//        List<Notification> notifications;
//            notifications = notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(memberId);
//            log.info("알림 전체 조회했어");
//            return notifications.stream()
//                    .map(NotificationResponseDto::create)
//                    .collect(Collectors.toList());
//    }
//    // 특정 회원이 받은 알림을 확인했다는 것을 서비스에 알리는 기능
//    @Transactional
//    public NotificationResponseDto confirmNotification(Member member, Long notificationId) {
//        Notification notification = notificationRepository.findById(notificationId).orElseThrow(
//                () -> new NotFoundException("Notification not found"));
//
//        // 확인한 유저가 알림을 받은 대상자가 아니라면 예외 발생
//        if (!notification.getReceiver().getId().equals(member.getId())) {
//            throw new IllegalArgumentException("접근권한이 없습니다. ");
//        }
//        if (!notification.getIsRead()) {
//            notification.setIsRead(true);
//            notificationRepository.save(notification);
//        }
//        return new NotificationResponseDto(notification);
//    }
//    // 선택된 알림 삭제
//    @Transactional
//    public void deleteNotification(Long notificationId, Member member) {
//
//            Notification notification = notificationRepository.findById(notificationId).orElseThrow(
//                    () -> new NotFoundException("Notification not found"));
//            // 확인한 유저가 알림을 받은 대상자가 아니라면 예외 발생
//            if (!notification.getReceiver().getId().equals(member.getId())) {
//                throw new IllegalArgumentException("접근권한이 없습니다. ");
//            }
//            notificationRepository.deleteById(notificationId);
//
//    }

}
