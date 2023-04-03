package com.sparta.sogonsogon.audioAlbum.dto;

import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbum;
import com.sparta.sogonsogon.enums.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AudioAlbumResponseDto {

    private Long id;

    private String title;

    private String instruction;

    private String backgroundImageUrl;

    private CategoryType categoryType;

    private String createdAt;

    private String modifiedAt;

    // TODO : CategoryType 생성, 적용
    // private CategrouType categoryType;
    public AudioAlbumResponseDto(AudioAlbum audioAlbum) {
        this.id = audioAlbum.getId();
        this.title = audioAlbum.getTitle();
        this.instruction = audioAlbum.getInstruction();
        this.backgroundImageUrl = audioAlbum.getBackgroundImageUrl();
        this.categoryType = audioAlbum.getCategoryType();
        this.createdAt = audioAlbum.getCreatedAt().toString();
        this.modifiedAt = audioAlbum.getModifiedAt().toString();
    }


    public static AudioAlbumResponseDto of(AudioAlbum audioAlbum) {
        return new AudioAlbumResponseDto(audioAlbum);
    }
}
