package com.sparta.sogonsogon.audioclip.service;

import com.sparta.sogonsogon.audioclip.dto.AudioClipRequestDto;
import com.sparta.sogonsogon.audioclip.dto.AudioClipResponseDto;
import com.sparta.sogonsogon.audioclip.entity.AudioClip;
import com.sparta.sogonsogon.audioclip.repository.AudioClipRepository;
import com.sparta.sogonsogon.dto.StatusResponseDto;
import com.sparta.sogonsogon.enums.ErrorMessage;
import com.sparta.sogonsogon.member.entity.Member;
import com.sparta.sogonsogon.member.repository.MemberRepository;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import com.sparta.sogonsogon.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AudioClipService {

    private final MemberRepository memberRepository;
    private final AudioClipRepository audioClipRepository;

    @Transactional
    public StatusResponseDto<AudioClipResponseDto> createdAudioClip(AudioClipRequestDto requestDto, UserDetailsImpl userDetails){
        Member member = memberRepository.findByMembername(userDetails.getUsername()).orElseThrow(
                ()-> new IllegalArgumentException(ErrorMessage.WRONG_USERNAME.getMessage())
        );

        AudioClip audioClip = new AudioClip(requestDto, member);
        audioClipRepository.save(audioClip);
        return StatusResponseDto.success(HttpStatus.OK, new AudioClipResponseDto(audioClip));
    }



}
