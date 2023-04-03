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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.awt.*;
import java.io.IOException;
import java.util.Map;

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

    // 전체 오디오앨범 조회
    @GetMapping("/")
    @Operation(summary = "전체 오디오앨범 조회", description = "생성된 오디오앨범 전체를 조회한다.")
    public StatusResponseDto<Map<String, Object>> getAudioAlbums(@RequestParam(defaultValue = "1") int page,
                                                                 @RequestParam(defaultValue = "10") int size,
                                                                 @RequestParam(required = false, defaultValue = "createdAt") String sortBy) {
        return StatusResponseDto.success(HttpStatus.OK, audioAlbumService.findAllAudioAlbum(page - 1, size, sortBy));
    }

    @GetMapping("/find/{audioAlbumId}")
    @Operation(summary = "선택한 오디오앨범 조회", description = "선택한 오디오앨범 하나를 조회한다.")
    public StatusResponseDto<AudioAlbumResponseDto> getAudioAlbum(@PathVariable Long audioAlbumId) {
        return StatusResponseDto.success(HttpStatus.OK, audioAlbumService.findAudioAlbum(audioAlbumId));
    }
}
