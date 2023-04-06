package com.sparta.sogonsogon.audioAlbum.service;

import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumIsLikeResponseDto;
import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumRequestDto;
import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumResponseDto;
import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbum;
import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbumLike;
import com.sparta.sogonsogon.audioAlbum.repository.AudioAlbumLikeRepository;
import com.sparta.sogonsogon.audioAlbum.repository.AudioAlbumRepository;
import com.sparta.sogonsogon.audioclip.dto.AudioClipResponseDto;
import com.sparta.sogonsogon.audioclip.entity.AudioClip;
import com.sparta.sogonsogon.audioclip.repository.AudioClipRepository;
import com.sparta.sogonsogon.dto.StatusResponseDto;
import com.sparta.sogonsogon.enums.CategoryType;
import com.sparta.sogonsogon.enums.ErrorMessage;
import com.sparta.sogonsogon.member.entity.Member;
import com.sparta.sogonsogon.member.repository.MemberRepository;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import com.sparta.sogonsogon.util.S3Uploader;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class AudioAlbumService {

    private final MemberRepository memberRepository;
    private final AudioAlbumRepository audioAlbumRepository;
    private final AudioClipRepository audioClipRepository;
    private final AudioAlbumLikeRepository audioAlbumLikeRepository;
    private final S3Uploader s3Uploader;

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
        String imageUrl = s3Uploader.uploadFiles(requestDto.getBackgroundImageUrl(), "audioAlbumImages");

        log.info(requestDto.getCategoryType().toString());
        AudioAlbum audioAlbum = AudioAlbum.builder()
                .title(requestDto.getTitle())
                .instruction(requestDto.getInstruction())
                .backgroundImageUrl(imageUrl)
                .categoryType(requestDto.getCategoryType())
                .member(member)
                .build();

        audioAlbumRepository.save(audioAlbum);
        return AudioAlbumResponseDto.of(audioAlbum);
    }

    // 오디오앨범 전체 조회
    @Transactional
    public Map<String, Object> findAllAudioAlbum(int page, int size, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.DESC, sortBy);
        Pageable sortedPageable = PageRequest.of(page, size, sort);
        Page<AudioAlbum> audioAlbumPage = audioAlbumRepository.findAll(sortedPageable);
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

        return responseBody;
    }

    // 선택한 오디오앨범 조회
    @Transactional
    public AudioAlbumResponseDto findAudioAlbum(Long audioAlbumId, UserDetailsImpl userDetails) {
        AudioAlbum audioAlbum = audioAlbumRepository.findById(audioAlbumId).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOALBUM.getMessage())
        );


        List<AudioClip> foundAudioClip = audioClipRepository.findTop10ByAudioAlbumIdOrderByCreatedAtDesc(audioAlbumId)
                .stream()
                .limit(10)
                .toList();
        List<AudioClipResponseDto> audioAlbumResponseDtos = new ArrayList<>();
        if(!foundAudioClip.isEmpty()){
            for(AudioClip audioClip : foundAudioClip){
                audioAlbumResponseDtos.add(new AudioClipResponseDto(audioClip));
            }
        }else {
            audioAlbumResponseDtos = null;
        }
        boolean isLikeCheck = audioAlbumLikeRepository.findByAudioAlbumAndMember(audioAlbum, userDetails.getUser()).isPresent();
        boolean isMine = audioAlbum.getMember().getId().equals(userDetails.getUser().getId());


        return new AudioAlbumResponseDto(audioAlbum, audioAlbumResponseDtos, isLikeCheck, isMine);
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
    }

    // 카테고리별 오디오앨범 조회
    @Transactional
    public Map<String, Object> findByCategory(int page, int size, String sortBy, CategoryType categoryType) {
        Sort sort = Sort.by(Sort.Direction.DESC, sortBy);
        Pageable sortedPageable = PageRequest.of(page, size, sort);
        Page<AudioAlbum> audioAlbumPage = audioAlbumRepository.findAllByCategoryType(categoryType,sortedPageable);
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
        String imageUrl = s3Uploader.uploadFiles(requestDto.getBackgroundImageUrl(), "audioAlbumImages");

        audioAlbum.update(requestDto, imageUrl);

        return AudioAlbumResponseDto.of(audioAlbum);
    }

}
