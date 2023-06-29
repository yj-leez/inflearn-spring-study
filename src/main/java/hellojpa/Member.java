package hellojpa;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity  //JPA가 관리하는 객체, JPA 사용해서 테이블과 매핑할 객체임을 알려줌
//@Table(name = "MBR") 테이블 이름을 "MBR"로 지정. insert MBR 이렇게 인서트 쿼리 나감
//@SequenceGenerator(name = "member_seq_generator", sequenceName = "member_seq") 기본 키 매핑 - 자동생성 - sequence 전략 - sequence object
//@TableGenerator(
//        name = "MEMBER_SEQ_GENERATOR",
//        table = "MY_SEQUENCE",
//        pkColumnValue = "MEMBER_SEQ", allocationSize = 1)
public class Member extends BaseEntity{

    @Id //pk 매핑
    @GeneratedValue
    @Column(name = "MEMBER_ID")
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_seq_generator")
//    @GeneratedValue(strategy = GenerationType.TABLE, generator = "MEMBER_SEQ_GENERATOR")
    private Long id;

//    @Column(unique = true, length = 10) name 필수, 10자 초과x, 실행 로직에는 영향x
    @Column(name = "USERNAME")
    private String username;

    private int age;
    //period
    @Embedded
    private Period workPeriod;

    //address
    @Embedded
    private Address homeAddress;

    @ElementCollection
    @CollectionTable(name = "FAVORITE_FOOD", joinColumns =
            @JoinColumn(name = "MEMBER_ID")
    )
    @Column(name = "FOOD_NAME") //예외적으로 허용되는 것
    private Set<String> favoriteFoods = new HashSet<>();

//    @ElementCollection
//    @CollectionTable(name = "ADDRESS", joinColumns =
//            @JoinColumn(name = "MEMBER_ID")
//    )
//    private List<Address> addressHistory = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true) // 값타입으로 쓰는 것보다 일대다 관계 고려
    @JoinColumn(name = "MEMBER_ID")
    private List<AddressEntity> addressHistory = new ArrayList<>();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEAM_ID", insertable = false, updatable = false) // 일대다 양방향, 업뎃x, 공식적으로 존재x
    private Team team;

    @OneToOne
    @JoinColumn(name = "LOCKER_ID")
    private Locker locker;

    @OneToMany(mappedBy = "member")
    private List<MemberProduct> memberProducts = new ArrayList<>();


//    @Column(name = "TEAM_ID")
//    private Long teamId; 객체를 테이블에 맞추어 모델링

//    @ManyToOne
//    @JoinColumn(name = "TEAM_ID") //매핑할 외래키 컬럼
//    private Team team;

//    private Integer age;
//
//    @Enumerated(EnumType.STRING) // 기본 설정 ORDINAL인데 무조건 STRING 사용!!
//    private RoleType roleType;
//
//    @Temporal(TemporalType.TIMESTAMP)
//    private Date createDate;
//
//    @Temporal(TemporalType.TIMESTAMP)  //date, type, timestamp
//    private Date lastModifiedDate;
//    private LocalDate lastModifiedDate; 최신 버전에선 이것만 써도 됨
//
//    @Lob
//    private String description;

    public Member(){

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Set<String> getFavoriteFoods() {
        return favoriteFoods;
    }

    public void setFavoriteFoods(Set<String> favoriteFoods) {
        this.favoriteFoods = favoriteFoods;
    }

    public List<AddressEntity> getAddressHistory() {
        return addressHistory;
    }

    public void setAddressHistory(List<AddressEntity> addressHistory) {
        this.addressHistory = addressHistory;
    }

    public Address getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = homeAddress;
    }

    //    public Team getTeam() {
//        return team;
//    }
//
//    /**
//     *연관관계 편의 메소드
//     */
//    public void changeTeam(Team team) {
//        this.team = team;
//        team.getMembers().add(this);
//    }

}
