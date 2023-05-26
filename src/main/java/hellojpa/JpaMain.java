package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
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
            member.setHomeAddress(new Address("HomeCity", "street","10000"));

            member.getFavoriteFoods().add("chicken");
            member.getFavoriteFoods().add("족발");
            member.getFavoriteFoods().add("pizza");

            member.getAddressHistory().add(new AddressEntity("old1", "street", "1000"));
            member.getAddressHistory().add(new AddressEntity("old2", "street", "1000"));

            em.persist(member);
            em.flush();
            em.clear();

            //homeCity -> newCity
            System.out.println(" ============================== ");
            Member findMember = em.find(Member.class, member.getId());
            Address a = findMember.getHomeAddress();
            findMember.setHomeAddress(new Address("newCity", a.getStreet(), a.getZipcode()));

            //chicken -> Korean food
            findMember.getFavoriteFoods().remove("chicken");
            findMember.getFavoriteFoods().add("Korean food");


//            findMember.getAddressHistory().remove(new Address("old1", "street", "1000"));
//            findMember.getAddressHistory().add(new Address("newCity1", "street", "1000"));



            tx.commit();
        } catch (Exception e){
            tx.rollback();
        }finally {
            em.close();
        }
        emf.close();


    }

}
