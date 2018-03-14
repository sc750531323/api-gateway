package com.nayunfz.gateway.configuration;

import com.nayunfz.gateway.security.ShiroCacheManager;
import com.nayunfz.gateway.security.ShiroCredentialsMatcher;
import com.nayunfz.gateway.security.ShiroRealm;
import com.nayunfz.gateway.security.ShiroSessionDAO;
import org.apache.shiro.session.mgt.SimpleSessionFactory;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by zhuo.shi on 2018/1/17.
 */
@Configuration(value = "shiro")
@ConfigurationProperties(prefix = "spring.shiro")
public class ShiroConfiguration {

    private Integer rememberMeCookieMaxAge;
    private Integer sessionIdCookieMaxAge;
    private Integer sessionTimeout;
    private Integer cacheTimeout;

    @Bean
    public ShiroFilterFactoryBean shirFilter(DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        //拦截器
        Map<String,String> filterChainDefinitionMap = new LinkedHashMap<>();
        // 配置不会被拦截的链接 顺序判断
        filterChainDefinitionMap.put("/static/**", "anon");
        // 配置退出 过滤器,其中的具体的退出代码Shiro已经替我们实现了
        filterChainDefinitionMap.put("/logout", "logout");
        // 过滤链定义，从上向下顺序执行，一般将/**放在最为下边
        // authc:所有url都必须认证通过才可以访问; anon:所有url都都可以匿名访问
//        filterChainDefinitionMap.put("/**", "authc");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        // 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面
        shiroFilterFactoryBean.setLoginUrl("/login");
        // 登录成功后要跳转的链接
        shiroFilterFactoryBean.setSuccessUrl("/index");
        // 未授权界面;
        shiroFilterFactoryBean.setUnauthorizedUrl("/403");
        return shiroFilterFactoryBean;
    }

    @Bean
    public DefaultWebSecurityManager securityManager(DefaultWebSessionManager sessionManager,
                                                     ShiroRealm realm, ShiroCacheManager cacheManager,
                                                     CookieRememberMeManager cookieRememberMeManager){
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setSessionManager(sessionManager);
        securityManager.setRealm(realm);
        securityManager.setCacheManager(cacheManager);
        securityManager.setRememberMeManager(cookieRememberMeManager);
        return securityManager;
    }

    @Bean
    public SimpleCookie rememberMeCookie(){
        SimpleCookie cookie = new SimpleCookie("rememberMe");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(rememberMeCookieMaxAge);
        return cookie;
    }

    @Bean
    public CookieRememberMeManager rememberMeManager(SimpleCookie rememberMeCookie){
        CookieRememberMeManager rememberMeManager = new CookieRememberMeManager();
        rememberMeManager.setCookie(rememberMeCookie);
        //<!-- cipherKey是加密rememberMeCookie的密钥；默认AES算 -->
        rememberMeManager.setCipherKey(org.apache.shiro.codec.Base64.decode("4AvVhmFLUs0KTA3Kprsdag=="));
        return rememberMeManager;
    }

    @Bean
    public SimpleCookie sessionIdCookie(){
        SimpleCookie cookie = new SimpleCookie("sid");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(sessionIdCookieMaxAge);
        //仅在 HTTPS 安全通信时才会发送 Cookie
        //cookie.setSecure(true);
        //域名
        //cookie.setDomain("www.xxx.com");
        //设置Cookie 名字，默认为JSESSIONID
        cookie.setName("token");
        //设置Cookie 的路径，默认空，即存储在域名根下
        cookie.setPath("/");
        return cookie;
    }

    @Bean
    public DefaultWebSessionManager sessionManager(RedisTemplate<String, byte[]> sessionRedisTemplate,
                                                   SimpleCookie sessionIdCookie){
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        ShiroSessionDAO sessionDAO = new ShiroSessionDAO(sessionRedisTemplate, sessionTimeout);
        sessionDAO.setSessionIdGenerator(new JavaUuidSessionIdGenerator());
        sessionManager.setSessionDAO(sessionDAO);
        sessionManager.setSessionFactory(new SimpleSessionFactory());
        sessionManager.setGlobalSessionTimeout(3600000);//session 有效时间为一个小时，单位（毫秒）
        sessionManager.setSessionIdCookieEnabled(true);
        sessionManager.setSessionIdCookie(sessionIdCookie);
        sessionManager.setSessionValidationSchedulerEnabled(false);
        return sessionManager;
    }

    @Bean
    public ShiroCacheManager shiroCacheManager(RedisTemplate<String, byte[]> redisTemplate){
        ShiroCacheManager shiroCacheManager = new ShiroCacheManager();
        shiroCacheManager.setRedisTemplate(redisTemplate);
        shiroCacheManager.setCacheTimeout(cacheTimeout);
        return shiroCacheManager;
    }

    @Bean
    public ShiroCredentialsMatcher credentialsMatcher(){
        ShiroCredentialsMatcher matcher = new ShiroCredentialsMatcher();
        matcher.setHashAlgorithmName("MD5");
        return matcher;
    }

    @Bean
    public ShiroRealm shiroRealm(ShiroCredentialsMatcher credentialsMatcher){
        ShiroRealm realm = new ShiroRealm();
        realm.setCredentialsMatcher(credentialsMatcher);
        return realm;
    }

    @Bean("sessionRedisTemplate")
    public RedisTemplate<String, byte[]> sessionRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, byte[]> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    public Integer getRememberMeCookieMaxAge() {
        return rememberMeCookieMaxAge;
    }

    public void setRememberMeCookieMaxAge(Integer rememberMeCookieMaxAge) {
        this.rememberMeCookieMaxAge = rememberMeCookieMaxAge;
    }

    public Integer getSessionIdCookieMaxAge() {
        return sessionIdCookieMaxAge;
    }

    public void setSessionIdCookieMaxAge(Integer sessionIdCookieMaxAge) {
        this.sessionIdCookieMaxAge = sessionIdCookieMaxAge;
    }

    public Integer getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Integer sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public Integer getCacheTimeout() {
        return cacheTimeout;
    }

    public void setCacheTimeout(Integer cacheTimeout) {
        this.cacheTimeout = cacheTimeout;
    }
}
