# 스프링 핵심 원리 - 기본편

### 회원 도메인 설계

- 회원 도메인 요구사항
    - 회원을 가입하고 조회할 수 있다.
    - 회원은 일반과 VIP 두 가지 등급이 있다.
    - 회원 데이터는 자체 DB를 구축할 수 있고, 외부 시스템과 연동할 수 있다.
<img width="659" alt="1" src="https://github.com/yj-leez/inflearn_spring_study/assets/77960090/55b3773f-ec8e-4901-b981-08adef5b4456">



### 회원 도메인 개발

- 회원 등급  enum `/main/java/hello/core/member/Grade`
- 회원 엔티티 `/main/java/hello/core/member/Member`
- 회원 저장소 인터페이스 `/main/java/hello/core/member/MemberService`
- 메모리 회원 저장소 구현체 - 데이터베이스가 아직 확정이 안된 상태라 가장 단순한 메모리 회원 저장소를 사용해 개발을 진행하자. `/main/java/hello/core/member/MemberServiceImpl`
- 회원 서비스 인터페이스 `/main/java/hello/core/member/MemberService`
- 회원 서비스 구현체 `/main/java/hello/core/member/MemberServiceImpl`
- 회원 가입 테스트 `/test/java/hello/core/member/MemberServiceTest`
    
    ```java
    import static org.junit.jupiter.api.Assertions.*;
    Assertions.assertThat(member).isEqualTo(findMember);
    ```
    

### 주문과 할인 도메인 설계

- 주문과 할인 정책
    - 회원은 상품을 주문할 수 있다.
    - 회원 등급에 따라 할인 정책을 적용할 수 있다.
    - 할인 정책은 모든 VIP는 1000원을 할인해주는 고정 금액 할인을 적용해달라. (나중에 변경 될 수 있다.)
    - 할인 정책은 변경 가능성이 높다. 회사의 기본 할인 정책을 아직 정하지 못했고, 오픈 직전까지 고민을 미루고 싶다. 최악의 경우 할인을 적용하지 않을 수 도 있다. (미확정)

<img width="653" alt="2" src="https://github.com/yj-leez/inflearn_spring_study/assets/77960090/2144c9f0-b5ea-48b9-b23d-4c887429b8eb">
<img width="673" alt="3" src="https://github.com/yj-leez/inflearn_spring_study/assets/77960090/ea2a6704-ffd9-4ff1-b9d5-e24e23c1ff4d">



### 주문과 할인 도메인 개발

- 할인 정책 인터페이스 `/main/java/hello/core/discount/DiscountPolicy`
- 정액 할인 정책 구현체 `/main/java/hello/core/discount/FixDiscountPolicy`
- 정률 할인 정책 구현체 `/main/java/hello/core/discount/RateDiscountPolicy`
- 주문 엔티티 `/main/java/hello/core/order/Order`
- 주문 서비스 인터페이스 `/main/java/hello/core/order/OrderService`
- 주문 서비스 구현체 `/main/java/hello/core/order/OrderServiceImpl`
- 주문과 할인 정책 테스트 `/test/java/hello/core/order/OrderServiceTest`

### 새로운 할인 정책 적용의 문제점

- 주문서비스 클라이언트(`OrderServiceImpl`)는 언뜻 보면 할인 정책 인터페이스에 의존하면서 DIP를 지킨 것 같지만 추상(인터페이스)뿐만 아니라 구체(구현) 클래스에도 의존하고 있다.
    
    → 추상(인터페이스) 의존: `DiscountPolicy`
    
         구체(구현) 클래스: `FixDiscountPolicy`, `RateDiscountPolicy`
    

### 관심사의 분리

- 공연을 구성하고, 담당 배우를 섭외하고, 지정하는 책임을 담당하는 별도의 공연 기획자가 나올 시점
    
    → 공연 기획자 AppConfig 등장
    
- AppConfig는 애플리케이션의 전체 동작 방식을 구성(config)하기 위해, 구현 객체를 생성하고, 연결하는 책임
- 이제부터 클라이언트 객체는 자신의 역할을 실행하는 것만 집중, 권한이 줄어듬(책임이 명확해짐)
<img width="673" alt="4" src="https://github.com/yj-leez/inflearn_spring_study/assets/77960090/0f1172ee-b722-4c48-9341-0db52fa05288">



- AppConfig처럼 객체를 생성하고 관리하면서 의존관계를 설정해주는 것을 IoC 컨테이너 혹은 DI 컨테이너라고 한다.
- `/main/java/hello/core/AppConfig`

### AppConfig 리펙터링

- 역할에 따른 구현이 잘 보이도록 수정

### AppConfig 스프링 기반으로 변경

- 스프링 컨테이너 = ApplicationContext
- 스프링 컨테이너는 @Configuration 이 붙은 AppConfig를 설정정보로 사용한다. 여기서 @Bean 이라 적힌 메서드를 모두 호출해서 반환된 객체를 스프링 컨테이너에 등록한다. 이렇게 스프링 컨테이너에 등록된 객체를 스프링 빈이라 한다.

### 스프링 컨테이너 생성

- 스프링 컨테이너는 xml 기반으로 만들 수 있고, 애노테이션 기반의 자바 설정 클래스로 만들 수 있다.
- 컨테이너 생성과정
    
    스프링 컨테이너 생성 → 스프링 빈 등록 → 스프링 빈 의존관계 설정 준비 → 의존관계 설정 완료
    
    - 스프링은 빈을 생성하고, 의존관계를 주입하는 단계가 나누어져 있다.

### 스프링 빈 조회

- 스프링에 등록된 모든 빈 이름 조회
    
    ```java
    ac.getBeanDefinitionNames()
    ```
    
- 스프링 빈 찾는 가장 기본적인 조회 방법
    
    ```java
    ac.getBean(빈이름, 타입);
    ac.getBean(타입);
    ```
    
- 부모 타입으로 조회 시, 자식 타입도 함께 조회한다. 모든 자바 객체의 최고 부모인 `Object.class`로 조회하면 모든 스프링 빈을 조회한다.

### ApplicationContext

- ApplicationContext는 BeanFactory의 기능을 상속받는다. 여기서 getBean을 제공한다.
- ApplicationContext는 빈 관리 기능 + 편리한 부가기능을 제공한다.

<img width="653" alt="5" src="https://github.com/yj-leez/inflearn_spring_study/assets/77960090/42ea8e7a-34d5-45d2-9d2a-f7a9af5ba7a6">

### 싱글톤패턴

- 스프링 없는 순수한 DI 컨테이너인 AppConfig는 요청을 할 때마다 객체를 새로 생성한다.
- 싱글톤 패턴: 클래스의 인스턴스가 딱 1개만 생성되는 것을 보장하는 디자인 패턴이다.
    - private 생성자를 사용해서 외부에서 임의로 new 키워드를 사용하지 못하도록 막아야 한다.

```java
//1. static 영역에 객체를 딱 1개만 생성해둔다.
private static final SingletonService instance = new SingletonService();

//2. public으로 열어서 객체 인스턴스가 필요하면 이 static 메서드를 통해서만 조회하도록 허용한다.
public static SingletonService getInstance() {
          return instance;
}

//3. 생성자를 private으로 선언해서 외부에서 new 키워드를 사용한 객체 생성을 못하게 막는다. 
private SingletonService() {
}
```

- 하지만 문제점들이 있다.

### 싱글톤 컨테이너

- 스프링 컨테이너는 싱글톤 패턴의 문제점을 해결하면서, 객체 인스턴스를 싱글톤(1개만 생성)으로 관리한다.
- 스프링 컨테이너 덕분에 고객의 요청이 올 때 마다 객체를 생성하는 것이 아니라, 이미 만들어진 객체를 공유해서 효율적으로 재사용할 수 있다.

<aside>
💡 주의점 ! 싱글톤 객체는 상태를 유지(stateful)하게 설계하면 안된다. 특정 클라이언트에 의존적인 필드가 없도록 무상태(stateless)로 설계해야한다. 스프링 빈의 필드에 공유 값을 설정하면 정말 큰 장애가 발생할 수 있다!!!

</aside>

> 문제점 발생
> 
> 
> <aside>
> 💡 @Configuration과 @Bean의 조합에도 불구하고 OrderServiceImpl에서 가져 온 Repository와 MemberServiceImpl에서 가져 온 Repository가 다르게 로그에 찍혔다.
> 
> </aside>
> 
> @Configuration과 @Bean의 조합으로 싱글톤을 보장하는 경우는 정적이지 않은 메서드일 때이다.
> 
> 정적 메서드에 @Bean을 사용하게 되면 싱글톤 보장을 위한 지원을 받지 못한다.
> 
> 해당 내용에 대한 상세는 아래 링크의 Bootstrapping 섹션을 참고하면 된다.
> 
> [https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/annotation/Bean.html](https://docs.spring.io/spring-framework/docs/6.0.x/javadoc-api/org/springframework/context/annotation/Bean.html)
> 
> By marking this method as `static`, it can be invoked without causing instantiation of its declaring `@Configuration` class, thus avoiding the above-mentioned lifecycle conflicts. Note however that `static` `@Bean` methods will not be enhanced for scoping and AOP semantics as mentioned above.
> 

### @Configuration과 바이트코드 조작의 마법

- `AnnotationConfigApplicationContext`에 파라미터로 넘긴 값도 스프링 빈으로 등록된다. 하지만 `AppConfig` 클래스 자체가 등록되는 것이 아니라, 스프링이 CGLIB라는 바이트코드 조작 라이브러리를 사용해서 AppConfig 클래스를 상속받은 임의의 다른 클래스를 만들고, 그 다른 클래스를 스프링 빈으로 등록한다.
- 이 임의의 다른 클래스가 싱글톤이 보장되도록 해준다. @Bean이 붙은 메서드마다 이미 스프링 빈이 존재하면 존재하는 빈을 반환하고, 스프링 빈이 없으면 생성해서 스프링 빈으로 등록하고 반환하는 코드가 동적으로 만들어진다.
- `@Configuration` 없이 빈을 등록하면 싱글톤이 깨진다!

### 컴포넌트 스캔과 의존관계 자동주입

- 스프링은 설정 정보가 없어도 자동으로 스프링 빈을 등록하는 컴포넌트 스캔이라는 기능을 제공한다. 컴포넌트 스캔은 이름 그대로 @Component 애노테이션이 붙은 클래스를 스캔해서 스프링 빈으로 등록한다. @AutoWired를 생성자에게 붙여주어 의존관계를 자동으로 주입해준다.

```java
@Component
public class MemberServiceImpl implements MemberService {
  private final MemberRepository memberRepository;

  @Autowired
  public MemberServiceImpl(MemberRepository memberRepository) {
      this.memberRepository = memberRepository;
  }
}
```

- 동작방식: @ComponentScan → @Autowired 의존관계 자동주입(기본 조회 전략은 타입이 같은 빈을 찾아서 주입)

### 탐색 위치와 기본 스캔 대상

- 컴포넌트 스캔은 `@Component`뿐만 아니라, `@Controller`, `@Service`, `@Repository`, `@Configuration`을 대상으로 한다.
- 필터: `includeFilters`, `excludeFilters`

### 중복 등록과 충돌

- 자동 빈 등록 vs 자동 빈 등록 → ConflictingBeanDefinitionException 예외 발생
- 수동 빈 등록 vs 자동 빈 등록 → 수동 빈이 자동 빈을 오버라이딩 함 → 최근에는 오류가 발생하도록 스프링이 기본 값을 바꿈

### 다양한 의존관계 주입 방법

- 생성자 주입: 불변, 필수인 의존관계에 사용

```java
@Component
  public class OrderServiceImpl implements OrderService {
      private final MemberRepository memberRepository; //무조건 값이 있어야함 = final

      @Autowired
      public OrderServiceImpl(MemberRepository memberRepository) {
          this.memberRepository = memberRepository;
      }
}
```

- 수정자 주입(setter): 선택, 변경가능성이 있는 의존관계에 사용

```java
@Component
  public class OrderServiceImpl implements OrderService {
      private MemberRepository memberRepository;

      @Autowired
        public void setMemberRepository(MemberRepository memberRepository) {
            this.memberRepository = memberRepository;
        }
}
```

- 필드 주입: 애플리케이션의 실제 코드와 관계 없는 테스트 코드, 스프링 설정을 목적으로 하는 @Configuration 같은 곳에서만 특별한 용도로 사용

```java
@Component
    public class OrderServiceImpl implements OrderService {
        @Autowired
        private MemberRepository memberRepository;
}
```

- 일반 메서드 주입: 딱히 잘 사용 x, 일반 메서드 통해서 사용
- 요즘에는 스프링을 포함한 DI 프레임워크 대부분이 생성자 주입을 권장한다.
    - lombok 라이브러리를 사용하면 `@RequiredArgsConstructor` 기능을 사용하여 final이 붙은 필드를 모아서 생성자를 자동으로 만들어준다.

### 조회 빈이 2개 이상- 문제

> @Autowired는 타입으로 조회하기 때문에, 선택된 빈이 2개 이상일 때 문제가 발생한다. 해결 방법은 세 가지가 있다.
> 
- @Autowired 필드명 매치: 필드명을 빈 이름으로 바꾼다.
- @Qualifier: 빈 등록시 `@Qualifier("등록한 이름")`을 붙여주고 주입 시 `@Qualifier("등록한 이름")`을 붙여준다.
- @Primary: @Autowired 사용 시 여러 빈이 매칭되면 `@Primary`를 가진 빈이 우선권을 가진다.
- 메인 데이터베이스의 커넥션을 획득하는 스프링 빈을 @Primary를 사용하고, 서브 데이터베이스 커넥션 빈을 획득할 때는 @Qualifier를 지정해서 명시적으로 획득하는 방식으로 사용하는 것을 추천한다.
- 애노테이션을 직접 만들어서 해결할 수 도 있다.

<aside>
💡 정리
업무 로직 빈은 자동 빈 등록을 사용하고, 기술 지원 빈은 수동 빈 등록을 사용하는 것을 권장한다.

</aside>

## 빈 생명주기 콜백

## 빈 스코프
