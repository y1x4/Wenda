package com.yixuwang.service;

import com.yixuwang.util.JedisAdapter;
import com.yixuwang.util.RedisKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 *
 * Created by yixu on 2018/6/30.
 */
@Service
public class FollowService {

    @Autowired
    JedisAdapter jedisAdapter;

    //用户关注了某个实体，用户、问题、评论等
    public boolean follow(int userId, int entityType, int entityId) {
        String followerKey = getFollowerKey(entityType, entityId);
        String followeeKey = getFolloweeKey(userId, entityType);
        Date date = new Date();

        Jedis jedis = jedisAdapter.getJedis();
        Transaction tx = jedisAdapter.multi(jedis);
        tx.zadd(followerKey, date.getTime(), String.valueOf(userId));   //实体的粉丝增加当前用户
        tx.zadd(followeeKey, date.getTime(), String.valueOf(entityId)); //用户对此类实体关注数+1

        List<Object> ret = jedisAdapter.exec(tx, jedis);
        return ret.size() == 2 && (Long) ret.get(0) > 0 && (Long) ret.get(1) > 0;
    }

    public boolean unfollow(int userId, int entityType, int entityId) {
        String followerKey = getFollowerKey(entityType, entityId);
        String followeeKey = getFolloweeKey(userId, entityType);

        Jedis jedis = jedisAdapter.getJedis();
        Transaction tx = jedisAdapter.multi(jedis);
        tx.zrem(followerKey, String.valueOf(userId));   //实体的粉丝删除当前用户
        tx.zrem(followeeKey, String.valueOf(entityId)); //用户对此类实体关注数-1

        List<Object> ret = jedisAdapter.exec(tx, jedis);
        return ret.size() == 2 && (Long) ret.get(0) > 0 && (Long) ret.get(1) > 0;
    }

    public List<Integer> getFollowers(int entityType, int entityId, int offset, int count) {
        String followerKey = getFollowerKey(entityType, entityId);
        return getIntIdsFromSet(jedisAdapter.zrevrange(followerKey, offset, offset + count));
    }

    public List<Integer> getFollowers(int entityType, int entityId, int count) {
        return getFollowers(entityType, entityId, 0, count);
    }

    public List<Integer> getFollowees(int userId, int entityType, int offset, int count) {
        String followeeKey = getFolloweeKey(userId, entityType);
        return getIntIdsFromSet(jedisAdapter.zrevrange(followeeKey, offset, offset + count));
    }

    public List<Integer> getFollowees(int userId, int entityType, int count) {
        return getFollowees(userId, entityType, 0, count);
    }

    public long getFollowerCount(int entityType, int entityId) {
        String followerKey = getFollowerKey(entityType, entityId);
        return jedisAdapter.zcard(followerKey);
    }

    public long getFolloweeCount(int userId, int entityType) {
        String followeeKey = getFolloweeKey(userId, entityType);
        return jedisAdapter.zcard(followeeKey);
    }

    //判断某用户是否关注某实体
    public boolean isFollower(int userId, int entityId, int entityType) {
        String followerKey = getFollowerKey(entityType, entityId);
        return jedisAdapter.zscore(followerKey, String.valueOf(userId)) != null;
    }


    private String getFollowerKey(int entityType, int entityId) {
        return RedisKeyUtils.getFollowerKey(entityType, entityId);
    }

    private String getFolloweeKey(int userId, int entityType) {
        return RedisKeyUtils.getFolloweeKey(userId, entityType);
    }

    private List<Integer> getIntIdsFromSet(Set<String> set) {
        List<Integer> res = new ArrayList<>();
        for (String id : set) {
            res.add(Integer.parseInt(id));
        }
        return res;
    }
}
