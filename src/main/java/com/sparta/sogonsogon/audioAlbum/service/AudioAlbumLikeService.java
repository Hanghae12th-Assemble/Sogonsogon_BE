package com.sparta.sogonsogon.audioAlbum.service;

import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumIsLikeResponseDto;
import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumResponseDto;
import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbum;
import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbumLike;
import com.sparta.sogonsogon.audioAlbum.repository.AudioAlbumLikeRepository;
import com.sparta.sogonsogon.audioAlbum.repository.AudioAlbumRepository;
import com.sparta.sogonsogon.dto.StatusResponseDto;
import com.sparta.sogonsogon.enums.ErrorMessage;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AudioAlbumLikeService {

    private final AudioAlbumRepository audioAlbumRepository;
    private final AudioAlbumLikeRepository audioAlbumLikeRepository;

    @Transactional
    public StatusResponseDto<AudioAlbumIsLikeResponseDto> likeAudioAlbum(Long audioAlbumId, UserDetailsImpl userDetails) {
        AudioAlbum audioAlbum = audioAlbumRepository.findById(audioAlbumId).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOALBUM.getMessage())
        );

        Optional<AudioAlbumLike> audioAlbumLike = audioAlbumLikeRepository.findByAudioAlbumAndMember(audioAlbum, userDetails.getUser());
        if (audioAlbumLike.isPresent()) {
            audioAlbumLikeRepository.deleteById(audioAlbumLike.get().getId());
            return StatusResponseDto.success(HttpStatus.OK, new AudioAlbumIsLikeResponseDto("해당 오디오앨범 좋아요가 취소 되었습니다.", false));
        }

        audioAlbumLikeRepository.save(new AudioAlbumLike(audioAlbum, userDetails.getUser()));
        return StatusResponseDto.success(HttpStatus.OK, new AudioAlbumIsLikeResponseDto("해당 오디오앨범 좋아요가 추가 되었습니다.", true));
    }

}
