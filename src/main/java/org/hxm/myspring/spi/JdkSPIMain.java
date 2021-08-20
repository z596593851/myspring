package org.hxm.myspring.spi;

import java.util.ServiceLoader;

public class JdkSPIMain {
    public static void main(String[] args) {
        ServiceLoader<IShout> shouts = ServiceLoader.load(IShout.class);
        for (IShout s : shouts) {
            s.shout();
        }
    }
}
