package com.hxm.myspring.mydark;


public interface IDarkFeature {
    boolean enabled();
    boolean dark(long darkTarget);
    boolean dark(String darkTarget);
}
