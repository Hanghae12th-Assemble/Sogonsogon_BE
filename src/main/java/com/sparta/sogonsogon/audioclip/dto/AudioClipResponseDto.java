package com.sparta.sogonsogon.audioclip.dto;

import com.sparta.sogonsogon.audioclip.entity.Comment;
import com.sparta.sogonsogon.audioclip.entity.AudioClip;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Slf4j
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
    private String memberprofileImageUrl;
    private int isLikeCount;
    private boolean isLikeCheck;

    private List<CommentResponseDto> commentResponseDtos = new ArrayList<>();

    @Builder
    public AudioClipResponseDto(AudioClip audioClip){
        log.info(audioClip.getMember().getProfileImageUrl());
        this.id = audioClip.getId();
        this.ablumTile = audioClip.getAudioalbum().getTitle();
        this.title = audioClip.getTitle();
        this.contents = audioClip.getContents();
        this.audioclipImageUrl = audioClip.getAudioclipImageUrl();
        this.audioclipUrl = audioClip.getAudioclipUrl();
        this.membernickname = audioClip.getMember().getNickname();
        this.membername = audioClip.getMember().getMembername();
        this.memberprofileImageUrl = audioClip.getMember().getProfileImageUrl().toString();
        this.createdAt = audioClip.getCreatedAt().toString();
        this.modifiedAt = audioClip.getModifiedAt().toString();
        this.isLikeCount = audioClip.getAudioClipLikes().size();
        for (Comment comment : audioClip.getCommentList()) {
            this.commentResponseDtos.add(CommentResponseDto.from(comment));
        }
    }



    @Builder
    public AudioClipResponseDto(AudioClip audioClip, boolean isLikeCheck) {
        this.id = audioClip.getId();
        this.title = audioClip.getTitle();
        this.ablumTile = audioClip.getAudioalbum().getTitle();
        this.contents = audioClip.getContents();
        this.audioclipImageUrl = audioClip.getAudioclipImageUrl();
        this.audioclipUrl = audioClip.getAudioclipUrl();
        this.membernickname = audioClip.getMember().getNickname();
        this.membername = audioClip.getMember().getMembername();
        this.memberprofileImageUrl = audioClip.getMember().getProfileImageUrl();
        this.createdAt = audioClip.getCreatedAt().toString();
        this.modifiedAt = audioClip.getModifiedAt().toString();
        this.isLikeCount = audioClip.getAudioClipLikes().size();
        this.isLikeCheck = isLikeCheck;
        for (Comment comment : audioClip.getCommentList()) {
            this.commentResponseDtos.add(CommentResponseDto.from(comment));
        }


    }


}
