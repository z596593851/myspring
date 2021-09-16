package org.hxm.myspring;

import org.hxm.myspring.annotation.MyComponentScan;
import org.hxm.myspring.annotation.MyConfiguration;
import org.hxm.myspring.annotation.MyEnableAutoConfiguration;
import org.hxm.myspring.factory.MySpringApplication;

@MyConfiguration
@MyEnableAutoConfiguration
@MyComponentScan
public class TestApplication {

    public static void main(String[] args) {
        MySpringApplication.run(TestApplication.class);
    }
}
