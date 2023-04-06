package com.sparta.sogonsogon.audioAlbum.entity;

import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumCommentRequestDto;
import com.sparta.sogonsogon.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "audioAlbum_comment")
public class AudioAlbumComment extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = AudioAlbum.class)
    @JoinColumn(name = "audioalbum_id")
    private AudioAlbum audioAlbum;

    public AudioAlbumComment(Member member, AudioAlbum audioAlbum, String content) {
        this.content = content;
        this.audioAlbum = audioAlbum;
        this.member = member;
    }

    public void update(AudioAlbumCommentRequestDto requestDto) {
        this.content = requestDto.getContent();
    }
}
