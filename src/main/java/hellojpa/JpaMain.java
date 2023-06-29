package hellojpa;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            Member member = new Member();
            member.setUsername("Jo");
            member.setAge(22);
            member.setHomeAddress(new Address("HomeCity", "street","10000"));
            em.persist(member);

            Team team = new Team();
            team.setName("Hongik");
            em.persist(team);
            member.setTeam(team);

            em.flush();
            em.clear();

//            String query = "select m from Member m inner join m.team t"; //이너 조인
//            String query = "select m from Member m left outer join m.team t"; //외부 조인
            String query = "select m from Member m, Team t where m.username = t.name"; //세타 조인
            List<Member> resultList = em.createQuery(query, Member.class)
                    .getResultList();


            tx.commit();


//            /**
//             * 스칼라타입 프로젝션 여러 값 조회
//             */
//            List<MemberDTO> anotherResult = em.createQuery("select new hellojpa.MemberDTO(m.username, m.age) from Member m", MemberDTO.class)
//                    .getResultList();
//
//            MemberDTO memberDTO = anotherResult.get(0);
//            System.out.println("memberDTO = " + memberDTO.getUsername());


//            TypedQuery<Member> query1 = em.createQuery("select m from Member m where m.username = :username", Member.class);
//            query1.setParameter("username", "Jo");
//            Member result = query1.getSingleResult();
//            System.out.println("result = " + result.getUsername());


//                        //Criteria 사용 준비, 자바 표준 제공 문법
//            CriteriaBuilder cb = em.getCriteriaBuilder();
//            CriteriaQuery<Member> query = cb.createQuery(Member.class);
//            //루트 클래스 (조회를 시작할 클래스)
//            Root<Member> m = query.from(Member.class);
//            //쿼리 생성
//            CriteriaQuery<Member> cq = query.select(m).where(cb.equal(m.get("username"), "kim"));
//            List<Member> resultList = em.createQuery(cq).
//                    getResultList();


//            member.getFavoriteFoods().add("chicken");
//            member.getFavoriteFoods().add("족발");
//            member.getFavoriteFoods().add("pizza");
//
//            member.getAddressHistory().add(new AddressEntity("old1", "street", "1000"));
//            member.getAddressHistory().add(new AddressEntity("old2", "street", "1000"));
//
//            em.persist(member);
//            em.flush();
//            em.clear();
//
//            //homeCity -> newCity
//            System.out.println(" ============================== ");
//            Member findMember = em.find(Member.class, member.getId());
//            Address a = findMember.getHomeAddress();
//            findMember.setHomeAddress(new Address("newCity", a.getStreet(), a.getZipcode()));
//
//            //chicken -> Korean food
//            findMember.getFavoriteFoods().remove("chicken");
//            findMember.getFavoriteFoods().add("Korean food");


//            findMember.getAddressHistory().remove(new Address("old1", "street", "1000"));
//            findMember.getAddressHistory().add(new Address("newCity1", "street", "1000"));

        } catch (Exception e){
            tx.rollback();
        }finally {
            em.close();
        }
        emf.close();


    }

}
