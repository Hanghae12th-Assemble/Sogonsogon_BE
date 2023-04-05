package com.sparta.sogonsogon.noti.controller;

import com.sparta.sogonsogon.dto.StatusResponseDto;
import com.sparta.sogonsogon.noti.dto.NotificationResponseDto;
import com.sparta.sogonsogon.noti.repository.NotificationRepository;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import com.sparta.sogonsogon.noti.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequiredArgsConstructor //생성자를 자동으로 생성
@RequestMapping("/api/notificaiton")
public class NotificationController {
    private final NotificationRepository notificationRepository;

    private final NotificationService notificationService;
    @ApiOperation(value = "알림 구독", notes = "알림을 구독한다.")
    @Operation(summary = "알림 구독", description = "알림 구독")
    @GetMapping(value = "/", produces = MediaType.TEXT_EVENT_STREAM_VALUE)///subscribe 엔드포인트로 들어오는 요청을 처리. produces 속성은 해당 메서드가 반환하는 데이터 형식을 지정
    @ResponseStatus(HttpStatus.OK)
    // 해당 메서드가 반환하는 HTTP 응답 코드를 지정합니다.
    // 이 경우 HttpStatus.OK 즉, 200을 반환합니다.
    public SseEmitter subscribe(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return notificationService.subscribe( userDetails.getUser().getId() );
    }


    @Operation(summary = "받은 알림 전체 조회", description = "받은 알림 전체 조회")
    @GetMapping("/AllNotifications")
    public StatusResponseDto<List<NotificationResponseDto>> getAllNotifications(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
//        List<Notification> notifications = notificationService.getNotificationsByReceiverId(receiverId, userDetails);
//        return notifications.stream()
//                .map(NotificationResponseDto::create)
//                .collect(Collectors.toList());
        return StatusResponseDto.success(HttpStatus.OK, notificationService.getAllNotifications(userDetails.getUser().getId()));
    }

//    @GetMapping("/{notificationId}")
//    @Operation(summary = "받은 알림 선택하여 조회", description = "받은 알림 선택하여 조회")
//    public NotificationResponseDto getNotification(@PathVariable Long notificationId,
//                                                   @ApiIgnore @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
//        return notificationService.getNotification(notificationId,userDetails);
//    }

    @PutMapping("/{notificationId}/confirm")
    @Operation(summary = "알림확인", description = "알림확인")
    public StatusResponseDto<NotificationResponseDto> confirmNotification(@PathVariable Long notificationId,
                                                                          @ApiIgnore @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        NotificationResponseDto notificationResponseDto = notificationService.confirmNotification(userDetails.getUser(),notificationId);
        return StatusResponseDto.success(HttpStatus.OK, notificationResponseDto);
//        return ResponseEntity.ok("Notification confirmed successfully");

    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "받은 알림 선택하여 삭제", description = "받은 알림 선택하여 삭제")
    public StatusResponseDto<NotificationResponseDto> deleteNotification(@PathVariable Long notificationId,
                                                                         @ApiIgnore @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails){
        notificationService.deleteNotification(notificationId,userDetails.getUser());
        return StatusResponseDto.success(HttpStatus.OK,null);
    }
}
