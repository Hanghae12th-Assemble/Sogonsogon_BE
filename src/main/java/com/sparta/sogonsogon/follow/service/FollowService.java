package com.sparta.sogonsogon.follow.service;

import com.sparta.sogonsogon.dto.StatusResponseDto;
import com.sparta.sogonsogon.enums.ErrorMessage;
import com.sparta.sogonsogon.follow.dto.FollowRequestDto;
import com.sparta.sogonsogon.follow.dto.FollowResponseDto;
import com.sparta.sogonsogon.follow.entity.Follow;
import com.sparta.sogonsogon.follow.repository.FollowRepository;
import com.sparta.sogonsogon.member.entity.Member;
import com.sparta.sogonsogon.member.repository.MemberRepository;
import com.sparta.sogonsogon.noti.service.NotificationService;
import com.sparta.sogonsogon.noti.util.AlarmType;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final NotificationService notificationService;
    private final DataSource dataSource;

    // 유저를 팔로잉하는 모든 사용자 가져오기
    @Transactional
    public List<FollowResponseDto> getFollowings(Long memberId) {
//
//        List<FollowResponseDto> followingList = followRepository.findAllBy
        Member member = memberRepository.findById(memberId).orElseThrow(
                ()-> new EntityNotFoundException(ErrorMessage.WRONG_USERNAME.getMessage())
        );

        List<Follow> followings = followRepository.findByFollower(member);
        List<FollowResponseDto> followingList = new ArrayList<>();
        for(Follow follow : followings) {
            FollowResponseDto responseDto = new FollowResponseDto();
            responseDto.setId(follow.getId());
            responseDto.setFollowername(follow.getFollower().getMembername());
            responseDto.setFollowingname(follow.getFollowing().getMembername());
            followingList.add(responseDto);
        }
        return followingList;

    }

    //유저가 팔로워하는 모든 유저 가져오기
    @Transactional
    public List<FollowResponseDto> getFollowers(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.WRONG_USERNAME.getMessage())
        );
        List<Follow> follows = followRepository.findByFollowingId(memberId);

        List<FollowResponseDto> responseDtos = new ArrayList<>();
        for(Follow follow : follows){
            FollowResponseDto responseDto = new FollowResponseDto();
            responseDto.setFollowername(follow.getFollower().getMembername());
            responseDto.setFollowingname(follow.getFollowing().getMembername());
//            responseDto.setFollowed(true);
            responseDtos.add(responseDto);

        }
        return  responseDtos;
    }


    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public FollowResponseDto toggleFollow(String membername, UserDetailsImpl userDetails) {
        Member follow = memberRepository.findByMembername(membername).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERNAME.getMessage())
        );
        Member follower = memberRepository.findById(userDetails.getUser().getId()).orElseThrow(
                () -> new AuthenticationCredentialsNotFoundException(ErrorMessage.ACCESS_DENIED.getMessage())
        );

        if (follow.getId().equals(follower.getId())) {
            throw new IllegalArgumentException(ErrorMessage.WRONG_SELF_REQUEST.getMessage());
        }

        Follow followStatus = followRepository.findByFollowingAndFollower(follower, follow).orElse(null);

//        Boolean isFollow = false; // 팔로우여부 확인

        if (followStatus == null) {
            Follow newFollow = new Follow(new FollowRequestDto(follow, follower));
            newFollow.getFollower().setIsFollowCheck(true);

            followRepository.save(newFollow);

            String message = "회원 " + follower.getNickname() + " 님이 회원님을 팔로우하였습니다.";
            notificationService.send(follow, AlarmType.eventFollower, message,follower.getMembername(),follower.getNickname(),follower.getProfileImageUrl());
            return FollowResponseDto.of(newFollow,follow.getNickname() + "님을 팔로우하였습니다.",newFollow.getFollower().getIsFollowCheck());

        } else {
            followRepository.deleteById(followStatus.getId());
//            isFollow = false;
            followStatus.getFollower().setIsFollowCheck(false);

            // 유저가 알림 구독 중인지 조회
            String message = "회원 " + follower.getNickname() + " 님이 회원님을 팔로우 취소하였습니다.";
            notificationService.send(follow, AlarmType.eventFollower, message,follower.getMembername(),follower.getNickname(),follower.getProfileImageUrl());
            return FollowResponseDto.of(followStatus,follow.getNickname() + "님을 팔로우 취소하였습니다.",followStatus.getFollower().getIsFollowCheck());
        }



    }
}
