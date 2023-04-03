package com.sparta.sogonsogon.audioAlbum.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AudioAlbum extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // 오디오 엘범 제목

    private String instruction; // 오디오 앨범 설명

    private String backgroundImageUrl; // 앨범 사진

    // TODO : 카테고리 타입 제작, 필드 생성
    // private CategotyType categoryType;



}
