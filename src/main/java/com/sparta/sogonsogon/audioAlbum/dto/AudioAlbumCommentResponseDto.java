package com.sparta.sogonsogon.audioAlbum.dto;

import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbumComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class AudioAlbumCommentResponseDto {

    private Long id;

    private String content;

    private String membername;

    private String nickname;

    private String createdAt;

    private String modifiedAt;

    public AudioAlbumCommentResponseDto(AudioAlbumComment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.membername = comment.getMember().getMembername();
        this.nickname = comment.getMember().getNickname();
        this.createdAt = comment.getCreatedAt().toString();
        this.modifiedAt = comment.getModifiedAt().toString();
    }

    public static AudioAlbumCommentResponseDto from(AudioAlbumComment comment) {
        return AudioAlbumCommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .membername(comment.getMember().getMembername())
                .createdAt(comment.getCreatedAt().toString())
                .nickname(comment.getMember().getNickname())
                .modifiedAt(comment.getModifiedAt().toString())
                .build();
    }
}
