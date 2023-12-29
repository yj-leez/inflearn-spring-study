package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {
    @Autowired
    EntityManager em;
    JPAQueryFactory queryFactory; // Querydsl은 무조건 JPAQueryFactory 생성으로 시작

    /* 테스트 하기전에 데이터 넣기 */
    @BeforeEach
    public void Before(){
        queryFactory = new JPAQueryFactory(em);

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
    }

    /* JPQL으로 쿼리 생성 */
    @Test
    public void startJPQL(){
        //memeber1 찾기
        Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1"); // option + enter로 Assertions 날리기

    }

    /* QueryDSL */
    @Test
    public void startQuerydsl(){
//        QMember m = new QMember("m"); // 어떤 QMember인지 구분하는 문자 추가
//        QMember m = QMember.member; // static import로 더 줄일 수 있음

        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1")) // 파라미터바인딩 방식을 사용하여 인젝션 공격 방지
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");

    }

    /* 검색 조건 */
    @Test
    public void search(){
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10))) // and or 다 걸 수 있음
                .fetchOne();


        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam(){
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1")
                        ,(member.age.eq(10))           // 쉼표로 and 표현 가능
                )
                .fetchOne();


        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    /* 결과 조회 */
    @Test
    public void resultFetch(){
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        Member fetchOne = queryFactory
                .selectFrom(member)
                .fetchOne();

        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();

        /* 페이징에서 사용 */
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();

        results.getTotal(); // count 가져오기
        List<Member> content = results.getResults(); // 데이터 가져오기


        long total = queryFactory
                .selectFrom(member)
                .fetchCount();
    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort(){
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> results = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = results.get(0);
        Member member6 = results.get(1);
        Member memberNull = results.get(2);

        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(member5.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    /* 페이징 */
    @Test
    public void paging1(){
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) // 몇번째부터 끊어서 할건지
                .limit(2)  // 몇개까지 가져올건지
                .fetch();

        assertThat(result).size().isEqualTo(2);
    }

    @Test
    public void paging2(){
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) // 몇번째부터 끊어서 시작할건지
                .limit(2)  // 몇개까지 가져올건지
                .fetchResults();

        assertThat(results.getTotal()).isEqualTo(4);
        assertThat(results.getLimit()).isEqualTo(2);
        assertThat(results.getOffset()).isEqualTo(1);
        assertThat(results.getResults().size()).isEqualTo(2);
    }

    /* 집합 */
    @Test
    public void aggregation(){
        // querydsl이 제공하는 Tuple로 꺼내올 수 있음
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
        //실무에선 tuple말고 dto로 뽑아오는 방법도 많이 사용
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라
     */
    @Test
    public void group(){
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);

    }

    /**
     * 팀 A에 소속된 모든 회원 조인으로 가져오기
     */
    @Test
    public void join(){
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)       // left join도 가능
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인
     * 회원의 이름이 팀 이름과 같은 회원 조희
     */
    @Test
    public void theta_join(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA","teamB");
    }

    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: select m, t from Member m left join m.team t on t.name = 'teamA'
     */
    @Test
    public void join_on_filtering(){
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA")) // join으로 변경 시 조인할 대상이 없는 데이터들은 제외됨
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple" + tuple);
        }
    }

    /**
     * 연관관계가 없는 엔티티 외부 조인
     * 회원의 이름이 팀 이름과 같은 회원 조희
     */
    @Test
    public void join_on_no_relation(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name)) // 원래는 leftJoin(member.team, team) -> 조인대상이 id로 정해짐
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo(){
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam()); // 이미 로딩된 엔티티인지 확인
        assertThat(loaded).as("패치 조인 미적용").isFalse();
    }

    @Test
    public void fetchJoinUse(){
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin() // 이 줄만 추가
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam()); // 이미 로딩된 엔티티인지 확인
        assertThat(loaded).as("패치 조인 적용").isTrue();
    }

    /**
     * 서브 쿼리 -
     * 나이가 가장 많은 회원 조회
     */
    @Test
    public void subQuery(){
        QMember memberSub = new QMember("memberSub"); // alias가 중복되면 안 되는 경우 직접 생성

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(40);
    }

    /**
     * 나이가 평균 이상인 회원
     */
    @Test
    public void subQueryGoe(){
        QMember memberSub = new QMember("memberSub"); // alias가 중복되면 안 되는 경우 직접 생성

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(30, 40);
    }

    /**
     * 서브쿼리 여러 개 처리, in 사용
     */
    @Test
    public void subQueryIn() throws Exception {
        QMember memberSub = new QMember("memberSub"); // alias가 중복되면 안 되는 경우 직접 생성

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40);
    }

    /**
     * 서브쿼리 여러 개 처리, in 사용
     */
    @Test
    public void selectSubQuery(){
        QMember memberSub = new QMember("memberSub"); // alias가 중복되면 안 되는 경우 직접 생성

        List<Tuple> result = queryFactory
                .select(member.username,
                        select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();


        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

    }

    /* case 문 */
    @Test
    public void basicCase(){
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void complexCase(){
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0 ~ 20살")
                        .when(member.age.between(21, 30)).then("21 ~ 30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    /* 상수, 문자 더하기 */
    @Test
    public void constant(){
        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void concat(){
        List<String> result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();

        for (String s : result) {
            System.out.println("tuple = " + s);
        }
    }

}
