package com.sparta.sogonsogon.enums;

public enum ErrorMessage {

    // jwt error
    FORBIDDEN("권한이 없습니다."),
    UNAUTHORIZED("토큰이 유효하지 않습니다."),

    // member
    DUPLICATE_USERNAME("중복된 아이디가 존재합니다."),
    DUPLICATE_EMAIL("중복된 이메일이 존재합니다."),
    WRONG_USERNAME("해당 사용자를 찾을 수 없습니다."),
    WRONG_PASSWORD("비밀번호가 틀렸습니다."),
    ACCESS_DENIED("권한이 없습니다."),
    NOT_FOUND_MEMBER("해당 사용자를 찾을 수 없습니다. "),

    // follow
    WRONG_SELF_REQUEST("자기자신을 팔로우할 수 없습니다."),

    // audioAlbum
    DUPLICATE_AUDIOALBUM_NAME("중복된 오디오앨범명입니다."),
    NOT_FOUND_AUDIOALBUM("해당하는 오디오앨범이 없습니다."),
//    NOT_FOUND_ENTER_MEMBER("해당 라디오에 참여하지 않습니다."),

    //audioclip
    NOT_FOUND_AUDIOCLIP("해당 오디오 클립을 찾을 수 없습니다."),
    WRONG_FILE_TYPE("파일의 타입이 잘못되었습니다. "),

    //audioclip 안에 있는 comment
    NOT_FOUND_COMMENT("해당 오디오 클립에 있는 댓글을 찾을 수 없습니다."),


    // category
    NOT_FOUND_CATEGORY("해당하는 카테고리가 없습니다.")

    ;

    String message;
    ErrorMessage(String description) {
        this.message = description;
    }

    public String getMessage() {
        return this.message;
    }
}
