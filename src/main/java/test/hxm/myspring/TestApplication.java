package test.hxm.myspring;

import org.hxm.myspring.annotation.MySpringBootApplication;
import org.hxm.myspring.factory.MySpringApplication;

@MySpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        MySpringApplication.run(TestApplication.class);
    }
}
