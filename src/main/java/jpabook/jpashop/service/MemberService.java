package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberEmRepository;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) // 읽기전용(select) - 더티체킹X, DB리소스절약 등의 이점
@RequiredArgsConstructor
public class MemberService {
    private final MemberEmRepository memberEmRepository; // @RequiredArgsConstructor에 의해 생성자 의존성 자동 주입됨
    private final MemberRepository memberRepository;// SpringDataJPA 리포지토리

    /**
     * 회원 가입
     */
    @Transactional(readOnly = false)// 기본값 false (insert,update 등 변경사항)
    public Long join(Member member) {
        validateDuplicateMember(member);
//        memberEmRepository.save(member);
        memberRepository.save(member);//SpringDataJPA 변경 반영
        return member.getId();
    }

    /**
     * 회원 수정(API) - 변경감지 <br/>
     * 이름 변경
     */
    @Transactional(readOnly = false)
    public void update(Long id, String name) {
//        Member member = memberEmRepository.findOne(id);
        Member member = memberRepository.findById(id).get();//SpringDataJPA 변경 반영
        member.setName(name);
    }
    /**
     * 중복회원 검증 메소드
     * @param member
     */
    private void validateDuplicateMember(Member member) {
//        List<Member> findMembers = memberEmRepository.findByName(member.getName()); // 동시성 문제 대안 : name 컬럼 Unique 제약조건 지정
        List<Member> findMembers = memberRepository.findByName(member.getName());//SpringDataJPA 변경 반영
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원 입니다.");
        }
    }

    /**
     * 회원 전체 조회
     */
    public List<Member> findMembers() {
//        return memberEmRepository.findAll();
        return memberRepository.findAll();//SpringDataJPA 변경 반영
    }

    /**
     * 회원 단건 조회
     */
    public Member findOne(Long id) {
//        return memberEmRepository.findOne(id);
        return memberRepository.findById(id).get();//SpringDataJPA 변경 반영
    }
}
