package com.sparta.sogonsogon.audioclip.repository;

import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbum;
import com.sparta.sogonsogon.audioclip.entity.AudioClip;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AudioClipRepository extends JpaRepository<AudioClip, Long> {

    @Query("SELECT a FROM AudioClip a WHERE a.audioalbum.id = :audioAlbumId ORDER BY a.createdAt DESC")
    List<AudioClip> findTop10ByAudioAlbumIdOrderByCreatedAtDesc(@Param("audioAlbumId") Long audioAlbumId);

    @Query("SELECT a from AudioClip a where a.audioalbum.id = :audioAlbumId")
    Page<AudioClip> findAudioClipsByAudio_album_Id(@Param("audioAlbumId")Long audioAlbumId, Pageable sortedPageable);

    List<AudioClip> findByAudioalbumOrderByAudioClipLikesDesc(AudioAlbum audioalbum);


}
