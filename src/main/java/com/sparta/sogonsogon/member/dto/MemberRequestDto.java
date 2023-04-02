package com.sparta.sogonsogon.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MemberRequestDto {

    private String nickname;
    private String memberInfo;
    private MultipartFile profileImage;



}
