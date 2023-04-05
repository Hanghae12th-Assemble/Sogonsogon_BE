package com.sparta.sogonsogon.audioclip.entity;

import com.sparta.sogonsogon.audioclip.entity.AudioClip;
import com.sparta.sogonsogon.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
@AllArgsConstructor
@Table(name = "audioclip_like")
public class AudioClipLike {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "audioclip_id")
    private AudioClip audioclip;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public AudioClipLike(AudioClip audioClip, Member member){
        this.audioclip = audioClip;
        this.member = member;
    }

}
