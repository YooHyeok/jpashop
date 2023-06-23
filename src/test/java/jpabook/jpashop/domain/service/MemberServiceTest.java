package jpabook.jpashop.domain.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.service.MemberService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberServiceTest {
    @Autowired
    MemberService memberService;
    @Autowired MemberRepository memberRepository;

    @Test
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("yoo");

        //when - persist로 인해 1차캐시에 등록 & flush하지 않았으므로 insert문이 날라가지 않는다.
        Long saveId = memberService.join(member); // select쿼리 발생 이유는 join메소드에서 저장직전 name중복여부를 select쿼리로 체킹하기때문.

        //then - 1차캐시로부터 객체를 반환받으므로 동일한 객체임을 보장받는다.
        Assertions.assertEquals(member, memberRepository.findOne(saveId));
    }
//    @Test(expected = IllegalStateException.class) //JUnit4에서 가능 - 현 JUnit5
    @Test
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("yoo1");
        Member member2 = new Member();
        member2.setName("yoo1");

        //when
        memberService.join(member1);
        try {
            memberService.join(member2);
        } catch (IllegalStateException e) { // IllegalStateException 발생시 종료
            return;
        }

        //then
        Assertions.fail("중복 회원 예외가 발생해야 한다. 회원 이름이 중복이 아닌지 확인해보자.");
    }

}