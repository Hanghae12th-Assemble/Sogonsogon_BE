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
    private String followerNickname;
    private String followerMembername;
    private String followerProfileImageUrl;
    private Long followerId;
    private String followingname;
//    private boolean isFollowed;
    private String message;
    private Boolean isFollowCheck;

    FollowResponseDto (Follow follow, String message, Boolean isFollowCheck) {
        this.followerNickname = follow.getFollower().getNickname();
        this.followingname = follow.getFollowing().getNickname();
        this.message = message;
        this.isFollowCheck = isFollowCheck;

    }

    public FollowResponseDto(Member followerMember, Member followingMember) {
        this.followerNickname = followerMember.getNickname();
        this.followerMembername = followerMember.getMembername();
        this.followerProfileImageUrl = followerMember.getProfileImageUrl();
        this.followerId = followerMember.getId();
        this.followingname = followingMember.getMembername();
    }

    public static FollowResponseDto of (Follow follow, String message, Boolean isFollowCheck){
        return new FollowResponseDto(follow,message,isFollowCheck);
    }




}
