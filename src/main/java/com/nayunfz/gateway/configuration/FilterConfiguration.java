package com.nayunfz.gateway.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * author: zhuo.shi
 * date: 2017/9/6
 */
@ConfigurationProperties(prefix = "zuul.filter")
public class FilterConfiguration {

    private String root;
    private Integer interval;

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }
}
