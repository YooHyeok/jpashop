package jpabook.jpashop;

import jpabook.jpashop.repository.MemberEmRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class MemberRepositoryTest {

    @Autowired
    MemberEmRepository memberRepository;

    @Test
    @Transactional
//    @Commit
    @Rollback(false)
    void  testMember() {
        //given
//        Member member = new Member();
//        member.setUsername("memberA");

        //when
//        Long saveId = memberRepository.save(member);
//        Member findMember = memberRepository.find(saveId);

        //then
//        Assertions.assertThat(findMember).isSameAs(member);
//        Assertions.assertThat(findMember).isEqualTo(member); //영속성 컨텍스트에 의해서 member와 같은 객체를 findMember로 반환한다. (SelectQuery가 돌지 않음.) - find메소드에 clear하면 오류발생한다.
//        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
    }
}