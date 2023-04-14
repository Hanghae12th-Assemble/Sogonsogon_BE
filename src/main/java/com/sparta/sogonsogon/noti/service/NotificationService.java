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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Setter
public class NotificationService {

    private final EmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;
    //DEFAULT_TIMEOUT을 기본값으로 설정
    private static final Long DEFAULT_TIMEOUT = 15 * 60 * 10000L;
    private final JdbcTemplate jdbcTemplate;
    private final RedisTemplate<String, Object> redisTemplate;


    @Transactional
    public SseEmitter subscribe(UserDetailsImpl userDetails) {
        String emitterId = makeTimeIncludeId(userDetails.getUser().getId());
//        emitterRepository.deleteAllEmitterStartWithId(String.valueOf(userDetails.getUser().getId()));
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));
        log.info("SSE 연결 됨");

        emitter.onCompletion(() -> {
            emitterRepository.deleteById(emitterId);
        });
        //시간이 만료된 경우 자동으로 레포지토리에서 삭제하고 클라이언트에서 재요청을 보낸다.
        emitter.onTimeout(() -> {
            emitterRepository.deleteById(emitterId);
        });
        emitter.onError((e) -> emitterRepository.deleteById(emitterId));
        return emitter;
    }
    private String makeTimeIncludeId(Long memberId) {
        return memberId + "_" + System.currentTimeMillis();
    }
    @Transactional
    public void send(Member receiver, AlarmType alarmType, String message, String senderMembername, String senderNickname, String senderProfileImageUrl) {
        // JdbcTemplate를 사용하여 데이터베이스 연결
        // jdbcTemplate.update() 메서드를 사용하여 INSERT 쿼리를 실행하면
        // JdbcTemplate 내부에서 커넥션을 가져오고, 사용 후 자동으로 반환
        jdbcTemplate.update(
                "INSERT INTO notification (receiver_id, alarm_type, message, sender_membername, sender_nickname, sender_profile_image_url) VALUES (?, ?, ?, ?, ?, ?)",
                receiver.getId(),
                alarmType.toString(),
                message,
                senderMembername,
                senderNickname,
                senderProfileImageUrl
        );
        Notification notification = notificationRepository.save(createNotification(receiver, alarmType, message, senderMembername, senderNickname, senderProfileImageUrl));
        String receiverId = String.valueOf(receiver.getId());
        String eventId = receiverId + "_" + System.currentTimeMillis();

//        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByMemberId(receiverId);
//        emitters.forEach((key, emitter) -> {
//                    emitterRepository.saveEventCache(key, notification);
//                    sendNotification(emitter, eventId, key, NotificationResponseDto.create(notification));
//        });
        Set<String> emitters = redisTemplate.keys(receiverId + "*");
        emitters.forEach(emitterId -> {
            redisTemplate.opsForList().rightPush(emitterId, notification);
            SseEmitter emitter = (SseEmitter) redisTemplate.opsForValue().get(emitterId);
            sendNotification(emitter, eventId, emitterId, NotificationResponseDto.create(notification));
        });
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
    public List<NotificationResponseDto> getAllNotifications(Long memberId, int size, int page) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notifications = notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(memberId,pageable);
        // 36시간이 지난 알림 삭제
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(36);
        List<Notification> expiredNotifications = notificationRepository.findExpiredNotification(cutoffTime);
        notificationRepository.deleteAll(expiredNotifications);

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
