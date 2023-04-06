package com.sparta.sogonsogon.audioclip.service;

import com.sparta.sogonsogon.audioclip.entity.AudioClip;
import com.sparta.sogonsogon.audioclip.dto.AudioClipIsLikeResponseDto;
import com.sparta.sogonsogon.audioclip.entity.AudioClipLike;
import com.sparta.sogonsogon.audioclip.repository.AudioClipLikeRepository;
import com.sparta.sogonsogon.audioclip.repository.AudioClipRepository;
import com.sparta.sogonsogon.dto.StatusResponseDto;
import com.sparta.sogonsogon.enums.ErrorMessage;
import com.sparta.sogonsogon.noti.service.NotificationService;
import com.sparta.sogonsogon.noti.util.AlarmType;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AudioClipLikeService {

    private final AudioClipRepository audioClipRepository;
    private final AudioClipLikeRepository audioClipLikeRepository;
    private final NotificationService notificationService;

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

        // 좋아요를 눌렀을때 오디오 클립 생성자에게 알림 보내기
        String message = userDetails.getUser().getNickname() + " 님이 제목: " + audioClip.getTitle() + " 오디오의 좋아요를 눌렀습니다. ";
        notificationService.send(audioClip.getMember(), AlarmType.eventAudioClipLike, message, userDetails.getUsername(), userDetails.getUser().getNickname(), userDetails.getUser().getProfileImageUrl());
        log.info("좋아요 클릭했지");
        audioClipLikeRepository.save(new AudioClipLike(audioClip, userDetails.getUser()));
        return StatusResponseDto.success(HttpStatus.OK, new AudioClipIsLikeResponseDto("해당 오디오 클립에 좋아요가 추가 되었습니다.", true));
    }


}


