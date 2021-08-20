package com.hxm.myspring.config;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TxDemo {

    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor = Exception.class)
    public void test1() {
        System.out.println("test1 run...");
        int i = 1 / 0;
        System.out.println("test1 finish...");
    }
}
