package com.sparta.sogonsogon.audioAlbum.service;

import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumCommentRequestDto;
import com.sparta.sogonsogon.audioAlbum.dto.AudioAlbumCommentResponseDto;
import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbum;
import com.sparta.sogonsogon.audioAlbum.entity.AudioAlbumComment;
import com.sparta.sogonsogon.audioAlbum.repository.AudioAlbumCommentRepository;
import com.sparta.sogonsogon.audioAlbum.repository.AudioAlbumRepository;
import com.sparta.sogonsogon.enums.ErrorMessage;
import com.sparta.sogonsogon.member.entity.Member;
import com.sparta.sogonsogon.member.entity.MemberRoleEnum;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AudioAlbumCommentService {

    private final AudioAlbumCommentRepository audioAlbumCommentRepository;
    private final AudioAlbumRepository audioAlbumRepository;

    @Transactional
    public AudioAlbumCommentResponseDto createComment(Long audioAlbumId, String content, UserDetailsImpl userDetails) {
        AudioAlbum audioAlbum = audioAlbumRepository.findById(audioAlbumId).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOALBUM.getMessage())
        );
        AudioAlbumComment comment = new AudioAlbumComment(userDetails.getUser(), audioAlbum, content);
        audioAlbumCommentRepository.save(comment);
        return new AudioAlbumCommentResponseDto(comment);
    }

    @Transactional
    public AudioAlbumCommentResponseDto updateComment(Long commentId, AudioAlbumCommentRequestDto requestDto, UserDetailsImpl userDetails) {
        Member member = userDetails.getUser();
        AudioAlbumComment comment = audioAlbumCommentRepository.findById(commentId).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.NOT_FOUND_COMMENT.getMessage())
        );

        if (member.getRole() == MemberRoleEnum.USER || member.getMembername().equals(comment.getMember().getMembername())) {
            comment.update(requestDto);
            return new AudioAlbumCommentResponseDto(comment);
        } else {
            throw new IllegalArgumentException(ErrorMessage.ACCESS_DENIED.getMessage());
        }
    }

    @Transactional
    public String deleteComment(Long commentId, UserDetailsImpl userDetails) {
        Member member = userDetails.getUser();
        AudioAlbumComment comment = audioAlbumCommentRepository.findById(commentId).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.NOT_FOUND_COMMENT.getMessage())
        );

        if (member.getRole() == MemberRoleEnum.USER || member.getMembername().equals(comment.getMember().getMembername())) {
            audioAlbumCommentRepository.delete(comment);
            return "해당 댓글이 삭제되었습니다.";
        } else {
            throw new IllegalArgumentException(ErrorMessage.ACCESS_DENIED.getMessage());
        }
    }

    @Transactional
    public List<AudioAlbumCommentResponseDto> getComments(Long audioAlbumId) {
        List<AudioAlbumComment> list = audioAlbumCommentRepository.findAllByAudioAlbumId(audioAlbumId);
        List<AudioAlbumCommentResponseDto> responseDtos = new ArrayList<>();
        for (AudioAlbumComment comment : list) {
            responseDtos.add(AudioAlbumCommentResponseDto.from(comment));
        }
        return responseDtos;
    }
}
