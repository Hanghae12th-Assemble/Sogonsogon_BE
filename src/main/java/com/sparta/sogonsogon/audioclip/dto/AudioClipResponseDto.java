package com.sparta.sogonsogon.audioclip.dto;

import com.sparta.sogonsogon.audioclip.entity.Comment;
import com.sparta.sogonsogon.audioclip.entity.AudioClip;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AudioClipResponseDto {

    private Long id;
    private String ablumTile;
    private String title;
    private String contents;
    private String audioclipImageUrl;
    private String audioclipUrl;
    private String createdAt;
    private String modifiedAt;
    private String membernickname;
    private String membername; //오디오 올린 주인 고유 아이디

    private int isLikeCount;
    private boolean isLikeCheck;

    private List<AudioClipCommentResponseDto> audioClipCommentResponseDtos = new ArrayList<>();

    @Builder
    public AudioClipResponseDto(AudioClip audioClip){
        this.id = audioClip.getId();
        this.ablumTile = audioClip.getAudio_album().getTitle();
        this.title = audioClip.getTitle();
        this.contents = audioClip.getContents();
        this.audioclipImageUrl = audioClip.getAudioclipImageUrl();
        this.audioclipUrl = audioClip.getAudioclipUrl();
        this.membernickname = audioClip.getMember().getNickname();
        this.membername = audioClip.getMember().getMembername();
        this.createdAt = audioClip.getCreatedAt().toString();
        this.modifiedAt = audioClip.getModifiedAt().toString();
        this.isLikeCount = audioClip.getAudioClipLikes().size();
        for (Comment comment : audioClip.getCommentList()) {
            this.audioClipCommentResponseDtos.add(AudioClipCommentResponseDto.from(comment));
        }
    }



    @Builder
    public AudioClipResponseDto(AudioClip audioClip, boolean isLikeCheck) {
        this.id = audioClip.getId();
        this.title = audioClip.getTitle();
        this.contents = audioClip.getContents();
        this.audioclipImageUrl = audioClip.getAudioclipImageUrl();
        this.audioclipUrl = audioClip.getAudioclipUrl();
        this.membernickname = audioClip.getMember().getNickname();
        this.membername = audioClip.getMember().getMembername();
        this.createdAt = audioClip.getCreatedAt().toString();
        this.modifiedAt = audioClip.getModifiedAt().toString();
        this.isLikeCount = audioClip.getAudioClipLikes().size();
        this.isLikeCheck = isLikeCheck;
        for (Comment comment : audioClip.getCommentList()) {
            this.audioClipCommentResponseDtos.add(AudioClipCommentResponseDto.from(comment));
        }


    }
}
