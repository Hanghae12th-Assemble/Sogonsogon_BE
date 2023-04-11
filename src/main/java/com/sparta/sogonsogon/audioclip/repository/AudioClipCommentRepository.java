package com.sparta.sogonsogon.audioclip.repository;

import com.sparta.sogonsogon.audioclip.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AudioClipCommentRepository extends JpaRepository<Comment , Long> {
    Page<Comment> findAllByAudioclipId(Long audioclipId, Pageable sortedPageable);
}
