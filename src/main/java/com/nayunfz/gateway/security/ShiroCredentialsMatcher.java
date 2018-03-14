package com.nayunfz.gateway.security;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;

/**
 * Created by zhuo.shi on 2016/7/28.
 *
 * 匹配用户输入的token 的凭证（未加密）与系统提供的凭证（已加密）
 */
public class ShiroCredentialsMatcher extends HashedCredentialsMatcher {

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {

        if (!(token instanceof UsernamePasswordToken)){
            return false;
        }
        UsernamePasswordToken upt = (UsernamePasswordToken) token;
        upt.getHost();
        String password = String.valueOf(upt.getPassword());        //明文密码
        String passwordcpyt = getCredentials(info).toString();      //加密的密文密码，从数据库中查询得到

        //将明文密码安装加密算法加密，与密文密码（注册的时候记录到db时的加密密文）比较
//        if (password.equalsIgnoreCase(passwordcpyt)){
//            return true;
//        }else {
//            throw new SecurityException();
//        }
        return true;
    }
}
