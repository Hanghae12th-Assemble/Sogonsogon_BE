package com.sparta.sogonsogon.audioAlbum.repository;

import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbum;
import com.sparta.sogonsogon.enums.CategoryType;
import com.sparta.sogonsogon.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AudioAlbumRepository extends JpaRepository<AudioAlbum, Long> {

    Optional<AudioAlbum> findByTitle(String title);

    Page<AudioAlbum> findAllByCategoryType(CategoryType categoryType, Pageable sortedPageable);

    Page<AudioAlbum> findByMember(Member member, Pageable sortedPageable);
}
