package com.sparta.sogonsogon.audioAlbum.repository;

import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbum;
import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbumLike;
import com.sparta.sogonsogon.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AudioAlbumLikeRepository extends JpaRepository<AudioAlbumLike, Long> {

    Optional<AudioAlbumLike> findByAudioAlbumAndMember(AudioAlbum audioAlbum, Member member);
}
