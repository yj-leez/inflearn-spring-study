# 1주차

# 섹션 1. 프로젝트 환경설정

## Querydsl 설정과 검증

```java
	/* test 롬복 사용 */
	testCompileOnly 'org.projectlombok:lombok'
	testAnnotationProcessor 'org.projectlombok:lombok'

	/* querydsl 추가 */
	implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
	annotationProcessor "com.querydsl:querydsl-apt:${dependencyManagement.importedProperties['querydsl.version']}:jakarta"
	annotationProcessor "jakarta.annotation:jakarta.annotation-api"
	annotationProcessor "jakarta.persistence:jakarta.persistence-api"
}

...

def querydslSrcDir = 'src/main/generated'
clean {
	delete file(querydslSrcDir)
}
tasks.withType(JavaCompile) {
	options.generatedSourceOutputDirectory = file(querydslSrcDir)
}
```

- Q 타입 생성 : Gradle → build → clean | Gradle → other → compileJava
    - 명령어로도 가능 : `./gradlew clean` → `./gradlew compileJava`
- Q 타입은 git에 올리지말자

## 스프링 부트 설정 - JPA, DB

- h2
    
    ```java
    cd desktop/h2/bin
    ./h2.sh
    
    jdbc:h2:~/querydsl -- 맨 처음 한 번 파일모드로 실행시켜야함
    jdbc:h2:tcp://localhost/~/querydsl -- 그 후 연결
    ```
    
- @Transaction + @Test → 항상 rollback

# 섹션 2. 예제 도메인 모델

# 섹션 3. 기본 문법

## 기본 Q-Type 활용

```java
@Test
 public void startQuerydsl() {
		//member1을 찾아라.
		JPAQueryFactory queryFactory = new JPAQueryFactory(em);
		QMember m = new QMember("m"); // 어떤 QMember인지 구분하는 문자 추가

    Member findMember = queryFactory
             .select(m)
						 .from(m)
						 .where(m.username.eq("member1"))//파라미터 바인딩 처리 
						 .fetchOne();
    
		assertThat(findMember.getUsername()).isEqualTo("member1");
 }
```

↓   JPAQueryFactory를 필드로 빼고, static import를 활용하여 다음과 같이 사용

단, 같은 테이블을 조인하는 경우에는 직접 선언하여 사용

```java
import static study.querydsl.entity.QMember.*;

		JPAQueryFactory queryFactory;

 		@Test
    public void startQuerydsl(){

        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");

    }
```

## 검색 조건 쿼리

- JPQL이 제공하는 모든 검색 조건 제공
    
    ```java
    member.username.eq("member1") // username = 'member1'
    member.username.ne("member1") //username != 'member1'
    member.username.eq("member1").not() // username != 'member1'
    member.username.isNotNull() //이름이 is not null
    member.age.in(10, 20) // age in (10,20)
    member.age.notIn(10, 20) // age not in (10, 20)
    member.age.between(10,30) //between 10, 30
    member.age.goe(30) // age >= 30
    member.age.gt(30) // age > 30
    member.age.loe(30) // age <= 30
    member.age.lt(30) // age < 30
    member.username.like("member%") //like 검색 member.username.contains("member") // like ‘%member%’ 검색 member.username.startsWith("member") //like ‘member%’ 검색
    ```
    
- And 조건은 쉼표로 표현 가능
    
    ```java
    .where(
              member.username.eq("member1")
              ,(member.age.eq(10))
          )
    ```
    

## 결과 조회

- `fetch()` : 리스트 조회, 데이터 없으면 빈 리스트 반환
- `fetchOne()` : 단 건 조회
    
    결과가 없으면 : null
    
    결과가 둘 이상이면 : com.querydsl.core.NonUniqueResultException
    
- `fetchFirst()` : `limit(1).fetchOne()`
- `fetchResults()` : 페이징 정보 포함, total count 쿼리 추가 실행
    
    ```java
    QueryResults<Member> results = queryFactory
                    .selectFrom(member)
                    .fetchResults();
    
    results.getTotal();
    List<Member> content = results.getResults();
    ```
    
    - 쿼리는 한 번 작성하지만 실제로는 select count(*) 와 select * 쿼리 두 번 실행
- `fetchCount()` : count 쿼리로 변경해서 count 수 조회

## 정렬

- `desc()` , `asc()` : 일반 정렬
- `nullsLast()` , `nullsFirst()` : null 데이터 순서 부여
    - null은 기본적으로 가장 작은 값으로 취급

## 페이징

```java
@Test
public void paging1() {
     List<Member> result = queryFactory
             .selectFrom(member)
						 .orderBy(member.username.desc()) .offset(1) //0부터 시작(zero index) .limit(2) //최대 2건 조회
						 .fetch();
     assertThat(result.size()).isEqualTo(2);
}
```

- 전체 결과 수도 필요하다면 `fetchResults()`와 `results.getTotal()`로 count 쿼리를 날려도 되지만 원본 쿼리와 같이 모두 조인을 해버려 성능이 안나올 수 있으므로 주의

## 집합

- JPQL이 제공하는 모든 집합 함수를 제공
- 단 결과의 값이 여러 타입일 수 있으므로 querydsl이 제공하는 Tuple로 꺼내옴

```java
@Test
public void aggregation(){
  
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.min()
                )
                .from(member)
                .fetch();
}
```

### Group By 사용

```java
@Test
public void group(){
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
}
```

## 조인

### 기본 조인

- `join()` , `innerJoin()` : 내부 조인(inner join)
    
    `.join(member.team, team)` → member 테이블의 외래키 team_id로 조인
    
- `leftJoin()` : left 외부 조인(left outer join)
- `rightJoin()` : rigth 외부 조인(rigth outer join)
- 참고) 내부조인은 JOIN 조건에서 동일한 값이 있는 행만을 반환, 외부조인은 JOIN 조건에서 동일한 값이 없어도 null 값으로 출력

```java
@Test
public void join(){
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
            .extracting("username")
            .containsExactly("member1", "member2");
}
```

### 세타 조인

- 연관관계가 없는 필드로 조인

```java
@Test
public void theta_join(){

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA","teamB");
}
```

- 단, 외부 조인은 불가능 → on을 사용하여 외부 조인 가능

### on 절

1. 조인 대상 필터링
    
    ```java
    @Test
    public void join_on_filtering(){
            List<Tuple> result = queryFactory
                    .select(member, team)
                    .from(member)
                    .leftJoin(member.team, team).on(team.name.eq("teamA"))
                    .fetch();
    
            for (Tuple tuple : result) {
                System.out.println("tuple = " + tuple);
            }
    }
    ```
    
    - *회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회*
    - 내부 조인을 이용한 조인 대상 필터링이 필요한 경우는 익숙한 where 절로 해결하는 것이 나음
2. 연관관계 없는 엔티티 외부 조인
    
    ```java
    @Test
    public void join_on_no_relation(){
            em.persist(new Member("teamA"));
            em.persist(new Member("teamB"));
            em.persist(new Member("teamC"));
    
            List<Tuple> result = queryFactory
                    .select(member, team)
                    .from(member)
                    .leftJoin(team).on(member.username.eq(team.name))
            for (Tuple tuple : result) {
                System.out.println("tuple = " + tuple);
            }
    }
    ```
    
    - *회원의 이름이 팀 이름과 같은 회원 외부 조인으로 조회*

### 페치 조인

```java
@Test
public void fetchJoinUse(){
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam()); // 이미 로딩된 엔티티인지 확인
        assertThat(loaded).as("패치 조인 적용").isTrue();
}
```

- `fetchJoin()` : 즉시로딩으로 Member, Team SQL 쿼리 조인으로 한번에 조회

## 서브쿼리

```java
@Test
    public void subQuery(){
        QMember memberSub = new QMember("memberSub"); // alias가 중복되면 안 되는 경우 직접 생성

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions.select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(40);
    }
```

→ static import 처리 가능

- from 절의 서브쿼리 한계
JPA JPQL 서브쿼리의 한계점으로 from 절의 서브쿼리(인라인 뷰)는 지원하지 않아서 Querydsl도 지원하지 않음
    
    → 서브쿼리를 join으로 변경하거나, 쿼리를 2번 분리해서 실행거나, nativeSQL을 사용하여 해결
    

## Case

김영한선생님은 case조건같은건 디비에서 하기보다는 애플리케이션 로직에서 하는 걸 권장