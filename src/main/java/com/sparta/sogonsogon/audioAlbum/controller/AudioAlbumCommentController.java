package com.sparta.sogonsogon.audioAlbum.controller;

import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumCommentRequestDto;
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

import java.util.List;

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

    @PostMapping("/{commentId}")
    @Operation(summary = "오디오 앨범 댓글 수정", description = "해당 오디오 앨범에 있는 댓글을 수정한다.")
    public StatusResponseDto<AudioAlbumCommentResponseDto> updateComment(@PathVariable Long audioAlbumCommentId,
                                                                         @RequestBody AudioAlbumCommentRequestDto requestDto,
                                                                         @Parameter(hidden = true)
                                                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return StatusResponseDto.success(HttpStatus.OK, audioAlbumCommentService.updateComment(audioAlbumCommentId, requestDto, userDetails));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "오디오앨범 댓글 삭제", description = "해당 오디오앨범에 있는 댓글을 삭제합니다.")
    public StatusResponseDto<String> deleteComment(@PathVariable Long AudioAlbumCommentId,
                                                   @Parameter(hidden = true)
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return StatusResponseDto.success(HttpStatus.OK, audioAlbumCommentService.deleteComment(AudioAlbumCommentId, userDetails));
    }

    @GetMapping("/{audioAlbumId}")
    @Operation(summary = "오디오앨범 댓글 전체 조회", description = "오디오앨범에 있는 댓글 전체를 조회합니다.")
    public StatusResponseDto<List<AudioAlbumCommentResponseDto>> getComments(@PathVariable Long audioAlbumId) {
        return StatusResponseDto.success(HttpStatus.OK, audioAlbumCommentService.getComments(audioAlbumId));
    }


}
