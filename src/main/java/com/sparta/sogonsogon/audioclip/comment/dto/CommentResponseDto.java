package com.sparta.sogonsogon.audioclip.comment.dto;

import com.sparta.sogonsogon.audioclip.comment.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
public class CommentResponseDto {

    private Long id;
    private String content;
    private String membername;
    private String nickname;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public CommentResponseDto(Comment comment){
        this.id = comment.getId();
        this.content = comment.getContent();
        this.membername = comment.getMember().getMembername();
        this.nickname = comment.getMember().getNickname();
        this.createdAt = comment.getCreatedAt();
        this.modifiedAt = comment.getModifiedAt();
    }

    public static CommentResponseDto from(Comment comment){
        return CommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .membername(comment.getMember().getMembername())
                .createdAt(comment.getCreatedAt())
                .nickname(comment.getMember().getNickname())
                .modifiedAt(comment.getModifiedAt())
                .build();
    }
}
