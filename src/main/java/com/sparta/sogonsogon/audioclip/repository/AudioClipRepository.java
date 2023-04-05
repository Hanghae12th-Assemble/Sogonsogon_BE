package com.sparta.sogonsogon.audioclip.repository;

import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbum;
import com.sparta.sogonsogon.audioclip.entity.AudioClip;
import org.hibernate.sql.Select;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

import static org.hibernate.loader.Loader.SELECT;

public interface AudioClipRepository extends JpaRepository<AudioClip, Long> {



    @Query("SELECT a FROM AudioClip a WHERE a.audio_album.id = :audioAlbumId ORDER BY a.createdAt DESC")
    List<AudioClip> findTop10ByAudioAlbumIdOrderByCreatedAtDesc(@Param("audioAlbumId") Long audioAlbumId);
}
