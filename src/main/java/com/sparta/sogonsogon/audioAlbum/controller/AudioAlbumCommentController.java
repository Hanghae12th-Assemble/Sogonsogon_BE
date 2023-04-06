package com.sparta.sogonsogon.audioAlbum.controller;

import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumCommentResponseDto;
import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbumComment;
import com.sparta.sogonsogon.audioAlbum.service.AudioAlbumCommentService;
import com.sparta.sogonsogon.dto.StatusResponseDto;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audioAlbum/comment")
public class AudioAlbumCommentController {

    private final AudioAlbumCommentService audioAlbumCommentService;

    @PostMapping("/{audioAlbumId}")
    @Operation(summary = "오디오앨범 댓글 생성", description = "해당 오디오앨범에 댓글을 생성합니다.")
    public StatusResponseDto<AudioAlbumCommentResponseDto> createComment(@PathVariable Long audioAlbumId,
                                                                         @RequestBody String content,
                                                                         @Parameter(hidden = true)
                                                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return StatusResponseDto.success(HttpStatus.CREATED, audioAlbumCommentService.createComment(audioAlbumId, content, userDetails));
    }


}
