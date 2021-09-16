package com.hxm.myspring.mydark;

public class Test {
    public static void main(String[] args) {
        DarkLaunch darkLaunch = new DarkLaunch();
        // 添加编程实现的灰度规则
        darkLaunch.addProgrammedDarkFeature("user_promotion", new UserPromotionDarkRule());
        IDarkFeature darkFeature = darkLaunch.getDarkFeature("user_promotion");
        System.out.println(darkFeature.enabled());
        System.out.println(darkFeature.dark(893));
    }
}
