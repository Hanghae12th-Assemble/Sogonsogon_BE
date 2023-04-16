package com.sparta.sogonsogon.follow.dto;

import com.sparta.sogonsogon.follow.entity.Follow;
import com.sparta.sogonsogon.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FollowResponseDto {
    private Long id;
    private Long followerId;
    private String followerNickname;
    private String followerMembername;
    private String followerProfileImageUrl;
    private Long followingId;

//    private boolean isFollowed;
    private String followingNickname;
    private String followingMembername;
    private String message;
    private Boolean isFollowCheck;

    FollowResponseDto (Follow follow, String message, Boolean isFollowCheck) {
        this.followerId = follow.getFollower().getId();
        this.followerNickname = follow.getFollower().getNickname(); // 내가 팔로워 누른대상
        this.followerMembername = follow.getFollower().getMembername();
        this.followerProfileImageUrl = follow.getFollower().getProfileImageUrl();
        this.followingId = follow.getFollowing().getId();
        this.followingNickname = follow.getFollowing().getNickname(); // 나자신
        this.followingMembername = follow.getFollowing().getMembername(); // 나자신
        this.message = message;
        this.isFollowCheck = follow.getFollower().getIsFollowCheck();

    }

    public FollowResponseDto(Member followerMember, Member followingMember) {
        // 내가 팔로워한 사람
        this.followerId = followerMember.getId();
        this.followerNickname = followerMember.getNickname();
        this.followerMembername = followerMember.getMembername();
        this.followerProfileImageUrl = followerMember.getProfileImageUrl();
        this.followingId = followingMember.getId();
        this.followingMembername= followingMember.getMembername(); // 나 자신
        this.followingNickname = followingMember.getNickname(); // 나 자신
        this.isFollowCheck = followingMember.getIsFollowCheck();
    }

    public static FollowResponseDto of (Follow follow, String message, Boolean isFollowCheck){
        return new FollowResponseDto(follow,message,isFollowCheck);
    }




}
