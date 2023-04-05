package com.sparta.sogonsogon.audioclip.dto;

import com.sparta.sogonsogon.audioclip.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class CommentResponseDto {

    private Long id;
    private String content;
    private String membername;
    private String nickname;
    private String createdAt;
    private String modifiedAt;

    public CommentResponseDto(Comment comment){
        this.id = comment.getId();
        this.content = comment.getContent();
        this.membername = comment.getMember().getMembername();
        this.nickname = comment.getMember().getNickname();
        this.createdAt = comment.getCreatedAt().toString();
        this.modifiedAt = comment.getModifiedAt().toString();
    }

    public static CommentResponseDto from(Comment comment){
        return CommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .membername(comment.getMember().getMembername())
                .createdAt(comment.getCreatedAt().toString())
                .nickname(comment.getMember().getNickname())
                .modifiedAt(comment.getModifiedAt().toString())
                .build();
    }
}
