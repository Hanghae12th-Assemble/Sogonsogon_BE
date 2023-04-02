package com.sparta.sogonsogon.audioclip.controller;

import com.sparta.sogonsogon.audioclip.dto.AudioClipRequestDto;
import com.sparta.sogonsogon.audioclip.dto.AudioClipResponseDto;
import com.sparta.sogonsogon.audioclip.service.AudioClipService;
import com.sparta.sogonsogon.dto.StatusResponseDto;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import com.sparta.sogonsogon.util.S3Uploader;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audioclip")
public class AudioClipController {

    private final AudioClipService audioClipService;
    private final S3Uploader s3Uploader;

    @PostMapping("/uploaded")
    @Operation(summary = "오디오 클립 생성", description = "오디오 클립을 생성 할 수 있습니다. ")
    public StatusResponseDto<AudioClipResponseDto> createdAudioClip(@RequestParam(value = "title") String title,
                                                                    @RequestParam(value = "content") String content,
                                                                    @RequestParam(value = "audioclipImage" )MultipartFile audioclipImage,
                                                                    @RequestParam(value = "audioclip") MultipartFile audioclip,
                                                                    @Parameter(hidden = true)
                                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        String audioclipImageUrl = s3Uploader.uploadFiles(audioclipImage, "audioclipImage/");
        String audioclipUrl = s3Uploader.uploadFiles(audioclip, "audioclips/");
        AudioClipRequestDto requestDto = new AudioClipRequestDto(title, content, audioclipImageUrl, audioclipUrl);
        return audioClipService.createdAudioClip(requestDto, userDetails);
    }

    @PutMapping("/updated/{audioclipId}")
    @Operation(summary = "오디오 클립 수정", description = "오디오 내부 내용을 전체 수정 할 수 있습니다. ")
    public StatusResponseDto<AudioClipResponseDto> updatedAudioClip(@PathVariable Long audioclipId,
                                                                    @RequestParam(value = "title") String title,
                                                                    @RequestParam(value = "content") String content,
                                                                    @RequestParam(value = "audioclipImage" )MultipartFile audioclipImage,
                                                                    @RequestParam(value = "audioclip") MultipartFile audioclip,
                                                                    @Parameter(hidden = true)
                                                                        @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        String audioclipImageUrl = s3Uploader.uploadFiles(audioclipImage, "audioclipImage/");
        String audioclipUrl = s3Uploader.uploadFiles(audioclip, "audioclips/");
        AudioClipRequestDto requestDto = new AudioClipRequestDto(title, content, audioclipImageUrl, audioclipUrl);
        return audioClipService.updateAudioClip(audioclipId, requestDto, userDetails);
    }



}
