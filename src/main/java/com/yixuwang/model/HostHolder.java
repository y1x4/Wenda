package com.yixuwang.model;

import org.springframework.stereotype.Component;

/**
 * 存储线程用户并返回当前线程用户
 * Created by yixuwang on 2018/6/25.
 */
@Component
public class HostHolder {
    private static ThreadLocal<User> users = new ThreadLocal<User>();

    public User getUser() {
        return users.get();
    }

    public void setUser(User user) {
        users.set(user);
    }

    public void clear() {
        users.remove();
    }
}
