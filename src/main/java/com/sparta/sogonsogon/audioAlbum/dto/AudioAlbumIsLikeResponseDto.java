package com.sparta.sogonsogon.audioAlbum.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AudioAlbumIsLikeResponseDto {

    private String result;

    private boolean isLike;

    public AudioAlbumIsLikeResponseDto(String result, boolean isLike) {
        this.result = result;
        this.isLike = isLike;
    }
}
