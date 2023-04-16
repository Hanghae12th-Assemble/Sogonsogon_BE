package com.sparta.sogonsogon.member.service;

import com.sparta.sogonsogon.dto.StatusResponseDto;
import com.sparta.sogonsogon.enums.ErrorMessage;
import com.sparta.sogonsogon.follow.repository.FollowRepository;
import com.sparta.sogonsogon.jwt.JwtUtil;
import com.sparta.sogonsogon.member.dto.*;
import com.sparta.sogonsogon.member.entity.Member;
import com.sparta.sogonsogon.member.entity.MemberRoleEnum;
import com.sparta.sogonsogon.member.repository.MemberRepository;
import com.sparta.sogonsogon.security.UserDetailsImpl;
import com.sparta.sogonsogon.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final JwtUtil jwtUtil;
    private final S3Uploader s3Uploader;

    private final JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String sogon;

    //회원 가입
    @Transactional
    public MemberResponseDto signup(SignUpRequestDto requestDto) throws IllegalAccessException {
        String membername = requestDto.getMembername();
        String password = passwordEncoder.encode(requestDto.getPassword());
        String email = requestDto.getEmail();

        Optional<Member> foundMembername = memberRepository.findByMembername(membername);
        if (foundMembername.isPresent()) {
            throw new DuplicateKeyException(ErrorMessage.DUPLICATE_USERNAME.getMessage()); // HTTP 409 Conflict
        }
        Optional<Member> foundEmail = memberRepository.findByEmail(email);
        if (foundEmail.isPresent()) {
            throw new DuplicateKeyException(ErrorMessage.DUPLICATE_EMAIL.getMessage());
        }

        Member member = new Member(requestDto, password);
        memberRepository.save(member);
        return new MemberResponseDto(member);
    }

    //로그인
    @Transactional(readOnly = true)
    public ResponseEntity<StatusResponseDto<MemberResponseDto>>  login(LoginRequestDto requestDto) {
        String email = requestDto.getEmail();
        String password = requestDto.getPassword();

        Member member = memberRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException(ErrorMessage.WRONG_USERNAME.getMessage()) // HTTP 404 Not Found
        );

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new BadCredentialsException(ErrorMessage.WRONG_PASSWORD.getMessage()); // HTTP 401 Unauthorized
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        String token = jwtUtil.createToken(member.getMembername(), member.getRole());
        responseHeaders.set("Authorization",token);

        MemberResponseDto memberResponseDto = new MemberResponseDto(member);

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(StatusResponseDto.success(HttpStatus.OK, memberResponseDto));

    }

    // 회원 정보 수정
    @Transactional
    public MemberResponseDto update(Long id, MemberRequestDto memberRequestDto, UserDetailsImpl userDetails) throws IOException {
        Member member = memberRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERNAME.getMessage())
        );

        String profileImageUrl = "";
        if(memberRequestDto.getProfileImage()== null){
            profileImageUrl = member.getProfileImageUrl();
        }else{
            profileImageUrl = s3Uploader.upload(memberRequestDto.getProfileImage(), "profileImages");
        }

        if(memberRequestDto.getMemberInfo() == null){
            memberRequestDto.setMemberInfo(member.getMemberInfo());
        } else if (memberRequestDto.getMemberInfo().isBlank()) {
            memberRequestDto.setMemberInfo(member.getMemberInfo());
        }

        if(memberRequestDto.getNickname() == null){
            memberRequestDto.setNickname(member.getNickname());
        }else if (memberRequestDto.getNickname().isBlank()) {
            memberRequestDto.setNickname(member.getNickname());
        }

        if (member.getRole() == MemberRoleEnum.USER  || member.getMembername().equals(userDetails.getUser().getMembername())) {
            member.update(memberRequestDto, profileImageUrl);
            return new MemberResponseDto(member);
        } else {
            throw new IllegalArgumentException(ErrorMessage.ACCESS_DENIED.getMessage());
        }
    }

    // 고유 아이디로 유저 정보 조회
    @Transactional
    public MemberResponseDto getInfoByMembername(String membername) {
        Member member = memberRepository.findByMembername(membername).orElseThrow(
                () -> new EntityNotFoundException(ErrorMessage.WRONG_USERNAME.getMessage())
        );

        return new MemberResponseDto(member);
    }

    //유저 닉네임으로 정보 조회
    @Transactional
    public StatusResponseDto<List<MemberOneResponseDto>> getListByNickname(String nickname) {
//        log.info(nickname);

        List<Member> memberlist = memberRepository.searchAllByNicknameLike(nickname);
//        log.info(memberlist.toString());
        List<MemberOneResponseDto> memberResponseDtos = new ArrayList<>();
        for (Member member : memberlist) {
            memberResponseDtos.add(MemberOneResponseDto.of(member));
        }
//        log.info(memberResponseDtos.toString());
        return StatusResponseDto.success(HttpStatus.OK, memberResponseDtos);
    }


    // 유사한 유저 닉네임으로 정보 조회 무한스크롤 적용
    @Transactional(readOnly = true)
    public List<MemberOneResponseDto> getListBySimilarNickname(int page, int size, String sortBy, String nickname) {
        // 검색 조건 설정. 대소문자 상관없이 검색
        // Create Example Matcher for searching by nickname
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("nickname", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        Member member = new Member();
        member.setNickname(nickname);
        Example<Member> example = Example.of(member, matcher);

        // Set up pagination and sorting options
        Sort sort = Sort.by(Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Find all members that match the given nickname
        Page<Member> nicknamePage = memberRepository.findAll(example, pageable);
        List<Member> memberList = nicknamePage.getContent();

        // Convert members to DTOs
        List<MemberOneResponseDto> memberOneResponseDtoList = new ArrayList<>();
        for (Member member1 : memberList) {
            memberOneResponseDtoList.add(MemberOneResponseDto.of(member1));
        }

        return memberOneResponseDtoList;
    }

    @Transactional
    public StatusResponseDto<MemberResponseDto> detailsMember(Long memberId) {
        Member member = memberRepository.findMemberById(memberId).orElseThrow(
                () -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다. ")
        );
        MemberResponseDto memberResponseDto = new MemberResponseDto(member);
        return StatusResponseDto.success(HttpStatus.OK, memberResponseDto);
    }


    // 회원정보 찾기
    @Transactional
    public String findMemberInfo(EmailRequestDto requestDto) {
        Member member = memberRepository.findByEmail(requestDto.getEmail()).orElseThrow(
                () -> new IllegalArgumentException(ErrorMessage.NOT_FOUND_MEMBER.getMessage())
        );
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(sogon);
        message.setTo(requestDto.getEmail());
        message.setSubject("소곤소곤 회원 정보 문의");
        message.setText(member.getEmail() + " " + member.getPassword());
        emailSender.send(message);
        return "이메일이 전송되었습니다.";
    }
}

