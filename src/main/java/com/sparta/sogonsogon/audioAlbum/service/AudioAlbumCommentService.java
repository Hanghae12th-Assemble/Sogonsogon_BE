package com.sparta.sogonsogon.audioAlbum.service;

import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumCommentResponseDto;
import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbum;
import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbumComment;
import com.sparta.sogonsogon.audioAlbum.repository.AudioAlbumCommentRepository;
import com.sparta.sogonsogon.audioAlbum.repository.AudioAlbumRepository;
import com.sparta.sogonsogon.enums.ErrorMessage;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AudioAlbumCommentService {

    private final AudioAlbumCommentRepository audioAlbumCommentRepository;
    private final AudioAlbumRepository audioAlbumRepository;

    @Transactional
    public AudioAlbumCommentResponseDto createComment(Long audioAlbumId, String content, UserDetailsImpl userDetails) {
        AudioAlbum audioAlbum = audioAlbumRepository.findById(audioAlbumId).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOALBUM.getMessage())
        );
        AudioAlbumComment comment = new AudioAlbumComment(userDetails.getUser(), audioAlbum, content);
        audioAlbumCommentRepository.save(comment);
        return new AudioAlbumCommentResponseDto(comment);
    }
}
