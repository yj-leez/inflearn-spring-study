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


# 2주차

# 섹션 4 중급 문법

## 프로젝션과 결과 반환 - 기본

### 프로젝션 결과가 둘 이상일 때 Tuple로 조회

```java
List<Tuple> result = queryFactory
         .select(member.username, member.age)
         .from(member)
         .fetch();
```

- Tuple로 조회했다면 Service 단에는 Dto로 변환해서 나가는 것을 추천

## **프로젝션과 결과 반환** - DTO **조회**

### JPQL로 조회

```java
List<MemberDto> result = em.createQuery(
         "select new study.querydsl.dto.MemberDto(m.username, m.age) " +
             "from Member m", MemberDto.class)
     .getResultList();
```

- DTO의 package이름을 다 적어줘야해서 지저분함
- 생성자 방식만 지원함

↓ QueryDsl은 이 문제들을 해결

### 프로퍼티 접근 - Setter

```java
List<MemberDto> result = queryFactory
         .select(Projections.bean(MemberDto.class,
                 member.username,
                 member.age))
         .from(member).fetch();
```

- 기본 생성자로 만들고 setter로 값 주입

### 필드 직접 접근

```java
List<MemberDto> result = queryFactory
         .select(Projections.fields(MemberDto.class,
        member.username,
        member.age))
.from(member)
.fetch();
```

- 기본 생성자로 만들고 필드 주입
- 필드명이 다를 시 (eg. `name`) `member.username.as("name")` 와 같이 필드에 별칭을 적용해야 함
    
    → 별칭 적용하지 않는 경우 null 값 들어감
    
    - 다른 방법 `ExpressionUtils.as(source,alias)` 도 있음
        
        ```java
        List<UserDto> result = queryFactory
               .select(Projections.fields(UserDto.class,
                      member.username.as("name"),
                      ExpressionUtils.as(JPAExpressions
                               .select(memberSub.age.max())
                               .from(memberSub), "age")))
                .from(member)
                .fetch();
        ```
        

### 생성자 사용

```java
List<MemberDto> result = queryFactory
         .select(Projections.constructor(MemberDto.class,
                 member.username,
                 member.age))
         .from(member)
				 .fetch();
```

- 생성자의 파라미터 순서와 일치해야함

## **프로젝션과 결과 반환** - @QueryProjection

Dto의 Constructor에 `@QueryProjection` 붙여주고 ./gradlew CompileJava 실행 후

```java
@QueryProjection
public MemberDto(String username, int age) {
    this.username = username;
    this.age = age;
}
```

```java
List<MemberDto> result = queryFactory
         .select(new QMemberDto(member.username, member.age))
         .from(member)
         .fetch();
```

- 기본 Constructor을 사용할 땐 파라미터 에러가 있더라도 런타임 시 에러 발견하지만
    
    @QueryProjection 사용 시 컴파일 시에도 에러 발견 가능
    
    → 가장 안전한 방법
    
- 하지만 DTO까지 Q 파일을 생성해야 하는 단점, 아키텍처적으로 고민해봐야할 사안이 있음

## 동적 쿼리 - BooleanBuilder 사용

```java
@Test
public void dynamicQuery_BooleanBuilder(){
    String usernameParam = "member1";
    Integer ageParam = null;

    List<Member> result = searchMember1(usernameParam, ageParam);
    assertThat(result.size()).isEqualTo(1);
}

private List<Member> searchMember1(String usernameCond, Integer ageCond) {

     BooleanBuilder builder = new BooleanBuilder(); // 초기 조건 넣을 수 있음
     if (usernameCond != null) {
        builder.and(member.username.eq(usernameCond)); // and나 or로 조립가능
     }
     if (ageCond != null){
        builder.and(member.age.eq(ageCond));
     }

     return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
}
```

- where 절을 한 번에 보기 어렵다는 단점 존재
- and나 or을 사용하여 조건을 조립할 수 있음

## 동적 쿼리 - Where 다중 파라미터 사용

```java
@Test
public void dynamicQuery_WhereParam(){
    String usernameParam = "member1";
    Integer ageParam = 10;

    List<Member> result = searchMember2(usernameParam, ageParam);
    assertThat(result.size()).isEqualTo(1);
}

private List<Member> searchMember2(String usernameCond, Integer ageCond) {
    return queryFactory
                .selectFrom(member)
                .where(usernameEq(usernameCond), ageEq(ageCond))
                .fetch();
}

private BooleanExpression usernameEq(String usernameCond) {
    return usernameCond == null ? null : member.username.eq(usernameCond);
}

private BooleanExpression ageEq(Integer ageCond) {
    return ageCond == null ? null : member.age.eq(ageCond);
}

private BooleanExpression allEq(String usernameCond, Integer ageCond) {
    return usernameEq(usernameCond).and(ageEq(ageCond));
}
```

- BooleanExpression을 사용하여 객체들을 조립할 수 있음 → `allEq()`

## 수정, 삭제 벌크 연산

```java
long count = queryFactory
         .update(member)
			   .set(member.username, "비회원") 
	       .where(member.age.lt(28)) .execute();
```

- 쿼리 한 번으로 대량의 데이터를 수정이 가능하지만
    
    영속성 컨텍스트를 거치지 않고 디비에 바로 쿼리를 날림
    
    → 데이터의 일관성이 무시됨
    
    → 추후 DB에서 조회해와도 영속성 컨텍스트에 데이터가 남아있으면 조회해 온 값을 무시하고 영속성 컨텍스트에 있는 엔티티를 취함 → 문제 발생
    
    ⇒ 해결 방안: 쿼리 날린 후 영속성 컨텍스트를 flush, clear하자
    
    ```java
    em.flush();
    em.clear();
    ```
    

```java
long count = queryFactory
         .update(member)
         .set(member.age, member.age.add(1))
         .execute();
```

- 내장함수를 사용해도 됨

## SQL function 호출

```java
String result = queryFactory
         .select(member.username)
				 .from(member)
				 .where(member.username.eq(Expressions.stringTemplate("function('lower', {0})",
 member.username)))
```

- SQL function은 JPA와 같이 Dialect에 등록된 내용만 호출 가능

```java
.where(member.username.eq(member.username.lower()))
```

- lower 같은 ansi 표준 함수들은 querydsl이 내장하고 있는 함수를 사용 가능
