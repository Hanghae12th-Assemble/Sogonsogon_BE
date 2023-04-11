package com.sparta.sogonsogon.audioclip.entity;

import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbum;
import com.sparta.sogonsogon.audioclip.dto.AudioClipRequestDto;
import com.sparta.sogonsogon.member.entity.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "audioclip")
public class AudioClip extends TimeStamped{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String contents;

    @Column(nullable = true)
    private String audioclipImageUrl;

    @Column(nullable = false)
    private String audioclipUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private int order;

    @OneToMany(mappedBy = "audioclip", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @OrderBy("createdAt desc")
    List<Comment> commentList = new ArrayList<>();

    @OneToMany(mappedBy = "audioclip", cascade = CascadeType.REMOVE)
    List<AudioClipLike> audioClipLikes = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "audioalbum_id")
    private AudioAlbum audioalbum;


    @Builder
    public AudioClip(AudioClipRequestDto requestDto, Member member, String audioclipUrl, String audioclipImageUrl, AudioAlbum audioalbum){
        this.title = requestDto.getTitle();
        this.contents = requestDto.getContents();
        this.audioclipImageUrl = audioclipImageUrl;
        this.audioclipUrl = audioclipUrl;
        this.member = member;
        this.audioalbum = audioalbum;
    }

    public void update (AudioClipRequestDto requestDto, String audioclipUrl, String audioclipImageUrl){
        this.title = requestDto.getTitle();
        this.contents = requestDto.getContents();
        this.audioclipUrl = audioclipUrl;
        this.audioclipImageUrl = audioclipImageUrl;
    }

}
