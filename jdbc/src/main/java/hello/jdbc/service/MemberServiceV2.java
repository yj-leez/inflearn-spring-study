package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
 */
@RequiredArgsConstructor
@Slf4j
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
                con.close();
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