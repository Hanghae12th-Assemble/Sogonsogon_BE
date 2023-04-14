package com.sparta.sogonsogon.audioclip.service;

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
import com.sparta.sogonsogon.member.entity.Member;
import com.sparta.sogonsogon.member.entity.MemberRoleEnum;
import com.sparta.sogonsogon.member.repository.MemberRepository;
import com.sparta.sogonsogon.noti.service.NotificationService;
import com.sparta.sogonsogon.noti.util.AlarmType;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import com.sparta.sogonsogon.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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


    //오디오 클립 생성
    @Transactional
    public StatusResponseDto<AudioClipResponseDto> createdAudioClip(Long audioablumId, AudioClipRequestDto requestDto, UserDetailsImpl userDetails) throws IOException {

        AudioAlbum audioAlbum = audioAlbumRepository.findById(audioablumId).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOALBUM.getMessage())
        );
        Member member = memberRepository.findByMembername(userDetails.getUsername()).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.WRONG_USERNAME.getMessage())
        );

        String audioclipImageUrl = "";
        String audioclipUrl = "";
        if(requestDto.getAudioclip() == null){
            audioclipUrl = "https://my-aws-bucket-image.s3.ap-northeast-2.amazonaws.com/%EA%B8%B0%EB%B3%B8%EC%9D%B4%EB%AF%B8%EC%A7%80%EB%98%90%EB%8A%94+%EC%98%A4%EB%94%94%EC%98%A4/intro.mp3";
        }else {
            audioclipUrl = s3Uploader.upload(requestDto.getAudioclip(), "audioclips");
        }
        if(requestDto.getAudioclipImage()== null){
            audioclipImageUrl = "https://my-aws-bucket-image.s3.ap-northeast-2.amazonaws.com/%EA%B8%B0%EB%B3%B8%EC%9D%B4%EB%AF%B8%EC%A7%80%EB%98%90%EB%8A%94+%EC%98%A4%EB%94%94%EC%98%A4/%EC%98%A4%EB%94%94%EC%98%A4+%ED%81%B4%EB%A6%BD+%EA%B8%B0%EB%B3%B8%EC%9D%B4%EB%AF%B8%EC%A7%80.png";
        }else {
            audioclipImageUrl = s3Uploader.upload(requestDto.getAudioclipImage(), "audioclipImage");
        }
        if(requestDto.getTitle()== null ){
            requestDto.setTitle(audioAlbum.getTitle() + "의 " + (audioAlbum.getAudioClips().size() + 1) + "번째 클립");
        } else if ( requestDto.getTitle().isBlank()) {
            requestDto.setTitle(audioAlbum.getTitle() + "의 " + (audioAlbum.getAudioClips().size() + 1) + "번째 클립");
        }
        if(requestDto.getContents().isBlank() ){
            requestDto.setContents(audioAlbum.getTitle() + "의 " + (audioAlbum.getAudioClips().size() + 1) + "번째 클립입니다. ");
        } else if (requestDto.getContents()== null) {
            requestDto.setContents(audioAlbum.getTitle() + "의 " + (audioAlbum.getAudioClips().size() + 1) + "번째 클립입니다. ");
        }


        AudioClip audioClip = new AudioClip(requestDto, member, audioclipUrl, audioclipImageUrl, audioAlbum);
        audioClipRepository.save(audioClip);

        // NotificationService를 통해 팔로우한  유저들에게 알림을 보낸다.
        List<Follow> followings = followRepository.findByFollower(userDetails.getUser());
        for (Follow following : followings) {
            String message = audioClip.getMember().getNickname() + "님이 [ " + audioClip.getTitle() + " ]오디오 클립을 생성하였습니다. ";
            notificationService.send(following.getFollowing(), AlarmType.eventAudioClipUploaded, message, audioClip.getMember().getMembername(), audioClip.getMember().getNickname(), audioClip.getMember().getProfileImageUrl());
            log.info("앨범클립 생성하였습니다. ");
        }

        return StatusResponseDto.success(HttpStatus.OK, new AudioClipResponseDto(audioClip));
    }

    //오디오 클립 수정
    @Transactional
    public StatusResponseDto<AudioClipResponseDto> updateAudioClip(Long audioclipId, AudioClipRequestDto requestDto, UserDetailsImpl userDetails) throws IOException {

        Member member = memberRepository.findById(userDetails.getUser().getId()).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.WRONG_USERNAME.getMessage())
        );
        AudioClip audioClip = audioClipRepository.findById(audioclipId).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOCLIP.getMessage())
        );

        String audioclipImageUrl = "";
        String audioclipUrl = "";
        if(requestDto.getAudioclip()== null){
            audioclipUrl = audioClip.getAudioclipUrl();
        }else {
            audioclipUrl = s3Uploader.upload(requestDto.getAudioclip(), "audioclips");
        }
        if(requestDto.getAudioclipImage()== null){
            audioclipImageUrl = audioClip.getAudioclipImageUrl();
        }else {
            audioclipImageUrl = s3Uploader.upload(requestDto.getAudioclipImage(), "audioclipImage");
        }
        if(requestDto.getTitle()== null ){
            requestDto.setTitle(audioClip.getTitle());
        } else if (requestDto.getTitle().isBlank()) {
            requestDto.setTitle(audioClip.getTitle());
        }
        if ( requestDto.getContents()== null){
            requestDto.setContents(audioClip.getContents());
        } else if (requestDto.getContents().isBlank() ) {
            requestDto.setContents(audioClip.getContents());
        }


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

                String message = audioClip.getMember().getNickname() + "님이 [ " + audioClip.getTitle() + " ]오디오 클립을 삭제하였습니다. ";
                notificationService.send(following.getFollowing(), AlarmType.eventAudioClipUploaded, message, audioClip.getMember().getMembername(), audioClip.getMember().getNickname(), audioClip.getMember().getProfileImageUrl());
            }

            return StatusResponseDto.success(HttpStatus.OK, "오디오 클립이 삭제 되었습니다. ");
        } else {
            throw new IllegalArgumentException(ErrorMessage.ACCESS_DENIED.getMessage());
        }

    }

    //오디오 클립 상세 조회
    @Transactional
    public StatusResponseDto<Map<String, Object>> detailsAudioClip(Long audioclipId, UserDetailsImpl userDetails) {
        AudioClip audioClip = audioClipRepository.findById(audioclipId).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOCLIP.getMessage())
        );

        Optional<AudioClipLike> audioClipLike = audioClipLikeRepository.findByAudioclipAndMember(audioClip, userDetails.getUser());

        boolean isLikeCheck = audioClipLike.isPresent();
        AudioClipResponseDto responseDto = new AudioClipResponseDto(audioClip, isLikeCheck);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("result", responseDto);
        responseBody.put("totalCommentCount", audioClip.getCommentList().size());
        return StatusResponseDto.success(HttpStatus.OK, responseBody);
    }


    @Transactional
    public StatusResponseDto<Map<String, Object>> getclips(int page, int size, String sortBy, Long audioAblumId, UserDetailsImpl userDetails) {
        AudioAlbum audioAlbum = audioAlbumRepository.findById(audioAblumId).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOALBUM.getMessage())
        );

        Member member = userDetails.getUser();
        Page<AudioClip> audioClipPage ;
        List<AudioClip> audioClips;
        List<AudioClipOneResponseDto> audioClipResponseDtoList = new ArrayList<>();
        size = audioAlbum.getAudioClips().size();

        for (int i = 0 ; i < audioAlbum.getAudioClips().size(); i++){
            AudioClip audioClip_sub = audioAlbum.getAudioClips().get(i);
            audioClip_sub.setOrders(i+1);
        }

        if (sortBy.equals("likesCount")) {

            Pageable sortedPageable = PageRequest.of(page, size);
            audioClipPage = audioClipRepository.findByAudioalbumOrderByAudioClipLikesDesc(audioAblumId, sortedPageable);
            audioClips = audioClipPage.getContent();

            if(audioClips.size()> 0) {
                for (int i = 0; i < audioClips.size(); i++) {
                    AudioClip audioClip = audioClips.get(i);
                    boolean islikecheck = audioClipLikeRepository.findByAudioclipAndMember(audioClip, member).isPresent();
                    audioClipResponseDtoList.add(new AudioClipOneResponseDto(audioClip, islikecheck));
                }
            }else{
                audioClipResponseDtoList = null;
            }

        }else{
            Sort sort = Sort.by(Sort.Direction.DESC, sortBy);
            Pageable sortedPageable = PageRequest.of(page, size, sort);
            audioClipPage = audioClipRepository.findAudioClipsByAudio_album_Id(audioAblumId, sortedPageable);
            audioClips = audioClipPage.getContent();

            if(audioClipPage.getTotalElements() > 0) {
                for (int i = 0; i < audioClips.size(); i++) {
                    AudioClip audioClip = audioClips.get(i);
                    boolean islikecheck = audioClipLikeRepository.findByAudioclipAndMember(audioClip, member).isPresent();
                    audioClipResponseDtoList.add(new AudioClipOneResponseDto(audioClip,islikecheck));
                }
            }else{
                audioClipResponseDtoList = null;
            }
        }
        //        // 생성된 오디오클립의 개수
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("audioClipCount", audioClipPage.getTotalElements());

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("result", audioClipResponseDtoList);
        responseBody.put("metadata", metadata);
        responseBody.put("albumTitle", audioAlbum.getTitle().toString());
        responseBody.put("isMine", userDetails.getUsername().equals(audioAlbum.getMember().getMembername()));
        return StatusResponseDto.success(HttpStatus.OK, responseBody);

    }
}
