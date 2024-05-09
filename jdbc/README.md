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
