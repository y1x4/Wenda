package com.yixuwang.service;

import org.springframework.stereotype.Service;

/**
 *
 * Created by yixuwang on 2018/6/25.
 */
@Service
public class WendaService {
    public String getMessage(int userId) {
        return "Hello Message: " + String.valueOf(userId);
    }
}
