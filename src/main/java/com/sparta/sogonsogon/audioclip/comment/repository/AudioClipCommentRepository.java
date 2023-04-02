package com.sparta.sogonsogon.audioclip.comment.repository;

import com.sparta.sogonsogon.audioclip.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AudioClipCommentRepository extends JpaRepository<Comment , Long> {
    List<Comment> findAllByAudioclipId(Long audioclipId);
}
