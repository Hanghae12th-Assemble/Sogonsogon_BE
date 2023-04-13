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
    public SseEmitter subscribe(
                                @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return notificationService.subscribe(userDetails);
    }
        /*  subscribe() 메서드는 불필요한 코드가 아니며, 삭제하면 클라이언트(유저)는 SSE 구독을 요청할 수 없게 됩니다.
    send() 메서드를 통해 알림 메시지를 전송할 수는 있지만,
    클라이언트(유저)는 이를 수신할 수 있는 SSE 연결이 없기 때문에 알림 메시지를 실시간으로 받을 수 없게 됩니다. */


    @Operation(summary = "받은 알림 전체 조회", description = "받은 알림 전체 조회")
    @GetMapping("/AllNotifications")
    public StatusResponseDto<List<NotificationResponseDto>> getAllNotifications(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                                @RequestParam(value = "page", defaultValue = "0") int page,
                                                                                @RequestParam(value = "size",defaultValue = "10") int size) {

        return StatusResponseDto.success(HttpStatus.OK, notificationService.getAllNotifications(userDetails.getUser().getId(),page,size));
    }

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
