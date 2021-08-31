package org.hxm.myspring.annotation;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Arrays;

public class MyPackagesAnnotationFilter implements MyAnnotationFilter{

    private final String[] prefixes;

    private final int hashCode;


    MyPackagesAnnotationFilter(String... packages) {
        Assert.notNull(packages, "Packages array must not be null");
        this.prefixes = new String[packages.length];
        for (int i = 0; i < packages.length; i++) {
            String pkg = packages[i];
            Assert.hasText(pkg, "Packages array must not have empty elements");
            this.prefixes[i] = pkg + ".";
        }
        Arrays.sort(this.prefixes);
        this.hashCode = Arrays.hashCode(this.prefixes);
    }


    @Override
    public boolean matches(String annotationType) {
        for (String prefix : this.prefixes) {
            if (annotationType.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return Arrays.equals(this.prefixes, ((MyPackagesAnnotationFilter) other).prefixes);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return "Packages annotation filter: " +
                StringUtils.arrayToCommaDelimitedString(this.prefixes);
    }
}
