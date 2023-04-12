package com.sparta.sogonsogon.audioAlbum.entity;

import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumRequestDto;
import com.sparta.sogonsogon.audioclip.entity.AudioClip;
import com.sparta.sogonsogon.enums.CategoryType;
import com.sparta.sogonsogon.member.entity.Member;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "audioalbum")
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

    @OneToMany(mappedBy = "audioalbum", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    List<AudioClip> audioClips = new ArrayList<>();

    @OneToMany(mappedBy = "audioAlbum", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AudioAlbumLike> audioalbumLikes = new ArrayList<>();
    private int likesCount;

    public void updateLikesCnt(int likesCount) {
        this.likesCount = likesCount;
    }


    public void update(AudioAlbumRequestDto requestDto, String imageurl) {
        this.title = requestDto.getTitle();
        this.instruction = requestDto.getInstruction();
        this.categoryType = requestDto.getCategoryType();
        this.backgroundImageUrl = imageurl;
    }
}
