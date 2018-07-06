package com.yixuwang.service;

import com.yixuwang.util.JedisAdapter;
import com.yixuwang.util.RedisKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Created by yixu on 2018/6/29.
 */
@Service
public class LikeService {

    @Autowired
    JedisAdapter jedisAdapter;

    //返回赞同数
    public long like(int userId, int entityType, int entityId) {
        String likeKey = RedisKeyUtils.getLikeKey(entityType, entityId);
        jedisAdapter.sadd(likeKey, String.valueOf(userId));

        //点赞同时取消以前点过的反对
        String dislikeKey = RedisKeyUtils.getDisLikeKey(entityType, entityId);
        jedisAdapter.srem(dislikeKey, String.valueOf(userId));

        return jedisAdapter.scard(likeKey);
    }

    //返回赞同数
    public long dislike(int userId, int entityType, int entityId) {
        String dislikeKey = RedisKeyUtils.getDisLikeKey(entityType, entityId);
        jedisAdapter.sadd(dislikeKey, String.valueOf(userId));

        //反对同时取消以前点过的赞同
        String likeKey = RedisKeyUtils.getLikeKey(entityType, entityId);
        jedisAdapter.srem(likeKey, String.valueOf(userId));

        return jedisAdapter.scard(likeKey);
    }

    public long getLikeCount(int entityType, int entityId) {
        String likeKey = RedisKeyUtils.getLikeKey(entityType, entityId);
        return jedisAdapter.scard(likeKey);
    }

    public int getLikeStatus(int userId, int entityType, int entityId) {
        String likeKey = RedisKeyUtils.getLikeKey(entityType, entityId);
        if (jedisAdapter.sismember(likeKey, String.valueOf(userId))) {
            return 1;
        }
        String dislikeKey = RedisKeyUtils.getDisLikeKey(entityType, entityId);
        return jedisAdapter.sismember(dislikeKey, String.valueOf(userId)) ? -1 : 0;
    }
}
