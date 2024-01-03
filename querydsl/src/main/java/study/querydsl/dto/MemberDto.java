package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;
import study.querydsl.entity.Member;

@Data
@NoArgsConstructor // 기본 생성자 만들어줘야 queryDsl 방식 사용할 수 있음
public class MemberDto {
    private String username;
    private int age;

    @QueryProjection
    public MemberDto(String username, int age){
        this.username = username;
        this.age = age;
    }
}
