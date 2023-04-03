package com.sparta.sogonsogon.audioclip.like.service;

import com.sparta.sogonsogon.audioclip.entity.AudioClip;
import com.sparta.sogonsogon.audioclip.like.dto.AudioClipIsLikeResponseDto;
import com.sparta.sogonsogon.audioclip.like.entity.AudioClipLike;
import com.sparta.sogonsogon.audioclip.like.repository.AudioClipLikeRepository;
import com.sparta.sogonsogon.audioclip.repository.AudioClipRepository;
import com.sparta.sogonsogon.dto.StatusResponseDto;
import com.sparta.sogonsogon.enums.ErrorMessage;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AudioClipLikeService {

    private final AudioClipRepository audioClipRepository;
    private final AudioClipLikeRepository audioClipLikeRepository;

    //오디오 클립 좋아요 기능
    @Transactional
    public StatusResponseDto<AudioClipIsLikeResponseDto> likeAudioClip(Long audioclipId, UserDetailsImpl userDetails){
        AudioClip audioClip = audioClipRepository.findById(audioclipId).orElseThrow(
                ()-> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOCLIP.getMessage())
        );

        Optional<AudioClipLike> audioClipLike = audioClipLikeRepository.findByAudioclipAndMember(audioClip, userDetails.getUser());
        if(audioClipLike.isPresent()){
            audioClipLikeRepository.deleteById(audioClipLike.get().getId());
            return StatusResponseDto.success(HttpStatus.OK, new AudioClipIsLikeResponseDto("해당 오디오 클립에 좋아요가 취소 되었습니다. ", false));
        }

        audioClipLikeRepository.save(new AudioClipLike(audioClip, userDetails.getUser()));
        return StatusResponseDto.success(HttpStatus.OK, new AudioClipIsLikeResponseDto("해당 오디오 클립에 좋아요가 추가 되었습니다.", true));
    }


}


