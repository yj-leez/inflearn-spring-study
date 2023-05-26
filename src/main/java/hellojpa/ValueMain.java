package hellojpa;

public class ValueMain {
    Address address1 = new Address("city", "street", "1000");
    Address address2 = new Address("city", "street", "1000");

//    System.out.println("address1 == address2" + (address1 == address2)); false 나옴
//    System.out.println("address1 equals address2" + (address1.equals(address2))); true 나옴. 오버라이딩 하기 전엔 비교가 ==이라 false나옴.


}
