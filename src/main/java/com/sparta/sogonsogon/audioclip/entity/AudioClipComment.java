package com.sparta.sogonsogon.audioclip.entity;

import com.sparta.sogonsogon.audioclip.dto.CommentRequestDto;
import com.sparta.sogonsogon.audioclip.entity.AudioClip;
import com.sparta.sogonsogon.audioclip.entity.TimeStamped;
import com.sparta.sogonsogon.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "audioclip_comment")
public class AudioClipComment extends TimeStamped{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = AudioClip.class)
    @JoinColumn(name = "audioclip_id")
    private AudioClip audioclip;

    public AudioClipComment(Member member, AudioClip audioClip, String content){
        this.content = content;
        this.audioclip = audioClip;
        this.member = member;
    }

    public void update(CommentRequestDto requestDto){
        this.content = requestDto.getContent();
    }

}
