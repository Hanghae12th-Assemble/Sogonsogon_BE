package com.sparta.sogonsogon.audioAlbum.service;

import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumIsLikeResponseDto;
import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumRequestDto;
import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumResponseDto;
import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbum;
import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbumLike;
import com.sparta.sogonsogon.audioAlbum.repository.AudioAlbumLikeRepository;
import com.sparta.sogonsogon.audioAlbum.repository.AudioAlbumRepository;
import com.sparta.sogonsogon.audioclip.dto.AudioClipOneResponseDto;
import com.sparta.sogonsogon.audioclip.dto.AudioClipResponseDto;
import com.sparta.sogonsogon.audioclip.entity.AudioClip;
import com.sparta.sogonsogon.audioclip.repository.AudioClipLikeRepository;
import com.sparta.sogonsogon.audioclip.repository.AudioClipRepository;
import com.sparta.sogonsogon.dto.StatusResponseDto;
import com.sparta.sogonsogon.enums.CategoryType;
import com.sparta.sogonsogon.enums.ErrorMessage;
import com.sparta.sogonsogon.follow.entity.Follow;
import com.sparta.sogonsogon.follow.repository.FollowRepository;
import com.sparta.sogonsogon.member.entity.Member;
import com.sparta.sogonsogon.member.repository.MemberRepository;
import com.sparta.sogonsogon.noti.service.NotificationService;
import com.sparta.sogonsogon.noti.util.AlarmType;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import com.sparta.sogonsogon.util.S3Uploader;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AudioAlbumService {

    private final MemberRepository memberRepository;
    private final AudioAlbumRepository audioAlbumRepository;
    private final AudioClipRepository audioClipRepository;
    private final AudioAlbumLikeRepository audioAlbumLikeRepository;
    private final S3Uploader s3Uploader;
    private final NotificationService notificationService;
    private final FollowRepository followRepository;
    private  final AudioClipLikeRepository audioClipLikeRepository;


    // 오디오앨범 생성
    @Transactional
    public AudioAlbumResponseDto createAudioAlbum(AudioAlbumRequestDto requestDto, UserDetailsImpl userDetails) throws IOException {
        // 유저 확인
        Member member = memberRepository.findByMembername(userDetails.getUsername()).orElseThrow(
                () -> new InsufficientAuthenticationException(ErrorMessage.ACCESS_DENIED.getMessage()) // 401 Unauthorized
        );

        // 오디오앨범 제목 중복 확인
        Optional<AudioAlbum> found = audioAlbumRepository.findByTitle(requestDto.getTitle());
        if (found.isPresent()) {
            throw new DuplicateKeyException(ErrorMessage.DUPLICATE_AUDIOALBUM_NAME.getMessage()); // 409 Conflict
        }

        // 오디오앨범 사진 추가
        String imageUrl = s3Uploader.upload(requestDto.getBackgroundImageUrl(), "audioAlbumImages");

        log.info(requestDto.getCategoryType().toString());
        AudioAlbum audioAlbum = AudioAlbum.builder()
                .title(requestDto.getTitle())
                .instruction(requestDto.getInstruction())
                .backgroundImageUrl(imageUrl)
                .categoryType(requestDto.getCategoryType())
                .member(member)
                .build();

        audioAlbumRepository.save(audioAlbum);

        // NotificationService를 통해 팔로우한  유저들에게 알림을 보낸다.
        List<Follow> followings = followRepository.findByFollower(userDetails.getUser());
        for (Follow following : followings) {
            String message = audioAlbum.getMember().getNickname() + "님이 제목:" + audioAlbum.getTitle() + "오디오 클립 앨범을 생성하였습니다. ";
            notificationService.send(following.getFollowing(), AlarmType.eventAudioClipUploaded, message, audioAlbum.getMember().getMembername(), audioAlbum.getMember().getNickname(), audioAlbum.getMember().getProfileImageUrl());
            log.info("앨범 생성하였습니다. ");
        }

        return AudioAlbumResponseDto.of(audioAlbum);
    }

    // 오디오앨범 전체 조회
    @Transactional
    public Map<String, Object> findAllAudioAlbum(int page, int size, String sortBy) {
        Page<AudioAlbum> audioAlbumPage;
        List<AudioAlbum> sortedAudioAlbums;

        if (sortBy.equals("likesCount")) {
            audioAlbumPage = audioAlbumRepository.findAll(PageRequest.of(page, size));
            sortedAudioAlbums = sortAudioAlbumsByLikesCount(audioAlbumPage);
        } else {
            Pageable sortedPageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
            audioAlbumPage = audioAlbumRepository.findAll(sortedPageable);
            sortedAudioAlbums = audioAlbumPage.getContent();
        }

        List<AudioAlbumResponseDto> audioAlbumResponseDtoList = sortedAudioAlbums
                .stream()
                .peek(audioAlbum -> {
                    int likesCount = audioAlbum.getAudioAlbumLikes().size();
                    audioAlbum.updateLikesCnt(likesCount);
                })
                .map(AudioAlbumResponseDto::new)
                .toList();

        // 생성된 오디오앨범의 개수
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("audioAlbumCount", audioAlbumPage.getTotalElements());

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("result", audioAlbumResponseDtoList);
        responseBody.put("metadata", metadata);

        return responseBody;
    }

    private List<AudioAlbum> sortAudioAlbumsByLikesCount(Page<AudioAlbum> audioAlbums) {
        return audioAlbums.stream()
                .sorted(Comparator.comparingInt((AudioAlbum a) -> a.getAudioAlbumLikes().size()).reversed())
                .collect(Collectors.toList());
    }

    // 선택한 오디오앨범 조회
    @Transactional
    public Map<String, Object> findAudioAlbum(Long audioAlbumId, UserDetailsImpl userDetails) {
        AudioAlbum audioAlbum = audioAlbumRepository.findById(audioAlbumId).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOALBUM.getMessage())
        );
        List<AudioClip> foundAudioClip = audioClipRepository.findTop10ByAudioAlbumIdOrderByCreatedAtDesc(audioAlbumId)
                .stream()
                .limit(10)
                .toList();
        List<AudioClipOneResponseDto> audioAlbumResponseDtos = new ArrayList<>();
        int index = audioAlbum.getAudioClips().size();
        if(!foundAudioClip.isEmpty()){
            for(AudioClip audioClip : foundAudioClip){
                boolean isLikeCheck = audioClipLikeRepository.findByAudioclipAndMember(audioClip, userDetails.getUser()).isPresent();
                audioAlbumResponseDtos.add(new AudioClipOneResponseDto(audioClip, isLikeCheck));
            }
        } else {
            audioAlbumResponseDtos = null;
        }
        boolean isLikeCheck = audioAlbumLikeRepository.findByAudioAlbumAndMember(audioAlbum, userDetails.getUser()).isPresent();
        boolean isMine = audioAlbum.getMember().getId().equals(userDetails.getUser().getId());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("audioClipCount", foundAudioClip.size());
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("metadata", metadata);
        responseBody.put("result", new AudioAlbumResponseDto(audioAlbum, audioAlbumResponseDtos, isLikeCheck, isMine));

        return responseBody;
    }

    // 선택한 오디오앨범 삭제
    @Transactional
    public void deleteAudioAlbum(Long audioAlbumId, UserDetailsImpl userDetails) {

        // 삭제할 오디오앨범이 있는지 확인
        AudioAlbum audioAlbum = audioAlbumRepository.findById(audioAlbumId).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOALBUM.getMessage())
        );

        Member member = userDetails.getUser();

        // 오디오앨범 삭제를 요청한 유저가 해당 오디오앨범의 생성자인지 확인
        if (!member.getId().equals(audioAlbum.getMember().getId())) {
            throw new IllegalArgumentException(ErrorMessage.ACCESS_DENIED.getMessage());
        }

        audioAlbumRepository.deleteById(audioAlbumId);

        //  앨범 삭제하면 알림 보내기
        String message = audioAlbum.getMember().getNickname() + "님이 제목:" + audioAlbum.getTitle() + "오디오 클립 앨범이 삭제되었습니다.  ";
        notificationService.send(audioAlbum.getMember(), AlarmType.eventAudioClipUploaded, message, audioAlbum.getMember().getMembername(), audioAlbum.getMember().getNickname(), audioAlbum.getMember().getProfileImageUrl());

    }

    // 카테고리별 오디오앨범 조회
    @Transactional
    public Map<String, Object> findByCategory(int page, int size, String sortBy, CategoryType categoryType) {
        Sort sort = Sort.by(Sort.Direction.DESC, sortBy);
        Pageable sortedPageable = PageRequest.of(page, size, sort);
        Page<AudioAlbum> audioAlbumPage = audioAlbumRepository.findAllByCategoryType(categoryType, sortedPageable);
        List<AudioAlbumResponseDto> audioAlbumResponseDtoList = audioAlbumPage.getContent().stream().map(AudioAlbumResponseDto::new).toList();

        // 생성된 오디오앨범의 개수
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("audioAlbumCount", audioAlbumPage.getTotalElements());

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("result", audioAlbumResponseDtoList);
        responseBody.put("metadata", metadata);

        return responseBody;
    }

    // 오디오앨범 수정
    @Transactional
    public AudioAlbumResponseDto updateAudioAlbum(Long audioAlbumId, AudioAlbumRequestDto requestDto, UserDetailsImpl userDetails) throws IOException {

        // 수정할 오디오앨범이 있는지 확인
        AudioAlbum audioAlbum = audioAlbumRepository.findById(audioAlbumId).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOALBUM.getMessage())
        );

        // 변경될 오디오앨범 제목 중복 확인
        Optional<AudioAlbum> found = audioAlbumRepository.findByTitle(requestDto.getTitle());
        if (found.isPresent()) {
            throw new DuplicateKeyException(ErrorMessage.DUPLICATE_AUDIOALBUM_NAME.getMessage()); // 409 Conflict
        }

        Member member = userDetails.getUser();

        // 오디오 앨범 수정을 요청한 유저가 해당 오디오 앨범 생성자인지 확인
        if (!member.getId().equals(audioAlbum.getMember().getId())) {
            throw new IllegalArgumentException(ErrorMessage.ACCESS_DENIED.getMessage());
        }

        // 오디오앨범 사진 추가
        String imageUrl = s3Uploader.upload(requestDto.getBackgroundImageUrl(), "audioAlbumImages");

        audioAlbum.update(requestDto, imageUrl);

        return AudioAlbumResponseDto.of(audioAlbum);
    }

    @Transactional
    public StatusResponseDto<AudioAlbumIsLikeResponseDto> likeAudioAlbum(Long audioAlbumId, UserDetailsImpl userDetails) {
        AudioAlbum audioAlbum = audioAlbumRepository.findById(audioAlbumId).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOALBUM.getMessage())
        );

        Optional<AudioAlbumLike> audioAlbumLike = audioAlbumLikeRepository.findByAudioAlbumAndMember(audioAlbum, userDetails.getUser());
        if (audioAlbumLike.isPresent()) {
            audioAlbumLikeRepository.deleteById(audioAlbumLike.get().getId());
            audioAlbum.updateLikesCnt(audioAlbum.getLikesCount() - 1);
            return StatusResponseDto.success(HttpStatus.OK, new AudioAlbumIsLikeResponseDto("해당 오디오앨범 좋아요가 취소 되었습니다.", false));
        }

        audioAlbumLikeRepository.save(new AudioAlbumLike(audioAlbum, userDetails.getUser()));
        audioAlbum.updateLikesCnt(audioAlbum.getLikesCount() + 1);

        // 좋아요 클릭하면 앨범생성자 한테 알림 보내기
        String message = audioAlbum.getMember().getNickname() + "님이 제목:" + audioAlbum.getTitle() + "오디오 클립 앨범에 좋아요가 추가 되었습니다. ";
        notificationService.send(audioAlbum.getMember(), AlarmType.eventAudioClipUploaded, message, audioAlbum.getMember().getMembername(), audioAlbum.getMember().getNickname(), audioAlbum.getMember().getProfileImageUrl());

        return StatusResponseDto.success(HttpStatus.OK, new AudioAlbumIsLikeResponseDto("해당 오디오앨범 좋아요가 추가 되었습니다.", true));
    }


    @Transactional
    public StatusResponseDto<Map<String, Object>> getMine(String sortBy, int page, int size, UserDetailsImpl userDetails) {
        Member member = memberRepository.findById(userDetails.getUser().getId()).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.NOT_FOUND_MEMBER.getMessage())
        );

        Sort sort = Sort.by(Sort.Direction.DESC, sortBy);
        Pageable sortedPageable = PageRequest.of(page, size, sort);
        Page<AudioAlbum> audioAlbumPage = audioAlbumRepository.findByMember(member, sortedPageable);
        List<AudioAlbumResponseDto> audioAlbumResponseDtoList = audioAlbumPage.getContent()
                .stream()
                .map(AudioAlbumResponseDto::new)
                .toList();

        // 생성된 오디오앨범의 개수
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("audioAlbumCount", audioAlbumPage.getTotalElements());

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("result", audioAlbumResponseDtoList);
        responseBody.put("metadata", metadata);

        return StatusResponseDto.success(HttpStatus.OK, responseBody);

    }

    // 오디오앨범 검색
    @Transactional
    public List<AudioAlbumResponseDto> findByTitle(String title) {
        List<AudioAlbum> list = audioAlbumRepository.findByTitleContaining(title);
        List<AudioAlbumResponseDto> audioAlbumResponseDtoList = new ArrayList<>();
        for (AudioAlbum audioAlbum : list) {
            audioAlbumResponseDtoList.add(new AudioAlbumResponseDto(audioAlbum));
        }
        return audioAlbumResponseDtoList; // test
    }
}
