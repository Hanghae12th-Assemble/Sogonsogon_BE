package com.sparta.sogonsogon.audioclip.service;

import com.sparta.sogonsogon.audioclip.dto.CommentRequestDto;
import com.sparta.sogonsogon.audioclip.dto.CommentResponseDto;
import com.sparta.sogonsogon.audioclip.entity.Comment;
import com.sparta.sogonsogon.audioclip.repository.AudioClipCommentRepository;
import com.sparta.sogonsogon.audioclip.entity.AudioClip;
import com.sparta.sogonsogon.audioclip.repository.AudioClipRepository;
import com.sparta.sogonsogon.dto.StatusResponseDto;
import com.sparta.sogonsogon.enums.ErrorMessage;
import com.sparta.sogonsogon.member.entity.Member;
import com.sparta.sogonsogon.member.entity.MemberRoleEnum;
import com.sparta.sogonsogon.noti.service.NotificationService;
import com.sparta.sogonsogon.noti.util.AlarmType;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AudioClipCommentService {

    private final AudioClipCommentRepository audioClipCommentRepository;
    private final AudioClipRepository audioClipRepository;
    private final NotificationService notificationService;

    //오디오 클립 댓글 올리기
    @Transactional
    public StatusResponseDto<CommentResponseDto> createComment(Long id, String content, UserDetailsImpl userDetails) {
        AudioClip audioClip = audioClipRepository.findById(id).orElseThrow(
                ()-> new IllegalArgumentException(ErrorMessage.NOT_FOUND_AUDIOCLIP.getMessage())
        );
        Comment comment = new Comment(userDetails.getUser(), audioClip, content);
        audioClipCommentRepository.save(comment);

        // 댓글 생성될 때 오디오 클립 생성한 유저한테 알림 가기
        notificationService.send(audioClip.getMember(), AlarmType.eventCreateComment, "제목: " + audioClip.getTitle() + "오디오 클립에 댓글이 생성되었습니다.  ", comment.getMember().getMembername(), comment.getMember().getNickname(), comment.getMember().getProfileImageUrl());
        log.info("댓글 생성하였습니다.");
        return StatusResponseDto.success(HttpStatus.OK, new CommentResponseDto(comment));
    }

    //오디오 클립 댓글 수정하기
    @Transactional
    public StatusResponseDto<CommentResponseDto> updateComment(Long id, CommentRequestDto commentRequestDto, UserDetailsImpl userDetails) {
        Member member = userDetails.getUser();
        Comment comment = audioClipCommentRepository.findById(id).orElseThrow(
                () -> new NullPointerException(ErrorMessage.NOT_FOUND_COMMENT.getMessage())
        );

        if (member.getRole() == MemberRoleEnum.USER || member.getMembername().equals(comment.getMember().getMembername())) {
            comment.update(commentRequestDto);
            return StatusResponseDto.success(HttpStatus.OK, new CommentResponseDto(comment));
        } else {
            throw new IllegalArgumentException("작성자만 수정이 가능합니다.");
        }
    }

    //오디오 클립 댓글 삭제
    @Transactional
    public StatusResponseDto<String> deleteComment(Long commetId, UserDetailsImpl userDetails) {
        Member member = userDetails.getUser();
        Comment comment = audioClipCommentRepository.findById(commetId).orElseThrow(
                () -> new NullPointerException("댓글을 찾을 수 없음")
        );
        if (member.getRole() == MemberRoleEnum.USER || member.getMembername().equals(comment.getMember().getMembername())) {
            audioClipCommentRepository.delete(comment);
            return StatusResponseDto.success(HttpStatus.OK, "해당 댓글이 삭제 되었습니다.");
        } else {
            throw new IllegalArgumentException("작성자만 수정이 가능합니다.");
        }

    }

    //오디오 클립 댓글 전체조회
    @Transactional
    public StatusResponseDto<List<CommentResponseDto>> getComments(Long audioclipId) {
        List<Comment> list = audioClipCommentRepository.findAllByAudioclipId(audioclipId);
        List<CommentResponseDto> responseDtos = new ArrayList<>();
        for(Comment comment : list){
            responseDtos.add(CommentResponseDto.from(comment));
        }
        return StatusResponseDto.success(HttpStatus.OK, responseDtos);
    }
}
