package com.sparta.sogonsogon.audioclip.service;

import com.sparta.sogonsogon.audioclip.dto.AudioClipRequestDto;
import com.sparta.sogonsogon.audioclip.dto.AudioClipResponseDto;
import com.sparta.sogonsogon.audioclip.entity.AudioClip;
import com.sparta.sogonsogon.audioclip.like.entity.AudioClipLike;
import com.sparta.sogonsogon.audioclip.like.repository.AudioClipLikeRepository;
import com.sparta.sogonsogon.audioclip.repository.AudioClipRepository;
import com.sparta.sogonsogon.dto.StatusResponseDto;
import com.sparta.sogonsogon.enums.ErrorMessage;
import com.sparta.sogonsogon.follow.entity.Follow;
import com.sparta.sogonsogon.follow.repository.FollowRepository;
import com.sparta.sogonsogon.member.dto.MemberResponseDto;
import com.sparta.sogonsogon.member.entity.Member;
import com.sparta.sogonsogon.member.entity.MemberRoleEnum;
import com.sparta.sogonsogon.member.repository.MemberRepository;
import com.sparta.sogonsogon.noti.service.NotificationService;
import com.sparta.sogonsogon.noti.util.AlarmType;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import com.sparta.sogonsogon.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AudioClipService {

    private final MemberRepository memberRepository;
    private final AudioClipRepository audioClipRepository;
    private final AudioClipLikeRepository audioClipLikeRepository;

    private final NotificationService notificationService;
    private final FollowRepository followRepository;

    private final S3Uploader s3Uploader;


    //오디오 클립 생성
    @Transactional
    public StatusResponseDto<AudioClipResponseDto> createdAudioClip(AudioClipRequestDto requestDto, UserDetailsImpl userDetails) throws IOException {
        String audioclipImageUrl = s3Uploader.uploadFiles(requestDto.getAudioclipImage(), "audioclipImage/");
        String audioclipUrl = s3Uploader.uploadFiles(requestDto.getAudioclip(), "audioclips/");
        Member member = memberRepository.findByMembername(userDetails.getUsername()).orElseThrow(
                ()-> new IllegalArgumentException(ErrorMessage.WRONG_USERNAME.getMessage())
        );

        AudioClip audioClip = new AudioClip(requestDto, member, audioclipUrl, audioclipImageUrl);
        audioClipRepository.save(audioClip);

        // 본인이 알림 구독을 하였는지 확인
        if (member.getIsSubscribed() == true) {
            notificationService.send(member, AlarmType.eventAudioClipUploaded, "제목: " + audioClip.getTitle() + "오디오 클립이 생성되었습니다. ", null, null, null);
        }

        // NotificationService를 통해 알림을 구독한 유저들에게 알림을 보낸다.
        List<Follow> followings = followRepository.findByFollower(userDetails.getUser());
        for (Follow following : followings) {
            if (following.getFollowing().getIsSubscribed() == true){
                String message = audioClip.getMember().getNickname() +"님이 제목:" + audioClip.getTitle() + "오디오 클립을 생성하였습니다. ";
                notificationService.send(following.getFollowing(), AlarmType.eventAudioClipUploaded, message, audioClip.getMember().getMembername(), audioClip.getMember().getNickname(), audioClip.getMember().getProfileImageUrl());
            }
        }

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


            // NotificationService를 통해 알림을 구독한 유저들에게 알림을 보낸다.
            List<Follow> followings = followRepository.findByFollower(userDetails.getUser());
            for (Follow following : followings) {
                if (following.getFollower().getIsSubscribed() == true){
                    String message = audioClip.getMember().getNickname() +"님이 제목:" + audioClip.getTitle() + "오디오 클립을 삭제하였습니다. ";
                    notificationService.send(following.getFollowing(), AlarmType.eventAudioClipUploaded, message, audioClip.getMember().getMembername(), audioClip.getMember().getNickname(), audioClip.getMember().getProfileImageUrl());
                }
            }

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
