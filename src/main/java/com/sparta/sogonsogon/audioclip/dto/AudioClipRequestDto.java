package com.sparta.sogonsogon.audioclip.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AudioClipRequestDto {

    private String title;

    private String contents;

    private String audioclipImageUrl;

    private String audioclipUrl;



}
