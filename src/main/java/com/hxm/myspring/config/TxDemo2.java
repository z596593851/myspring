package com.hxm.myspring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TxDemo2 {

    @Autowired
    TxDemo txDemo;

    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor = Exception.class)
    public void test2() {
        System.out.println("test2 run...");
        txDemo.test1();
        System.out.println("test2 finish...");
    }
}
