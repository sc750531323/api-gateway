package com.nayunfz.gateway.security;

import com.alibaba.fastjson.JSON;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.ValidatingSession;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.SerializationUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * shiro Session管理器 CRUD
 */
public class ShiroSessionDAO extends AbstractSessionDAO {

    private static final transient Logger logger = LoggerFactory.getLogger(ShiroSessionDAO.class);

    private RedisTemplate<String, byte[]> redisTemplate;
    private int sessionTimeout;

    public ShiroSessionDAO(RedisTemplate<String, byte[]> redisTemplate, int sessionTimeout){
        this.redisTemplate = redisTemplate;
        this.sessionTimeout = sessionTimeout;
    }

    private static final String SHIRO_SESSION_PREFIX = "shiro_session:";

    private void saveSession(Session session){
        if (session == null || session.getId() == null){
            return;
        }
        String key = (SHIRO_SESSION_PREFIX + session.getId().toString());
        redisTemplate.opsForValue().set(
                key, SerializationUtils.serialize(session));
        redisTemplate.expire(key, sessionTimeout, TimeUnit.SECONDS);
    }

    /**
     * 如果自定义了SessionFactory，这里要转化为自定义的Session
     */
//    protected void assignSessionId(Session session, Serializable sessionId) {
//        ((ShiroSession) session).setId(sessionId);
//    }

    @Override
    protected Serializable doCreate(Session session) {

        Serializable sessionId = super.getSessionIdGenerator().generateId(session);
        assignSessionId(session, sessionId);
        saveSession(session);

        logger.debug("shiro create session : " + session);
        return session.getId();
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        logger.debug("shiro read session: id=" + sessionId);
        if (sessionId == null){
            return null;
        }
        try {
            Session session = (Session) SerializationUtils.deserialize(redisTemplate.opsForValue().get(SHIRO_SESSION_PREFIX + sessionId.toString()));
            logger.debug("session= " + JSON.toJSONString(session));
            return session;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        logger.debug("shiro update session: " + JSON.toJSONString(session));
        if(session instanceof ValidatingSession && !((ValidatingSession)session).isValid()) {
            return; //如果会话过期或者停止 没必要再更新了
        }
        saveSession(session);
    }

    @Override
    public void delete(Session session) {
        logger.debug("shiro del session: " + JSON.toJSONString(session));
        if (session == null || session.getId() == null){
            return;
        }
        redisTemplate.delete((SHIRO_SESSION_PREFIX + session.getId().toString()));
    }

    /**
     * shiro配置的sessionValidationScheduler，定时调用此方法，检测活跃的session
     */
    @Override
    public Collection<Session> getActiveSessions() {
        logger.debug("---getActiveSessions()---");
        Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());
        List<byte[]> values = redisTemplate.opsForValue().multiGet(redisTemplate.keys(SHIRO_SESSION_PREFIX + "*"));
        for (byte[] value : values){
            Session session = null;
            logger.debug("shiro get activie session: " + SerializationUtils.deserialize(value));
            try {
                session = (Session) SerializationUtils.deserialize(value);
                sessions.add(session);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return sessions;
    }
}
