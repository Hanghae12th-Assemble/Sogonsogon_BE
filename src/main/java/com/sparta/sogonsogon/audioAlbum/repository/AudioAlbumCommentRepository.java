package com.sparta.sogonsogon.audioAlbum.repository;

import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbumComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AudioAlbumCommentRepository extends JpaRepository<AudioAlbumComment, Long> {
    List<AudioAlbumComment> findAllByAudioAlbumId(Long audioAlbumId);
}
