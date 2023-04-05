package com.sparta.sogonsogon.audioclip.repository;

import com.sparta.sogonsogon.audioclip.entity.AudioClip;
import com.sparta.sogonsogon.audioclip.entity.AudioClipLike;
import com.sparta.sogonsogon.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AudioClipLikeRepository extends JpaRepository<AudioClipLike, Long> {
    Optional<AudioClipLike> findByAudioclipAndMember(AudioClip audioClip, Member user);
}
