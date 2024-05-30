# 스프링 DB

# 1. JDBC 이해

### 애플리케이션 서버와 DB- 일반적인 사용법

1. 커넥션 연결: 주로 TCP/IP를 이용해서 커넥션을 연결한다.
2. SQL 전달: 애플리케이션 서버는 DB가 이해할 수 있는 SQL을 연결된 커넥션을 통해 DB에 전달한다.
3. 결과 응답: DB는 전달된 SQL을 수행하고 그 결과를 응답한다. 애플리케이션 서버는 응답 결과를 활용한다.

문제는, 각각의 데이터베이스마다 커넥션을 연결하는 방법, SQL을 전달하는 방법, 그리고 결과를 응답 받는 방법이 모두 다르다는 점이다. 그래서 ***JDBC***라는 자바 표준이 등장하였다.

### JDBC 표준 인터페이스

JDBC는 위의 애플리케이션 서버와 DB의 연결 방법을 ***인터페이스*** 기능으로 정의하여 제공한다.

<img width="598" alt="1" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/92b828e2-bae6-450d-aaaf-fab832962bb2">

아래와 같이 사용할 수 있다.

```java
public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        Connection con = null; 
        PreparedStatement pstmt = null; 
        ResultSet rs = null; 

        try {
            con = getConnection(); // 커넥션 연결
            pstmt = con.prepareStatement(sql); // SQL 전달
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery(); // 결과 응답
            if(rs.next()){
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
            
            }

        } catch (SQLException e) {
            
        } finally {
            closeConnection(con, pstmt, rs);
        }
}
```

JDBC의 등장으로 데이터베이스가 변경될 때마다 사용 코드가 변경되는 문제, 데이터베이스마다 커넥션 연결/ SQL 전달/ 결과를 응답받는 방식을 새로 학습해야하는 문제들을 해결했지만, ***표준화하는데 한계점이 존재**하였다.*

따라서 이러한 한계점을 극복하고, 편리하게 사용할 수 있는 다양한 기술들이 개발되었다.

### JDBC와 최신 데이터 접근 기술

1. ***SQL Mapper***

<img width="596" alt="2" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/833beb1a-2412-46f8-b18c-af81e018ad44">

- 장점: JDBC 편하게 사용할 수 있다.
    - SQL의 응답 결과를 객체로 편리하게 변환해준다.
    - JDBC의 반복 코드를 제거해준다.
- 단점: 개발자가 SQL을 직접 작성해야 한다.

1. ***ORM 기술***

<img width="596" alt="3" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/34679318-e9e7-4280-a9cc-6ea313419f46">

- ORM은 객체를 관계형 데이터베이스 테이블과 매핑해주는 기술이다. 반복적인 SQL을 직접 작성하지 않고, ORM 기술이 SQL을 동적으로 자동으로 만들어 실행해준다.
또한, 각각의 데이터베이스마다 다른 SQL을 사용하는 문제도 중간에서 해결해준다.
- 대표 기술: JPA, 하이버네이트, 이클립스링크
    - JPA는 ***자바 진영의 ORM 표준 인터페이스***이고, 이것을 구현한 것으로 하이버네이트와 이클립스 링크 등의 구현 기술이 있다.

<aside>
💡 JDBC를 직접 사용하지는 않더라도, JDBC가 어떻게 동작하는지 기본 원리를 알아두어야 위 기술들도 깊이 있게 이해할 수 있고, 문제가 발생했을 때 근본적인 문제를 찾아서 해결할 수 있다.

</aside>

### 데이터베이스 연결

```java
Connection connection = DriverManager.getConnection(URL, USERNAME,PASSWORD);
```

먼저, JDBC는 `java.sql.Connection` 표준 커넥션 인터페이스를 정의하고 있다.

그리고 여러 데이터베이스 드라이버는 JDBC Connection 인터페이스를 구현한 구현체를 제공한다. 

<img width="300" alt="4" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/139f637c-5565-43d5-81f3-7dab41a2a34d">

그래서 `DriverManager.getConnection()` 가 호출되면,

1. DriverManager는 라이브러리에 등록된 드라이버 목록을 자동으로 인식하고 있으므로, 이 드라이버들에게 순서대로 정보를 넘겨서 커넥션을 획득할 수 있는지 확인한다.
2. 각 드라이버는 정보를 체크해서 처리할 수 있다면 각 드라이버가 구현한 커넥션 구현체를 반환하여 클라이언트가 사용할 수 있게된다.

<img width="596" alt="5" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/576d82f5-9bf1-47e2-819f-b9cd65d9af2d">


### JDBC 개발

등록, 조회, 수정, 삭제와 같은 CRUD는`src/main/java/hello/jdbc/repository/MemberRepositoryV1.java` 에서 확인할 수 있다.

# 2. 커넥션풀과 데이터 소스 이해

`애플리케이션 서버와 DB- 일반적인 사용법`을 참고하면, 매번 커넥션을 새로 만들어 반환하는 과정은 복잡하고 시간도 많이 많이 소모되는 일임을 알 수 있다. DB는 물론이고 애플리케이션 서버에서도 **TCP/IP 커넥션**을 새로 생성하기 위한 리소스를 매번 사용해야 한다.

이런 문제를 해결하기 위해 ***커넥션 풀***이라는 개념이 등장하였다. 대표적인 커넥션 풀 오픈소스는 `commons-dbcp2` , `tomcat-jdbc pool` , `HikariCP` 등이 있다. 성능과 사용의 편리함 측면에서 최근에는 hikariCP를 주로 사용하고, 스프링 부트 2.0 부터 또한 기본 커넥션 풀로 hikariCP를 제공한다.

### 커넥션 풀

<img width="818" alt="1" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/04aebeef-bf12-46b8-a484-dd0cc6445fa2">

애플리케이션을 시작할 때 커넥션 풀은 필요한 만큼 커넥션을 미리 확보해서 풀에 보관한다. 보통 얼마나 보관할 지는 서비스의 특징과 서버 스펙에 따라 다르지만 기본값은 보통 10개이고 Spring boot - mysql 커넥션 풀도 디폴트 값은 10개이다.

이 풀에 들어있는 커넥션들은 TCP/IP로 DB와 커넥션이 연결되어 있는 상태이기 때문에 언제든지 즉시 SQL을 DB에 전달할 수 있다.

이제 드라이버에서 새로운 커넥션을 획득하는 것이 아닌, ***커넥션 풀을 통해 이미 생성되어 있는 커넥션을 객체 참조로 그냥 가져다 쓰기만 하면 된다.***

### 이상적인 풀의 크기

적절한 커넥션 풀 숫자는 서비스의 특징과 애플리케이션 서버 스펙, DB 서버 스펙에 따라 다르기 때문에 성능 테
스트를 통해서 정해야 한다.
	
여기 `@GeneratedValue(strategy = GenerationType.AUTO)` 어노테이션을 사용하면서 id를 받기 위해 hibernate_sequence 테이블을 조회, update를 하면서 sub transaction이 생성∙실행되는데, 이 추가적인  커넥션때문에 deadlock이 걸리는 상황을 해결하며 이상적인 풀의 크기를 측정한 [우아한 형제 사례](https://techblog.woowahan.com/2663/)가 있다.

<img width="695" alt="2" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/e4e34e89-3e66-4d08-a5cc-370a505b8dce">

### 그래서 DataSource는 뭔데?

<img width="654" alt="3" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/2f8ab553-af20-4600-bc64-68b7a278ffdd">

예를 들어 위의 그림처럼 애플리케이션 로직에서 DriverManager을 사용하여 커넥션을 획득하다가 HikariCP 같은 커넥션 풀을 사용하도록 변경하면 의존관계가 변경되기 때문에 커넥션을 획득하는 애플리케이션 코드도 변경해야한다. 

따라서 이러한 불편함을 줄이기 위해 커넥션을 획득하는 방법을 추상화하는 `javax.sql.DataSource`라는 인터페이스를 제공한다.

<img width="654" alt="4" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/0675594c-ce87-4135-ba76-34ebbd8667e6">

 `DataSource` 를 통해 커넥션을 획득하는 방법을 추상화했기 때문에 애플리케이션 로직은 `DataSource` 인터페이스에만 의존하면 된다. 덕분에 `DriverManagerDataSource` 를 통해서 `DriverManager`를 사용하다가 커넥션 풀을 사용하도록 코드를 변경해도 애플리케이션 로직은 변경하지 않아도 된다.

### DriverManager 사용할 때

*변경 전*

```java
Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
```

*DataSource를 사용하는 방식으로 변경 후*

```java
DriverManagerDataSource dataSource = new DriverManagerDataSource(URL,USERNAME, PASSWORD);

Connection con1 = dataSource.getConnection();
Connection con2 = dataSource.getConnection();
```

### 커넥션 풀 사용할 때

```java
HikariDataSource dataSource = new HikariDataSource(); 
dataSource.setJdbcUrl(URL);
dataSource.setUsername(USERNAME); 
dataSource.setPassword(PASSWORD); 
dataSource.setMaximumPoolSize(10);
dataSource.setPoolName("MyPool");

Connection con1 = dataSource.getConnection();
Connection con2 = dataSource.getConnection();
```

### DataSource **적용**

DataSource로 connection을 획득하여 사용하는 애플리케이션 로직도 살펴보자.

```java
public class MemberRepositoryV1 {
    private final DataSource dataSource;
    
    public MemberRepositoryV1(DataSource dataSource) {
        this.dataSource = dataSource;
		}
		
		//save()
    //findById()
    //update()
    //delete()
    
    private void close(Connection con, Statement stmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeConnection(con);
		}
    private Connection getConnection() throws SQLException {
        Connection con = dataSource.getConnection();
        log.info("get connection={}, class={}", con, con.getClass());
        return con;
		}
}
```

위와 같이 레포지토리를 구성하고 외부에서 DataSource를 주입하여 사용할 수 있다. 스프링은 커넥션을 편하게 여닫을 수 있는 `JdbcUtils`라는 편의 메서드를 제공한다.

이제 DriverManagerDataSource → HikariDataSource로 변경해도 **MemberRepositoryV1의 코드는 전혀 변경하지 않아도 된다.** MemberRepositoryV1는 **DataSource 인터페이스에만 의존**하기 때문이다. 이것이 DataSource를 사용하는 장점이다.

***⇒ DI + OCP***

# 3. 트랜잭션의 이해

### 트랜잭션

트랜잭션은 ACID를 보장해야한다. 

하지만 트랜잭션 간에 격리성을 완벽히 보장하려면 트랜잭 션을 거의 순서대로 실행해야 한다. 이렇게 하면 동시 처리 성능이 매우 나빠지기 때문에 ANSI 표준은 트랜 잭션의 격리 수준을 4단계로 나누어 정의했다.

**트랜잭션 격리 수준 - Isolation level**

- READ UNCOMMITED(커밋되지 않은 읽기)
- READ COMMITTED(커밋된 읽기)
    
    > 실제 테이블 값을 가져오는 것이 아니라 Undo 영역에 백업된 레코드에서 값을 가져옴
    > 
    
    Commit이 이루어진 정보만 조회 가능
    
    대부분의 SQL 서버가 Default로 사용하는 Isolation Level임
    
    <img width="500" alt="7" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/26122231-92a3-4ad2-9a4c-ff96875968d1">
    
- REPEATABLE READ(반복 가능한 읽기)
    
    > MySQL에서는 트랜잭션마다 트랜잭션 ID를 부여하여 트랜잭션 ID보다 작은 트랜잭션 번호에서 변경한 것만 읽게 함
    > 
    
    트랜잭션이 범위 내에서 조회한 데이터 내용이 항상 동일함을 보장함
    
    다른 사용자는 트랜잭션 영역에 해당되는 데이터에 대한 수정 불가능, 하지만 **새로운 행을 추가**할 수 있음
    
    <img width="500" alt="7" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/c1ec79c8-dc8f-47e8-8733-c2dce5951bab">

    
- SERIALIZABLE(직렬화 가능)

### 데이터베이스 연결 구조와 DB 세션

<img width="690" alt="7" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/6be631bf-a605-42d8-bf2f-5ba9a0ec3f40">


사용자는 웹 애플리케이션 서버(WAS)나 DB 접근 툴 같은 클라이언트를 사용해서 데이터베이스 서버에 접근할 수 있다. 클라이언트는 데이터베이스 서버에 연결을 요청하고 커넥션을 맺게 된다. 이때 데이터베이스 서버는 내부에 세션이라는 것을 만든다. 그리고 앞으로 해당 커넥션을 통한 모든 요청은 이 세션을 통해서 실행하게 된다.

즉, 개발자가 클라이언트를 통해 SQL을 전달하면 현재 커넥션에 연결된 세션이 SQL을 실행한다.

### 자동 commit, 수동 commit

*자동 commit:*

각각의 쿼리 실행 직후에 자동으로 커밋을 호출하여, 직접 커밋이나 롤백을 호출하지 않아도 되는 장점이 있다. 하지만 쿼리를 하나하나 실행할 때 마다 자동으로 커밋이 되어버리기 때문에 트랜잭션 기능을 제대로 사용할 수 없다.

*수동 commit:*

```sql
set autocommit false; //수동 커밋 모드 설정
insert into member(member_id, money) values ('data3',10000);
insert into member(member_id, money) values ('data4',10000);
commit; //수동 커밋
```

보통 자동 커밋 모드가 기본으로 설정된 경우가 많기 때문에, **수동 커밋 모드로 설정하는 것을 트랜잭션을 시작**한다고 표현할 수 있다. 그리고 수동 커밋 설정을 하면 이후에 꼭 `commit` , `rollback` 을 호출해야 한다.

### DB 락

<img width="765" alt="8" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/594e3cbf-afc1-497c-91fa-fdb08928d3b5">


```sql
set autocommit false;
update member set money=500 where member_id = 'memberA';
```

세션 1에서 트랜잭션을 시작하고, memberA의 데이터를 500원으로 업데이트 했지만 커밋은 하지 않았다면, memberA 로우의 락은 세션1이 가지게 된다.

<img width="765" alt="9" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/c66037e0-d3f7-4f3a-8b94-4daab9427137">


```sql
SET LOCK_TIMEOUT 60000; --60초 안에 락을 얻지 못하면 예외가 발생
set autocommit false;
update member set money=1000 where member_id = 'memberA';
```

이 때, 세션2가 memberA 의 데이터를 1000원으로 수정하려 해도 세션1이 트랜잭션을 종료하지 않았으므로 락을 가지고 있다. 따라서 세션2가 락을 획득하지 못하기 때문에 데이터를 수정할 수 없고, 락이 돌아올 때까지 대기하게 된다.

<br></br>

하지만 락은 수정 시에만 걸고 있고, **조회할 때는** 락을 획득하지 않고 **바로 데이터를 조회**할 수 있다.  만약 memberA의 금액을 세션1, 세션2에서 동시에 조회하고 값을 차례로 변경한다면, 세션 2에서는 세션 1에서 변경한 값을 무시하게 된다. 이럴 때는 **조회 시점에 락을 획득하여야한다.**

```sql
select * from member where member_id='memberA' for update;
```

`select for update` 구문을 사용하면 조회를 하면서 동시에 선택한 로우의 락도 획득하고, 트랜잭션을 종료할 때까지 로우의 락을 보유할 수 있다.

### 트랜잭션 구현

<img width="860" alt="20" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/c8fe20d5-cb8a-41e8-a396-20e67d65700f">


트랜잭션은 비즈니스 로직이 있는 서비스 계층에서 시작해야 한다. 비즈니스 로직이 잘못되면 해당 비즈니스 로직
으로 인해 문제가 되는 부분을 함께 롤백해야 하기 때문이다.

그런데 트랜잭션을 시작하려면 커넥션이 필요하기 때문에, 서비스 계층에서 커넥션을 만들고 트랜잭션 커밋 이후에 커넥션을 종료해야 한다.

또한, 애플리케이션에서 DB 트랜잭션을 사용하려면 같은 세션을 사용해야하므로 **트랜잭션을 사용하는 동안 같은 커넥션을 유지**해야한다.

먼저, 리포지토리가 파라미터를 통해 같은 커넥션을 유지할 수 있도록 파라미터를 추가한다.

```java
 public class MemberRepositoryV2 {
 
     private final DataSource dataSource;
     
     public MemberRepositoryV2(DataSource dataSource) {
         this.dataSource = dataSource;
     }
     
     public Member findById(Connection con, String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId=" + memberId);
						}
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
				} finally {
						//connection은 여기서 닫지 않는다. 
						JdbcUtils.closeResultSet(rs); 
						JdbcUtils.closeStatement(pstmt);
				} 
		}
}
```

그리고 서비스에서 파라미터를 넘기고 트랜잭션을 구성하는 로직을 작성하자.

```java
@RequiredArgsConstructor
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException, IllegalAccessException {
        Connection con = dataSource.getConnection();

        try {
            con.setAutoCommit(false); // 트랜잭션 시작

            // 비즈니스 로직
            bizLogic(con, fromId, toId, money);

            con.commit(); // 트랜잭션 성공 -> 커밋
        } catch (Exception e) {
            con.rollback(); // 트랜잭션 실패 -> 롤백
            throw new IllegalAccessException();
        } finally {
            release(con);
        }

    }

    private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException, IllegalAccessException {
        Member fromMember = memberRepository.findById(con, fromId);
        Member toMember = memberRepository.findById(con, toId);

        memberRepository.update(con, fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(con, toId, toMember.getMoney() + money);
    }

    private static void release(Connection con) {
        if (con != null){
            try {
                con.setAutoCommit(true); // 커넥션이 풀에 돌아가므로 문제를 예방하기 위해 true로 세팅
                con.close(); // 커넥션 풀 사용하고 있으므로 커넥션이 종료되는 것이 아니라 풀에 반납
            } catch (Exception e){
                log.info("error", e);
            }
        }
    }

    private static void validation(Member toMember) throws IllegalAccessException {
        if (toMember.getMemberId().equals("ex")){
            throw new IllegalAccessException("이체중 예외");
        }
    }
}
```

위와 같이 애플리케이션에서 DB 트랜잭션을 적용할 수 있지만, 서비스 계층이 매우 지저분해지고, 생각보다 매우 복잡한 코드를 요구한다. 스프링을 이용하여 이와 같은 문제를 해결할 수 있다.

# 4. 스프링과 문제 해결 - 트랜잭션

### 문제점들

컨트롤러 계층, 서비스 계층, 리포지토리 계층에서 가장 중요한 부분은 서비스 계층이다. 데이터 기술을 변경하든, 웹 기술이 변경되어도 비즈니스 로직을 담당하는 서비스 계층은 최대한 변경없이 유지되어야 한다. 그래서 서비스 계층을 **특정 기술에 종속적이지 않게 개발해야 한다.**

<br></br>

하지만 트랜잭션을 적용한 MemberServiceV2 코드는 `javax.sql.DataSource`, `java.sql.Connection`, `java.sql.SQLException`같은 JDBC 기술에 의존하고 있고, 핵심 비즈니스 로직과 JDBC 기술이 섞여 있어서 유지보수 하기 어렵다.

<br></br>

즉, 트랜잭션을 적용하기 위해 JDBC 구현 기술이 서비스 계층에 누수되었고,

같은 트랜잭션을 유지하기 위해 커넥션을 파라미터로 넘겨야 하고,

트랜잭션 적용 코드를 보면 반복이 많고,

데이터 접근 계층의 JDBC 구현 기술 예외가 서비스 계층으로 전파되는 문제점들을 가지고 있다.

### 트랜잭션 추상화

먼저, 서비스 계층이 트랜잭션을 사용하기 위해서 JDBC 기술에 의존하고 있는 문제를 해결하자.

<img width="715" alt="1" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/003b19bf-d8f5-4ff8-bc2f-da7a700ca3b8">

JDBC, JPA, 하이버네이트 등 다양한 데이터 접근 기술들은 트랜잭션을 사용하는 코드들이 다르다.

하지만 다행히도 스프링은 이것들을 추상화한 `PlatformTransactionManager`인터페이스를 제공한다.

### 트랜잭션 동기화

트랜잭션 매니저 `PlatformTransactionManager`는 크게 2가지 기능을 제공한다.

- 트랜잭션 추상화
- 리소스 동기화

<img width="717" alt="2" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/f5693441-915c-4406-9cfc-63bac45daa4f">


트랜잭션을 유지하려면 트랜잭션의 시작부터 끝까지 같은 데이터베이스 커넥션을 유지해야하기 때문에 스프링은 **트랜잭션 동기화 매니저**를 제공한다. 이것은 스레드 로컬(`ThreadLocal`)을 사용해서 커넥션을 동기
화해준다. 그리고 트랜잭션 매니저는 내부에서 이 트랜잭션 동기화 매니저를 사용한다.

### 트랜잭션 매니저의 전체 동작흐름

<img width="712" alt="3" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/11501090-4dad-4b7a-bb15-ab5f15963f4f">


1. 서비스 계층에서 `transactionManager.getTransaction()`을 호출해서 트랜잭션을 시작한다.
2. 트랜잭션을 시작하려면 먼저 데이터베이스 커넥션이 필요하다. 트랜잭션 매니저는 내부에서 데이터소스를 사용해서 커넥션을 생성한다.
3. 커넥션을 **수동 커밋 모드로 변경**해서 실제 데이터베이스 트랜잭션을 시작한다.
4. 커넥션을 트랜잭션 동기화 매니저에 보관한다.
5. 트랜잭션 동기화 매니저는 쓰레드 로컬에 커넥션을 보관한다. 따라서 멀티 쓰레드 환경에 안전하게 커넥션을 보관할 수 있다.

<img width="712" alt="4" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/6c4e0fa4-ac06-48dc-85aa-0c2b97e378ab">


1. 서비스는 비즈니스 로직을 실행하면서 리포지토리의 메서드들을 호출한다. 이때 커넥션을 파라미터로 전달하지않는다.
2. 리포지토리 메서드들은 트랜잭션이 시작된 커넥션이 필요하다. 리포지토리는 `DataSourceUtils.getConnection()`을 사용해서 트랜잭션 동기화 매니저에 보관된 커넥션을 꺼내서 사용한다. 이 과정을 통해서 자연스럽게 같은 커넥션을 사용하고, 트랜잭션도 유지된다.
    
    ** `DataSourceUtils`를 따라가다 보면 `TransactionSynchronizationManager`를 사용해서 동기화를 보장하는 것을 알 수 있다
    
    <img width="898" alt="5" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/f0363240-1fe5-466b-a68d-d45e095602c5">

    
3. 획득한 커넥션을 사용해서 SQL을 데이터베이스에 전달해서 실행한다.

<img width="712" alt="6" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/08a11d54-bf77-4001-b8db-717015101f0f">

1. 비즈니스 로직이 끝나고 트랜잭션을 종료한다. 트랜잭션은 커밋하거나 롤백하면 종료된다.
2. 트랜잭션을 종료하려면 동기화된 커넥션이 필요하다. 트랜잭션 동기화 매니저를 통해 동기화된 커넥션을 획득한다.
3. 획득한 커넥션을 통해 데이터베이스에 트랜잭션을 커밋하거나 롤백한다.
4. 전체 리소스를 정리한다.
    - 트랜잭션 동기화 매니저를 정리한다. 쓰레드 로컬은 사용후 꼭 정리해야 한다.
    - `con.setAutoCommit(true)`로 되돌린다. 커넥션 풀을 고려해야 한다.
    - `con.close()`를 호출해셔 커넥션을 종료한다. 커넥션 풀을 사용하는 경우 `con.close()`를 호출하면 커넥션 풀에 반환된다.

### 트랜잭션 매니저 사용 코드

```java
//트랜잭션 시작
TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

try {
//비즈니스 로직
     bizLogic(fromId, toId, money);
		 transactionManager.commit(status); //성공시 커밋 
		} catch (Exception e) {
		 transactionManager.rollback(status); //실패시 롤백
     throw new IllegalStateException(e);
 }
```

여기서는 트랜잭션을 시작하고, 비즈니스 로직을 실행하고, 성공하면 커밋하고, 예외가 발생해서 실패하면 롤백하는 코드가 반복됨을 확인할 수 있다.

스프링에서는 **템플릿 콜백 메서드를** 활용하여 반복을 제거하는 **트랜잭션 템플릿**을 제공한다.

### 트랜잭션 템플릿 사용 코드

```java
txTemplate.executeWithoutResult((status) -> {
     try {
					//비즈니스 로직
         bizLogic(fromId, toId, money);
     } catch (SQLException e) {
         throw new IllegalStateException(e);
     }
});
```

트랜잭션 템플릿 덕분에, 트랜잭션을 사용할 때 반복하는 코드를 제거할 수 있다. 하지만 이곳은 서비스 로직인데 비즈니스 로직 뿐만 아니라 트랜잭션을 처리하는 기술 로직이 함께 포함되어 있다.

### **트랜잭션 문제 해결** - **트랜잭션** AOP **이해**

<img width="720" alt="7" src="https://github.com/yj-leez/inflearn-spring-study/assets/77960090/846df0c4-e7fa-4521-abfa-23954d5e1008">



프록시를 사용하여 트랜잭션을 처리하는 객체와 비즈니스 로직을 처리하는 서비스 객체를 명확하게 분리할 수 있다. 스프링이 AOP를 통해 프록시를 도입해서 트랜잭션을 편리하게 처리해주는 `@Transactional`을 사용하자.

```java
@Transactional
public void accountTransfer(String fromId, String toId, int money) throws SQLException {
    bizLogic(fromId, toId, money);
}
```

이렇게 비즈니스 로직과 트랜잭션을 처리하는 로직을 분리할 수 있었다. 하지만 주의해야 할 점이 있는데, `@Transactional`은 스프링 AOP를 기반으로 동작하고, 스프링 AOP는 Spring container에 등록된 빈을 찾아서 사용하기 때문에 Service, transactionManager를 빈에 등록해야하고 Service에 주입되는 DataSource도 등록해야한다.
