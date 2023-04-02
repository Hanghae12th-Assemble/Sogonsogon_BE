package com.sparta.sogonsogon.audioclip.like.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AudioClipIsLikeResponseDto  {

        private String result;
        private boolean isLike;

        public AudioClipIsLikeResponseDto(String result, boolean isLike){
            this.result = result;
            this.isLike = isLike;
        }
}
