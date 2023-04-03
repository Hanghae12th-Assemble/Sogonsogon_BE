package com.sparta.sogonsogon.noti.controller;

import com.sparta.sogonsogon.dto.StatusResponseDto;
import com.sparta.sogonsogon.noti.dto.NotificationResponseDto;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import com.sparta.sogonsogon.noti.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notificaiton")
public class NotificationController {

    private final NotificationService notificationService;
    @ApiOperation(value = "알림 구독", notes = "알림을 구독한다.")
    @Operation(summary = "알림 구독", description = "알림 구독")
    @GetMapping(value = "/", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public SseEmitter subscribe(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return notificationService.subscribe(userDetails.getUser().getId() );
    }


    @Operation(summary = "받은 알림 전체 조회", description = "받은 알림 전체 조회")
    @GetMapping("/AllNotifications")
    public StatusResponseDto<List<NotificationResponseDto>> getAllNotifications(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {

        return StatusResponseDto.success(HttpStatus.OK, notificationService.getAllNotifications(userDetails.getUser().getId()));
    }


    @PutMapping("/{notificationId}/confirm")
    @Operation(summary = "알림확인", description = "알림확인")
    public StatusResponseDto<NotificationResponseDto> confirmNotification(@PathVariable Long notificationId,
                                                                          @ApiIgnore @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        NotificationResponseDto notificationResponseDto = notificationService.confirmNotification(userDetails.getUser(),notificationId);
        return StatusResponseDto.success(HttpStatus.OK, notificationResponseDto);

    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "받은 알림 선택하여 삭제", description = "받은 알림 선택하여 삭제")
    public StatusResponseDto<NotificationResponseDto> deleteNotification(@PathVariable Long notificationId,
                                                                         @ApiIgnore @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails){
        notificationService.deleteNotification(notificationId,userDetails.getUser());
        return StatusResponseDto.success(HttpStatus.OK,null);
    }
}
