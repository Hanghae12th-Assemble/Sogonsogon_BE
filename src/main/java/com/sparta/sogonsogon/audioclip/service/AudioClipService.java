package com.sparta.sogonsogon.audioclip.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbum;
import com.sparta.sogonsogon.audioAlbum.repository.AudioAlbumRepository;
import com.sparta.sogonsogon.audioclip.dto.AudioClipOneResponseDto;
import com.sparta.sogonsogon.audioclip.dto.AudioClipRequestDto;
import com.sparta.sogonsogon.audioclip.dto.AudioClipResponseDto;
import com.sparta.sogonsogon.audioclip.entity.AudioClip;
import com.sparta.sogonsogon.audioclip.entity.AudioClipLike;
import com.sparta.sogonsogon.audioclip.repository.AudioClipLikeRepository;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.persistence.EntityManager;
import java.util.*;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AudioClipService {

    private final MemberRepository memberRepository;
    private final AudioAlbumRepository audioAlbumRepository;
    private final AudioClipRepository audioClipRepository;
    private final AudioClipLikeRepository audioClipLikeRepository;

    private final NotificationService notificationService;
    private final FollowRepository followRepository;

    private final S3Uploader s3Uploader;

    private final EntityManager entityManager;

    //오디오 클립 생성
    @Transactional
    public StatusResponseDto<AudioClipResponseDto> createdAudioClip(Long audioablumId, AudioClipRequestDto requestDto, UserDetailsImpl userDetails) throws IOException {
        String audioclipImageUrl = s3Uploader.upload(requestDto.getAudioclipImage(), "audioclipImage/");
        String audioclipUrl = s3Uploader.upload(requestDto.getAudioclip(), "audioclips/");
        AudioAlbum audioAlbum = audioAlbumRepository.findById(audioablumId).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOALBUM.getMessage())
        );
        Member member = memberRepository.findByMembername(userDetails.getUsername()).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.WRONG_USERNAME.getMessage())
        );

        AudioClip audioClip = new AudioClip(requestDto, member, audioclipUrl, audioclipImageUrl, audioAlbum);
        audioClipRepository.save(audioClip);

        // 본인이 알림 구독을 하였는지 확인
        notificationService.send(member, AlarmType.eventAudioClipUploaded, "제목: " + audioClip.getTitle() + "오디오 클립이 생성되었습니다. ", null, null, null);

        // NotificationService를 통해 알림을 구독한 유저들에게 알림을 보낸다.
        List<Follow> followings = followRepository.findByFollower(userDetails.getUser());
        for (Follow following : followings) {
            String message = audioClip.getMember().getNickname() + "님이 제목:" + audioClip.getTitle() + "오디오 클립을 생성하였습니다. ";
            notificationService.send(following.getFollowing(), AlarmType.eventAudioClipUploaded, message, audioClip.getMember().getMembername(), audioClip.getMember().getNickname(), audioClip.getMember().getProfileImageUrl());
            log.info("생성하였습니다. ");
        }

        return StatusResponseDto.success(HttpStatus.OK, new AudioClipResponseDto(audioClip));
    }

    //오디오 클립 수정
    @Transactional
    public StatusResponseDto<AudioClipResponseDto> updateAudioClip(Long audioclipId, AudioClipRequestDto requestDto, UserDetailsImpl userDetails) throws IOException {
        String audioclipImageUrl = s3Uploader.upload(requestDto.getAudioclipImage(), "audioclipImage/");
        String audioclipUrl = s3Uploader.upload(requestDto.getAudioclip(), "audioclips/");
        Member member = memberRepository.findById(userDetails.getUser().getId()).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.WRONG_USERNAME.getMessage())
        );
        AudioClip audioClip = audioClipRepository.findById(audioclipId).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOCLIP.getMessage())
        );

        if (member.getRole() == MemberRoleEnum.USER || member.getMembername().equals(userDetails.getUser().getMembername())) {
            audioClip.update(requestDto, audioclipUrl, audioclipImageUrl);
            return StatusResponseDto.success(HttpStatus.OK, new AudioClipResponseDto(audioClip));
        } else {
            throw new IllegalArgumentException(ErrorMessage.ACCESS_DENIED.getMessage());
        }
    }

    //오디오 클립 삭제
    @Transactional
    public StatusResponseDto<String> deleteAudioClip(Long audioclipId, UserDetailsImpl userDetails) {
        Member member = userDetails.getUser();
        AudioClip audioClip = audioClipRepository.findById(audioclipId).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOCLIP.getMessage())
        );

        if (member.getRole() == MemberRoleEnum.USER || member.getMembername().equals(userDetails.getUser().getMembername())) {
            audioClipRepository.deleteById(audioclipId);

            // NotificationService를 통해 알림을 구독한 유저들에게 알림을 보낸다.
            List<Follow> followings = followRepository.findByFollower(userDetails.getUser());
            for (Follow following : followings) {

                String message = audioClip.getMember().getNickname() + "님이 제목:" + audioClip.getTitle() + "오디오 클립을 삭제하였습니다. ";
                notificationService.send(following.getFollowing(), AlarmType.eventAudioClipUploaded, message, audioClip.getMember().getMembername(), audioClip.getMember().getNickname(), audioClip.getMember().getProfileImageUrl());
            }

            return StatusResponseDto.success(HttpStatus.OK, "오디오 클립이 삭제 되었습니다. ");
        } else {
            throw new IllegalArgumentException(ErrorMessage.ACCESS_DENIED.getMessage());
        }

    }

    //오디오 클립 상세 조회
    @Transactional
    public StatusResponseDto<AudioClipResponseDto> detailsAudioClip(Long audioclipId, UserDetailsImpl userDetails) {
        AudioClip audioClip = audioClipRepository.findById(audioclipId).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOCLIP.getMessage())
        );

        Optional<AudioClipLike> audioClipLike = audioClipLikeRepository.findByAudioclipAndMember(audioClip, userDetails.getUser());

        boolean isLikeCheck = audioClipLike.isPresent();
        AudioClipResponseDto responseDto = new AudioClipResponseDto(audioClip, isLikeCheck);
        return StatusResponseDto.success(HttpStatus.OK, responseDto);
    }

    @Transactional
    public StatusResponseDto<Map<String, Object>> findAllinAblumOrderbyLike(int page, int size, String SortBy, Long audioAblumId, UserDetailsImpl userDetails){
        AudioAlbum audioAlbum = audioAlbumRepository.findById(audioAblumId).orElseThrow(
                ()-> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOALBUM.getMessage())
        );

        Sort sort = Sort.by(Sort.Direction.ASC, SortBy);
        Pageable sortedPageable = PageRequest.of(page, size, sort);
        Page<AudioClip> audioClipPage = audioClipRepository.findAudioClipsByAudioalbum(audioAlbum, sortedPageable);
        List<AudioClipOneResponseDto> audioClipResponseDtoList = new ArrayList<>();

        int index = 1;
        for (int i = 0; i < audioClipPage.getTotalElements(); i++){
            AudioClip audioClip = audioClipPage.getContent().get(i);
            boolean islikecheck = audioClipLikeRepository.findByAudioclipAndMember(audioClip, userDetails.getUser()).isPresent();
            audioClipResponseDtoList.add(new AudioClipOneResponseDto(audioClip, index, islikecheck));
            index += 1;
        }

        audioClipResponseDtoList.sort(new Comparator<AudioClipOneResponseDto>() {
            @Override
            public int compare(AudioClipOneResponseDto o1, AudioClipOneResponseDto o2) {
                return Integer.compare(o2.getIsLikeCount(), o1.getIsLikeCount());
            }
        });

        // 생성된 오디오클립의 개수
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("audioClipCount", audioClipPage.getTotalElements());

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("result", audioClipResponseDtoList);
        responseBody.put("metadata", metadata);
        responseBody.put("is Mine", userDetails.getUsername().equals(audioAlbum.getMember().getMembername()));
        return StatusResponseDto.success(HttpStatus.OK, responseBody);
    }


    @Transactional
    public StatusResponseDto<Map<String, Object>> getclips(int page, int size, String sortBy, Long audioAblumId, UserDetailsImpl userDetails) {
        AudioAlbum audioAlbum = audioAlbumRepository.findById(audioAblumId).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOALBUM.getMessage())
        );

        Sort sort = Sort.by(Sort.Direction.DESC, sortBy);
        Pageable sortedPageable = PageRequest.of(page, size, sort);
        Page<AudioClip> audioClipPage = audioClipRepository.findAudioClipsByAudio_album_Id(audioAblumId, sortedPageable);
        List<AudioClipOneResponseDto> audioClipResponseDtoList = new ArrayList<>();

        int index = 1;
        for (int i = 0; i < audioClipPage.getTotalElements(); i++){
            AudioClip audioClip = audioClipPage.getContent().get(i);
            boolean islikecheck = audioClipLikeRepository.findByAudioclipAndMember(audioClip, userDetails.getUser()).isPresent();
            audioClipResponseDtoList.add(new AudioClipOneResponseDto(audioClip, index, islikecheck));
            index += 1;
        }

        //        // 생성된 오디오클립의 개수
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("audioClipCount", audioClipPage.getTotalElements());

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("result", audioClipResponseDtoList);
        responseBody.put("metadata", metadata);
        responseBody.put("is Mine", userDetails.getUsername().equals(audioAlbum.getMember().getMembername()));
        return StatusResponseDto.success(HttpStatus.OK, responseBody);

    }
}
