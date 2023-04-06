package com.sparta.sogonsogon.audioclip.repository;

import com.sparta.sogonsogon.audioclip.entity.AudioClipComment;
import com.sparta.sogonsogon.audioclip.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AudioClipCommentRepository extends JpaRepository<AudioClipComment , Long> {
    List<AudioClipComment> findAllByAudioclipId(Long audioclipId);
}
