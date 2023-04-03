package com.sparta.sogonsogon.audioAlbum.dto;

import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbum;
import com.sparta.sogonsogon.enums.CategoryType;
import com.sparta.sogonsogon.member.entity.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AudioAlbumResponseDto {

    private Long id;

    private String title;

    private String instruction;

    private String backgroundImageUrl;

    private CategoryType categoryType;

    private String createdAt;

    private String modifiedAt;
    private String memberName;

    private String meberNickname;
    @Builder
    public AudioAlbumResponseDto(AudioAlbum audioAlbum) {
        this.id = audioAlbum.getId();
        this.title = audioAlbum.getTitle();
        this.instruction = audioAlbum.getInstruction();
        this.backgroundImageUrl = audioAlbum.getBackgroundImageUrl();
        this.categoryType = audioAlbum.getCategoryType();
        this.createdAt = audioAlbum.getCreatedAt().toString();
        this.modifiedAt = audioAlbum.getModifiedAt().toString();
        this.memberName = audioAlbum.getMember().getMembername();
        this.meberNickname = audioAlbum.getMember().getNickname();
    }


    public static AudioAlbumResponseDto of(AudioAlbum audioAlbum) {
        return new AudioAlbumResponseDto(audioAlbum);
    }
}
