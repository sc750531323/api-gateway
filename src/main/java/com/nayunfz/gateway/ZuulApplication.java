package com.nayunfz.gateway;

import com.nayunfz.gateway.configuration.FilterConfiguration;
import com.netflix.zuul.FilterFileManager;
import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.groovy.GroovyCompiler;
import com.netflix.zuul.groovy.GroovyFileFilter;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

/**
 * Created by Administrator on 2017/9/4.
 */
@SpringCloudApplication
@EnableZuulProxy
@EnableConfigurationProperties(value = {FilterConfiguration.class})
public class ZuulApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ZuulApplication.class).web(true).run(args);
    }

    /**
     * API 网关服务动态加载过滤器，每隔${interval}秒，从API网关服务所在位置的 ${root}/pre、${root}/post 目录下获取Groovy定义的过滤器，并对其进行编译和动态加载使用。
     */
    @Bean
    public FilterLoader filterLoader(FilterConfiguration filterConfiguration){
        FilterLoader filterLoader = FilterLoader.getInstance();
        filterLoader.setCompiler(new GroovyCompiler());
        FilterFileManager.setFilenameFilter(new GroovyFileFilter());
        try {
            FilterFileManager.init(
                    filterConfiguration.getInterval(),
                    filterConfiguration.getRoot() + "/pre",
                    filterConfiguration.getRoot() + "/post");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filterLoader;
    }
}
