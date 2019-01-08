package com.yixuwang.controller;

import com.yixuwang.async.EventModel;
import com.yixuwang.async.EventProducer;
import com.yixuwang.async.EventType;
import com.yixuwang.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 *
 * Created by yixuwang on 2018/6/25.
 */
@Controller
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    UserService userService;

    @Autowired
    EventProducer eventProducer;


    // 注册
    @RequestMapping(path = {"/reg/"}, method = {RequestMethod.POST})
    public String reg(Model model, @RequestParam("username") String username,
                      @RequestParam("password") String password,
                      @RequestParam("email") String email,
                      @RequestParam(value="next", required = false) String next,
                      @RequestParam(value="rememberme", defaultValue = "false") boolean rememberme,
                      HttpServletResponse response) {
        try {

            // 注册并返回信息，包括 错误信息msg、userId、checkCode、为登录的 userId 添加一天有效期的 ticket
            Map<String, Object> map = userService.register(username, password, email);

            if (map.containsKey("ticket")) {    // 注册成功
                Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
                cookie.setPath("/");
                if (rememberme) {
                    cookie.setMaxAge(3600*24*5);
                }
                response.addCookie(cookie);
                if (StringUtils.isNotBlank(next))
                    return "redirect:" + next;
                else
                    return "redirect:/";
            } else {
                model.addAttribute("msg", map.get("msg"));
                return "login";
            }
        } catch (Exception e) {
            userService.deleteByName(username);
            logger.error("注册异常" + e.getMessage());
            model.addAttribute("msg", "服务器错误,注册失败！");
            return "login";
        }
    }


    // 登录注册页面，也可能是提问回答时被拦截跳转至此，next 表示原页面路径
    @RequestMapping(path = {"/reglogin"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String regloginPage(Model model,
                               @RequestParam(value="next", required = false) String next) {
        model.addAttribute("next", next);
        return "login";
    }


    // 登录
    @RequestMapping(path = {"/login/"}, method = {RequestMethod.POST})
    public String login(Model model, @RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @RequestParam(value="next", required = false) String next,
                        @RequestParam(value="rememberme", defaultValue = "false") boolean rememberme,
                        HttpServletResponse response) {
        try {
            Map<String, Object> map = userService.login(username, password);
            if (map.containsKey("ticket")) {
                Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
                cookie.setPath("/");
                if (rememberme) {
                    cookie.setMaxAge(3600*24*5);
                }
                response.addCookie(cookie);

                // 登录成功，异步执行发送邮件
                eventProducer.fireEvent(new EventModel(EventType.LOGIN)
                        //.setExt("username", username).setExt("email", "zjuyxy@qq.com")
                        .setExt("username", username).setExt("email", userService.getUser((int)map.get("userId")).getEmail())
                        .setActorId((int)map.get("userId")));

                if (StringUtils.isNotBlank(next)) {
                    return "redirect:" + next;
                }
                return "redirect:/";
            } else {
                model.addAttribute("msg", map.get("msg"));
                return "login";
            }
        } catch (Exception e) {
            logger.error("登陆异常" + e.getMessage());
            return "login";
        }
    }


    // 邮件点击激活新注册账号
    @RequestMapping(path = {"/activate"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String activate(Model model, @RequestParam("id") int id,
                           @RequestParam("checkCode") String checkCode,
                           HttpServletResponse response) {
        try {
            Map<String, Object> map = userService.activate(id, checkCode);
            if (map.containsKey("ticket")) {
                Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
                cookie.setPath("/");
                response.addCookie(cookie);
                return "redirect:/";
            } else {
                model.addAttribute("msg", map.get("msg"));
                return "error";
            }
        } catch (Exception e) {
            logger.error("激活异常" + e.getMessage());
            return "error";
        }
    }


    // 退出登录：ticket 设置为无效
    @RequestMapping(path = {"/logout"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/";
    }
}
