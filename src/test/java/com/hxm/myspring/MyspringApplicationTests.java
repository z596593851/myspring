package com.hxm.myspring;

import com.hxm.myspring.config.TxDemo2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MyspringApplicationTests {

    @Autowired
    TxDemo2 txDemo2;

    @Test
    void contextLoads() {
        txDemo2.test2();
    }

}
