package com.yixuwang.async.handler;

import com.yixuwang.async.EventHandler;
import com.yixuwang.async.EventModel;
import com.yixuwang.async.EventType;
import com.yixuwang.util.MailSender;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by yixu on 2018/6/29.
 */
@Component
public class LoginExceptionHandler implements EventHandler {

    //@Autowired
    //MailSender mailSender;

    @Override
    public void doHandle(EventModel model) {
        // xxxx判断发现这个用户登陆异常
        Map<String, Object> map = new HashMap<>();
        map.put("username", model.getExt("username"));
        new MailSender().sendWithHTMLTemplate(model.getExt("email"), "登陆成功", "mails/login_exception.html", map);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LOGIN);
    }
}
