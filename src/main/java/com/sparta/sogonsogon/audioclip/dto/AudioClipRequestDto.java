package com.sparta.sogonsogon.audioclip.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AudioClipRequestDto {

    private String title;

    private String contents;

    private MultipartFile audioclipImage;

    private MultipartFile audioclip;



}
