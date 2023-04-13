package com.sparta.sogonsogon.audioclip.dto;

import com.sparta.sogonsogon.audioclip.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Builder
@Data
@AllArgsConstructor
@Slf4j
public class CommentResponseDto {

    private Long id;
    private String content;
    private String membername;
    private String nickname;
    private String createdAt;
    private String modifiedAt;
    private String memberImageUrl;
    private boolean isMine;

    public CommentResponseDto(Comment comment, boolean isMine){
        this.id = comment.getId();
        this.content = comment.getContent();
        this.membername = comment.getMember().getMembername();
        this.memberImageUrl = comment.getMember().getProfileImageUrl().toString();
        this.nickname = comment.getMember().getNickname();
        this.createdAt = comment.getCreatedAt().toString();
        this.modifiedAt = comment.getModifiedAt().toString();
        this.isMine = isMine;
    }

    public CommentResponseDto(Comment comment){
        this.id = comment.getId();
        this.content = comment.getContent();
        this.membername = comment.getMember().getMembername();
        this.memberImageUrl = comment.getMember().getProfileImageUrl().toString();
        this.nickname = comment.getMember().getNickname();
        this.createdAt = comment.getCreatedAt().toString();
        this.modifiedAt = comment.getModifiedAt().toString();
    }

    public static CommentResponseDto from(Comment comment){
        log.info(comment.getMember().getProfileImageUrl());
        return CommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .membername(comment.getMember().getMembername())
                .memberImageUrl(comment.getMember().getProfileImageUrl().toString())
                .createdAt(comment.getCreatedAt().toString())
                .nickname(comment.getMember().getNickname())
                .modifiedAt(comment.getModifiedAt().toString())
                .build();
    }
}
