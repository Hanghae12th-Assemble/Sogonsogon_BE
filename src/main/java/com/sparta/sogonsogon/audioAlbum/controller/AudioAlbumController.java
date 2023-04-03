package com.sparta.sogonsogon.audioAlbum.controller;

import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumRequestDto;
import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumResponseDto;
import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbum;
import com.sparta.sogonsogon.audioAlbum.service.AudioAlbumService;
import com.sparta.sogonsogon.dto.StatusResponseDto;
import com.sparta.sogonsogon.enums.CategoryType;
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
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audioAlbum")
public class AudioAlbumController {

    private AudioAlbumService audioAlbumService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "오디오앨범 생성", description = "오디오앨범을 생성한다.")
    public StatusResponseDto<AudioAlbumResponseDto> createAudioAlbum(@Valid @ModelAttribute AudioAlbumRequestDto requestDto,
                                                                     @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        return StatusResponseDto.success(HttpStatus.CREATED, audioAlbumService.createAudioAlbum(requestDto, userDetails));
    }

    @GetMapping("/")
    @Operation(summary = "전체 오디오앨범 조회", description = "생성된 오디오앨범 전체를 조회한다.")
    public StatusResponseDto<Map<String, Object>> getAudioAlbums(@RequestParam(defaultValue = "1") int page,
                                                                 @RequestParam(defaultValue = "10") int size,
                                                                 @RequestParam(required = false, defaultValue = "createdAt") String sortBy) {
        return StatusResponseDto.success(HttpStatus.OK, audioAlbumService.findAllAudioAlbum(page - 1, size, sortBy));
    }

    @GetMapping("/{categoryType}")
    @Operation(summary = "오디오앨범 카테고리 조회", description = "음악, 일상, 도서, ASMR의 카테고리별 생성된 오디오앨범 조회")
    public StatusResponseDto<Map<String, Object>> getAudioAlbumByCategory(@PathVariable CategoryType categoryType,
                                                                                  @RequestParam(defaultValue = "1") int page,
                                                                                  @RequestParam(defaultValue = "10") int size,
                                                                                  @RequestParam(required = false, defaultValue = "createdAt") String sortBy) {
        return StatusResponseDto.success(HttpStatus.OK, audioAlbumService.findByCategory(page - 1, size, sortBy, categoryType));
    }

    @PostMapping(value = "/update/{audioAlbumId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "오디오앨범 수정", description = "선택한 오디오앨범을 수정, 오디오앨범을 생성한 사람만 수정할 수 있다.")
    public StatusResponseDto<AudioAlbumResponseDto> updateAudioAlbum(@PathVariable Long audioAlbumId,
                                                                     @ModelAttribute AudioAlbumRequestDto requestDto,
                                                                     @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return StatusResponseDto.success(HttpStatus.OK, audioAlbumService.updateAudioAlbum(audioAlbumId, requestDto, userDetails));
    }

    @GetMapping("/find/{audioAlbumId}")
    @Operation(summary = "선택한 오디오앨범 조회", description = "선택한 오디오앨범 하나를 조회한다.")
    public StatusResponseDto<AudioAlbumResponseDto> getAudioAlbum(@PathVariable Long audioAlbumId) {
        return StatusResponseDto.success(HttpStatus.OK, audioAlbumService.findAudioAlbum(audioAlbumId));
    }

    @DeleteMapping("/{audioAlbumId}")
    @Operation(summary = "선택한 오디오앨범 삭제", description = "선택한 오디오앨범을 생성한 당사자라면 자격 확인 후 삭제")
    public StatusResponseDto<AudioAlbum> deleteAudioAlbum(@PathVariable Long audioAlbumId,
                                                          @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        audioAlbumService.deleteAudioAlbum(audioAlbumId, userDetails);
        return StatusResponseDto.success(HttpStatus.OK, null);
    }
}
