package com.sparta.sogonsogon.audioAlbum.controller;

import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumIsLikeResponseDto;
import com.sparta.sogonsogon.audioAlbum.service.AudioAlbumLikeService;
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
@RequestMapping("/api/audioAlbum")
public class AudioAlbumLikeController {

    private AudioAlbumLikeService audioAlbumLikeService;


    @PostMapping("/like/{audioAlbumId}")
    @Operation(summary = "선택한 오디오앨범 좋아요", description = "선택한 오디오앨범에 좋아요를 추가하거나 취소한다.")
    public StatusResponseDto<AudioAlbumIsLikeResponseDto> likeAudioAlbum(@PathVariable Long audioAlbumId,
                                                                         @Parameter(hidden = true)
                                                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return audioAlbumLikeService.likeAudioAlbum(audioAlbumId, userDetails);
    }
}
