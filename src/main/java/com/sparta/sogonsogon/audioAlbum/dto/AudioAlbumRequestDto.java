package com.sparta.sogonsogon.audioAlbum.dto;

import com.sparta.sogonsogon.enums.CategoryType;
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
    private String title;

    private String instruction;

    private MultipartFile backgroundImageUrl; // 이미지
    private CategoryType categoryType;

}
