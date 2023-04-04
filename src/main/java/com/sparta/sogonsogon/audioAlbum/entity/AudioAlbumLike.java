package com.sparta.sogonsogon.audioAlbum.entity;

import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumRequestDto;
import com.sparta.sogonsogon.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AudioAlbumLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "audioAlbum_id")
    private AudioAlbum audioAlbum;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public AudioAlbumLike(AudioAlbum audioAlbum, Member member) {
        this.audioAlbum = audioAlbum;
        this.member = member;
    }
}
