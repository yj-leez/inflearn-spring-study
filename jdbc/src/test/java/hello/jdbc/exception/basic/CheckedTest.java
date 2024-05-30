package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class CheckedTest {

    @Test
    void checked_catch(){
        Service service = new Service();
        Assertions.assertThatThrownBy(() -> service.callCatch())
                .isInstanceOf(MyCheckedException.class);
    }

    @Test
    void checked_throw() throws MyCheckedException {
        Service service = new Service();
        service.callThrow();
    }

    /**
     * Exception을 상속 받은 예외는 체크 예외가 된다.
     */
    static class MyCheckedException extends Exception{
        public MyCheckedException(String message){
            super(message);
        }
    }

    /**
     * 체크 예외는
     * 예외를 잡아서 처리하거나, 던지거나 둘 중 하나를 필수로 해야한다.
     */
    static class Service {
        Repository repository = new Repository();

        /**
         * 예외를 잡아서 처리하는 코드
         */
        public void callCatch() {
            try {
                repository.call();
            } catch (MyCheckedException e){
                log.info("예외 처리, message = {}", e.getMessage(), e);
                /**
                 * e의 내용
                 * hello.jdbc.exception.basic.CheckedTest$MyCheckedException: ex
                 * 	at hello.jdbc.exception.basic.CheckedTest$Repository.call(CheckedTest.java:46)
                 * 	at hello.jdbc.exception.basic.CheckedTest$Service.callCatch(CheckedTest.java:36)
                 * 	at hello.jdbc.exception.basic.CheckedTest.checked_catch(CheckedTest.java:12)
                 * 	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
                 */
            }
        }

        /**
         * 체크 예외를 밖으로 던지는 코드
         * 체크 예외는 예외를 잡지 않고 밖으로 던지려면 throws 예외를 메서드에 필수로 선언해야한다.
         */
        public void callThrow() throws MyCheckedException {
            repository.call();
        }

    }

    static class Repository{
        public void call() throws MyCheckedException {
            throw new MyCheckedException("ex"); // 체크 예외를 잡지 않는다면 무조건 던짐을 명시 해야한다. 이것을 컴파일러가 체크한다.
        }
    }

}
