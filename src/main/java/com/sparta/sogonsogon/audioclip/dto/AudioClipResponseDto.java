package com.sparta.sogonsogon.audioclip.dto;

import com.sparta.sogonsogon.audioclip.comment.dto.CommentResponseDto;
import com.sparta.sogonsogon.audioclip.comment.entity.Comment;
import com.sparta.sogonsogon.audioclip.entity.AudioClip;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class AudioClipResponseDto {

    private Long id;
    private String title;
    private String contents;
    private String audioclipImageUrl;
    private String audioclipUrl;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private String membernickname;
    private String membername; //오디오 올린 주인 고유 아이디

    private List<CommentResponseDto> commentResponseDtos = new ArrayList<>();

    @Builder
    public AudioClipResponseDto(AudioClip audioClip){
        this.id = audioClip.getId();
        this.title = audioClip.getTitle();
        this.contents = audioClip.getContents();
        this.audioclipImageUrl = audioClip.getAudioclipImageUrl();
        this.audioclipUrl = audioClip.getAudioclipUrl();
        this.membernickname = audioClip.getMember().getNickname();
        this.membername = audioClip.getMember().getMembername();
        this.createdAt = audioClip.getCreatedAt();
        this.modifiedAt = audioClip.getModifiedAt();
        for (Comment comment : audioClip.getCommentList()) {
            this.commentResponseDtos.add(CommentResponseDto.from(comment));
        }

    }


}
