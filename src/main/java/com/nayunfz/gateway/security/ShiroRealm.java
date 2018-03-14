package com.nayunfz.gateway.security;

import com.alibaba.fastjson.JSON;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

public class ShiroRealm extends AuthorizingRealm {

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) { //表示根据用户身份获取授权信息

        String username = (String)principalCollection.getPrimaryPrincipal();
        //从数据库中查询出用户的Roles，Permissions

        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        System.out.println("---doGetAuthorizationInfo " + JSON.toJSON(authorizationInfo));
        authorizationInfo.addRole("admin");
//        authorizationInfo.addObjectPermission();

        return authorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)       //表示获取身份验证信息，登录验证
            throws AuthenticationException {

         String username = (String) token.getPrincipal();
         String password = new String((char[])token.getCredentials());  //用户输入的password

         //根据用户名查询数据库，得到password
         String pasword_f_db = "";

         SimpleAuthenticationInfo sac =
                new SimpleAuthenticationInfo(username, password.toCharArray(), getName());
         //通过SimpleAuthenticationInfo 的credentialsSalt设置盐，HashedCredentialsMatcher 会自动识别这个盐
         String salt = username + new SecureRandomNumberGenerator().nextBytes().toHex();
         sac.setCredentialsSalt(ByteSource.Util.bytes(salt));
         return sac;
    }
}
