package com.sparta.sogonsogon.audioclip.like.controller;

import com.sparta.sogonsogon.audioclip.like.dto.AudioClipIsLikeResponseDto;
import com.sparta.sogonsogon.audioclip.like.service.AudioClipLikeService;
import com.sparta.sogonsogon.dto.StatusResponseDto;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audioclip/like")
public class AudioClipLikeController {

    private final AudioClipLikeService audioClipLikeService;

    @PostMapping("/{audioclipId}")
    @Operation(summary = "오디오 클립 좋아요 기능", description = "해당 오디오클립에 좋아요가 추가 또는 삭제가 가능합니다.")
    public StatusResponseDto<AudioClipIsLikeResponseDto> likeAudioClip(@PathVariable Long audioclipId,
                                                                       @Parameter(hidden = true)
                                                                       @AuthenticationPrincipal UserDetailsImpl userDetails){
        return audioClipLikeService.likeAudioClip(audioclipId, userDetails);
    }
}
