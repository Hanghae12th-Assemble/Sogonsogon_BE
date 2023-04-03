package com.sparta.sogonsogon.audioAlbum.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class AudioAlbumRequestDto {

    @NotBlank
    @Size(min = 4, max = 30)
    private String title;

    @Size(max = 100)
    private String instruction;

    private MultipartFile backgroundImageUrl;

    // TODO : categotyType 생성 및 지정
    // private CategoryType categoryType;

}
