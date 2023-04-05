package com.sparta.sogonsogon.audioAlbum.entity;

import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumRequestDto;
import com.sparta.sogonsogon.enums.CategoryType;
import com.sparta.sogonsogon.member.entity.Member;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AudioAlbum extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // 오디오 엘범 제목

    private String instruction; // 오디오 앨범 설명

    private String backgroundImageUrl; // 앨범 사진

    @Enumerated(EnumType.STRING)
    private CategoryType categoryType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_Id")
    private Member member; // 오디오앨범 생성자

    public void update(AudioAlbumRequestDto requestDto, String imageurl) {
        this.title = requestDto.getTitle();
        this.instruction = requestDto.getInstruction();
        this.categoryType = requestDto.getCategoryType();
        this.backgroundImageUrl = imageurl;
    }
}
