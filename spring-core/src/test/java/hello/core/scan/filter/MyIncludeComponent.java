package hello.core.scan.filter;

import java.lang.annotation.*;

@Target(ElementType.TYPE) // Type이라 하면 class에 붙는 것
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyIncludeComponent {
}
