package com.hxm.myspring.test;

import java.util.HashMap;
import java.util.Map;

public class MyTest {
    public static void main(String[] args) {
        Map<Integer,Integer> map=new HashMap<>();
        map.put(0,2);
        map.put(null,1);
        System.out.println(map.get(null));
    }
}
