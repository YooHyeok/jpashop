package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository {
    private final EntityManager em;

    /**
     * 회원 저장
     * @param member
     */
    public void save(Member member) {
        em.persist(member);
    }

    /**
     * 회원 단건 조회 (id기준)
     * @param id
     * @return
     */
    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    /**
     * 회원 전체 조회
     * @return
     */
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    /**
     * 회원 이름을 기준으로 회원 전체 조회
     * @param name
     * @return
     */
    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
