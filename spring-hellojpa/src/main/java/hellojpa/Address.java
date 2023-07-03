package hellojpa;

import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class Address {
    private String city;
    private String street;
    private String zipcode;


    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }

    public Address(){

    }

    public String getCity() {
        return city;
    } //여러 엔티티에서 공유하지 못하도록 불변 객체로 설계해야함 -> 생성자 생성, setter 삭제

    public String getStreet() {
        return street;
    }

    public String getZipcode() {
        return zipcode;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(city, address.city) &&
                Objects.equals(street, address.street) &&
                Objects.equals(zipcode, address.zipcode);
    }

    @Override //equals 구현하면 hashCode도 알맞게 구현해야함. hash사용하는 hashMap등을 자바컬렉션에서 효율적으로 사용함.천
    public int hashCode(){
        return Objects.hash(city, street, zipcode);
    }

}
