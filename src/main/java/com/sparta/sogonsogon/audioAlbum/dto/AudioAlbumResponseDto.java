package com.sparta.sogonsogon.audioAlbum.dto;

import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbum;
import com.sparta.sogonsogon.audioclip.dto.AudioClipResponseDto;
import com.sparta.sogonsogon.audioclip.entity.AudioClip;
import com.sparta.sogonsogon.enums.CategoryType;
import com.sparta.sogonsogon.member.entity.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter

@NoArgsConstructor
public class AudioAlbumResponseDto {

    private Long id;

    private String title;

    private String instruction;

    private String backgroundImageUrl;

    private String categoryType;

    private String createdAt;

    private String modifiedAt;
    private String memberName;

    private String meberNickname;

    private List<AudioClipResponseDto> audioClips;

    private boolean isLikeCheck;

    private boolean isMine;

    private int likesCount;

    @Builder
    public AudioAlbumResponseDto(AudioAlbum audioAlbum) {
        this.id = audioAlbum.getId();
        this.title = audioAlbum.getTitle();
        this.instruction = audioAlbum.getInstruction();
        this.backgroundImageUrl = audioAlbum.getBackgroundImageUrl();
        this.categoryType = audioAlbum.getCategoryType().getValue();
        this.createdAt = audioAlbum.getCreatedAt().toString();
        this.modifiedAt = audioAlbum.getModifiedAt().toString();
        this.memberName = audioAlbum.getMember().getMembername();
        this.meberNickname = audioAlbum.getMember().getNickname();
        this.likesCount = audioAlbum.getLikesCount();
    }


    public  AudioAlbumResponseDto (AudioAlbum audioAlbum, List<AudioClipResponseDto> audioClips, boolean isLikeCheck, boolean isMine) {
        this.id = audioAlbum.getId();
        this.title = audioAlbum.getTitle();
        this.instruction = audioAlbum.getInstruction();
        this.backgroundImageUrl = audioAlbum.getBackgroundImageUrl();
        this.categoryType = audioAlbum.getCategoryType().getValue();
        this.createdAt = audioAlbum.getCreatedAt().toString();
        this.modifiedAt = audioAlbum.getModifiedAt().toString();
        this.memberName = audioAlbum.getMember().getMembername();
        this.meberNickname = audioAlbum.getMember().getNickname();
        this.audioClips = audioClips;
        this.isLikeCheck = isLikeCheck;
        this.isMine = isMine;
        this.likesCount = audioAlbum.getLikesCount();
    }

    public static AudioAlbumResponseDto of(AudioAlbum audioAlbum){
        return new AudioAlbumResponseDto(audioAlbum);
    }
}
