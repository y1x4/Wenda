package com.yixuwang.async;

import java.util.List;

/**
 *
 * Created by yixu on 2018/6/29.
 */
public interface EventHandler {
    void doHandle(EventModel model);

    List<EventType> getSupportEventTypes();
}
