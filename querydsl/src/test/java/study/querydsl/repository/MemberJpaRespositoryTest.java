package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

// command + shift + t 로 테스트 생성
@SpringBootTest
@Transactional
class MemberJpaRespositoryTest {


    @Autowired
    EntityManager em;
    @Autowired MemberJpaRespository memberJpaRespository;

    @Test
    public void basicTest(){
        Member member = new Member("member1", 10);
        memberJpaRespository.save(member);

        Member findMember = memberJpaRespository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member); // 같은 영속성 콘텍스트이기 때문에 객체 주소가 같음

        List<Member> result1 = memberJpaRespository.findAll_querydsl();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRespository.findByUsername_querydsl("member1");
        assertThat(result2).containsExactly(member);
    }

    @Test
    public void searchTest(){
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);


        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        List<MemberTeamDto> result = memberJpaRespository.search(condition);

        assertThat(result).extracting("username").containsExactly("member4");

    }




}