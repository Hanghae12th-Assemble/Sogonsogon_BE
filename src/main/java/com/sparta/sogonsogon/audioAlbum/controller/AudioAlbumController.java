package com.sparta.sogonsogon.audioAlbum.controller;

import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumRequestDto;
import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumResponseDto;
import com.sparta.sogonsogon.audioAlbum.service.AudioAlbumService;
import com.sparta.sogonsogon.dto.StatusResponseDto;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.awt.*;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audioAlbum")
public class AudioAlbumController {

    private AudioAlbumService audioAlbumService;

    // 오디오앨범 생성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "오디오앨범 생성", description = "오디오앨범을 생성한다.")
    public StatusResponseDto<AudioAlbumResponseDto> createAudioAlbum(@Valid @ModelAttribute AudioAlbumRequestDto requestDto,
                                                                     @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        return StatusResponseDto.success(HttpStatus.CREATED, audioAlbumService.createAudioAlbum(requestDto, userDetails));
    }
}
