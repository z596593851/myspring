package com.hxm.myspring.mydark;


import java.util.List;

public class DarkRuleConfig {
    private List<DarkFeatureConfig> features;

    public List<DarkFeatureConfig> getFeatures() {
        return this.features;
    }

    public void setFeatures(List<DarkFeatureConfig> features) {
        this.features = features;
    }

    public static class DarkFeatureConfig {
        private String key;
        private boolean enabled;
        private String rule;
        // 省略getter、setter方法

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getRule() {
            return rule;
        }

        public void setRule(String rule) {
            this.rule = rule;
        }
    }
}