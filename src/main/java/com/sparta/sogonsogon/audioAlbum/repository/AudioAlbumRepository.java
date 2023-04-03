package com.sparta.sogonsogon.audioAlbum.repository;

import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AudioAlbumRepository extends JpaRepository<AudioAlbum, Long> {

    Optional<AudioAlbum> findByTitle(String title);
}
