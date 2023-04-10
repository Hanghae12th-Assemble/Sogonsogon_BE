package com.sparta.sogonsogon.noti.service;

import com.amazonaws.services.kms.model.NotFoundException;
import com.sparta.sogonsogon.enums.ErrorMessage;
import com.sparta.sogonsogon.member.entity.Member;
import com.sparta.sogonsogon.member.repository.MemberRepository;
import com.sparta.sogonsogon.noti.dto.NotificationResponseDto;
import com.sparta.sogonsogon.noti.entity.Notification;
import com.sparta.sogonsogon.noti.repository.EmitterRepository;
import com.sparta.sogonsogon.noti.repository.NotificationRepository;
import com.sparta.sogonsogon.noti.util.AlarmType;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmitterRepository emitterRepository ;
    private final NotificationRepository notificationRepository;

    private final MemberRepository memberRepository;
    //DEFAULT_TIMEOUT을 기본값으로 설정
    private static final Long DEFAULT_TIMEOUT = 60 * 60 * 10000L;


    public SseEmitter subscribe(UserDetailsImpl userDetails) {
        String emitterId = makeTimeIncludeId(userDetails.getUser().getId());
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));
        /* 이 코드는 SseEmitter 객체를 생성하고 emitterRepository를 사용하여 저장하는 부분입니다.
        SseEmitter는 Server-Sent Events(SSE)를 사용하여 실시간으로 클라이언트와 통신할 수 있는 객체입니다.
        save() 메서드를 사용하여 emitterId와 함께 emitterRepository에 저장하면,나중에 해당 emitter를 식별하고 관리할 수 있습니다.
        이 코드는 subscribe() 메서드에서 클라이언트가 새로운 SSE를 구독할 때마다 실행되며,
        새로운 SseEmitter 객체를 생성하고 이를 emitterRepository에 저장합니다.
        이후 생성된 SseEmitter는 클라이언트에게 실시간으로 알림을 보내는 데 사용됩니다. */

        send(userDetails.getUser(),AlarmType.eventSystem,"회원님이 알림 구독하였습니다.",null,null,null);
        log.info("본인 구독하였습니다.");

        emitter.onCompletion(() -> emitterRepository.deleteById(emitterId)); //onCompletion 메서드: SseEmitter가 완료될 때 호출되는 콜백 함수를 정의
        emitter.onTimeout(() -> emitterRepository.deleteById(emitterId)); //  SSEEmitter를 찾아 emitterRepository에서 삭제하는 메서드

        String eventId = makeTimeIncludeId(userDetails.getUser().getId());
        sendNotification(emitter, eventId, emitterId, "EventStream Created. [userId=" + userDetails.getUser().getId() + "]");

        return emitter;
    }

    private String makeTimeIncludeId(Long memberId) {
        return memberId + "_" + System.currentTimeMillis();
    }

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
            emitter.send(SseEmitter.event()
                    .id(eventId)
                    .data(data));
        } catch (IOException exception) {
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
    public void deleteNotification(Long notificationId, Member member) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(
                () -> new NotFoundException("Notification not found"));

        // 확인한 유저가 알림을 받은 대상자가 아니라면 예외 발생
        if (!notification.getReceiver().getId().equals(member.getId())) {
            throw new IllegalArgumentException("접근권한이 없습니다. ");
        }
        notificationRepository.deleteById(notificationId);

    }
}
