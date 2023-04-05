package com.sparta.sogonsogon.audioclip.service;

import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbum;
import com.sparta.sogonsogon.audioAlbum.repository.AudioAlbumRepository;
import com.sparta.sogonsogon.audioclip.dto.AudioClipRequestDto;
import com.sparta.sogonsogon.audioclip.dto.AudioClipResponseDto;
import com.sparta.sogonsogon.audioclip.entity.AudioClip;
import com.sparta.sogonsogon.audioclip.entity.AudioClipLike;
import com.sparta.sogonsogon.audioclip.repository.AudioClipLikeRepository;
import com.sparta.sogonsogon.audioclip.repository.AudioClipRepository;
import com.sparta.sogonsogon.dto.StatusResponseDto;
import com.sparta.sogonsogon.enums.ErrorMessage;
import com.sparta.sogonsogon.member.entity.Member;
import com.sparta.sogonsogon.member.entity.MemberRoleEnum;
import com.sparta.sogonsogon.member.repository.MemberRepository;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import com.sparta.sogonsogon.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AudioClipService {

    private final MemberRepository memberRepository;
    private final AudioAlbumRepository audioAlbumRepository;
    private final AudioClipRepository audioClipRepository;
    private final AudioClipLikeRepository audioClipLikeRepository;
    private final S3Uploader s3Uploader;

    //오디오 클립 생성
    @Transactional
    public StatusResponseDto<AudioClipResponseDto> createdAudioClip(Long audioablumId, AudioClipRequestDto requestDto, UserDetailsImpl userDetails) throws IOException {
        String audioclipImageUrl = s3Uploader.uploadFiles(requestDto.getAudioclipImage(), "audioclipImage/");
        String audioclipUrl = s3Uploader.uploadFiles(requestDto.getAudioclip(), "audioclips/");
        AudioAlbum audioAlbum = audioAlbumRepository.findById(audioablumId).orElseThrow(
                ()-> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOALBUM.getMessage())
        );
        Member member = memberRepository.findByMembername(userDetails.getUsername()).orElseThrow(
                ()-> new IllegalArgumentException(ErrorMessage.WRONG_USERNAME.getMessage())
        );

        AudioClip audioClip = new AudioClip(requestDto, member, audioclipUrl, audioclipImageUrl, audioAlbum);
        audioClipRepository.save(audioClip);
        return StatusResponseDto.success(HttpStatus.OK, new AudioClipResponseDto(audioClip));
    }

    //오디오 클립 수정
    @Transactional
    public StatusResponseDto<AudioClipResponseDto> updateAudioClip(Long audioclipId, AudioClipRequestDto requestDto, UserDetailsImpl userDetails) throws IOException {
        String audioclipImageUrl = s3Uploader.uploadFiles(requestDto.getAudioclipImage(), "audioclipImage/");
        String audioclipUrl = s3Uploader.uploadFiles(requestDto.getAudioclip(), "audioclips/");
        Member member = memberRepository.findById(userDetails.getUser().getId()).orElseThrow(
                ()-> new IllegalArgumentException(ErrorMessage.WRONG_USERNAME.getMessage())
        );
        AudioClip audioClip = audioClipRepository.findById(audioclipId).orElseThrow(
                ()-> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOCLIP.getMessage())
        );

        if (member.getRole() == MemberRoleEnum.USER  || member.getMembername().equals(userDetails.getUser().getMembername())) {
            audioClip.update(requestDto, audioclipUrl, audioclipImageUrl);
            return StatusResponseDto.success(HttpStatus.OK, new AudioClipResponseDto(audioClip));
        } else {
            throw new IllegalArgumentException(ErrorMessage.ACCESS_DENIED.getMessage());
        }
    }

    //오디오 클립 삭제
    @Transactional
    public StatusResponseDto<String> deleteAudioClip(Long audioclipId, UserDetailsImpl userDetails){
        Member member = userDetails.getUser();
        AudioClip audioClip = audioClipRepository.findById(audioclipId).orElseThrow(
                ()-> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOCLIP.getMessage())
        );

        if (member.getRole() == MemberRoleEnum.USER  || member.getMembername().equals(userDetails.getUser().getMembername())) {
            audioClipRepository.deleteById(audioclipId);
            return StatusResponseDto.success(HttpStatus.OK, "오디오 클립이 삭제 되었습니다. ");
        } else {
            throw new IllegalArgumentException(ErrorMessage.ACCESS_DENIED.getMessage());
        }

    }

    //오디오 클립 상세 조회
    public StatusResponseDto<AudioClipResponseDto> detailsAudioClip(Long audioclipId, UserDetailsImpl userDetails){
        AudioClip audioClip = audioClipRepository.findById(audioclipId).orElseThrow(
                ()-> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOCLIP.getMessage())
        );

        Optional<AudioClipLike> audioClipLike = audioClipLikeRepository.findByAudioclipAndMember(audioClip, userDetails.getUser());

        boolean isLikeCheck = audioClipLike.isPresent();
        AudioClipResponseDto responseDto = new AudioClipResponseDto(audioClip, isLikeCheck);
        return StatusResponseDto.success(HttpStatus.OK, responseDto);
    }


}
