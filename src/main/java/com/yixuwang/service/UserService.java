package com.yixuwang.service;

import com.yixuwang.dao.LoginTicketDAO;
import com.yixuwang.dao.UserDAO;
import com.yixuwang.model.LoginTicket;
import com.yixuwang.model.User;
import com.yixuwang.util.MailSender;
import com.yixuwang.util.WendaUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 *
 * Created by yixu on 2018/6/25.
 */
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private LoginTicketDAO loginTicketDAO;

    //@Autowired
    //MailSender mailSender;

    public Map<String, Object> register(String username, String password, String email) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(username)) {
            map.put("msg", "用户名不能为空!");
            return map;
        }

        if (StringUtils.isBlank(password)) {
            map.put("msg", "密码不能为空!");
            return map;
        }

        if (StringUtils.isBlank(email)) {
            map.put("msg", "邮箱不能为空!");
            return map;
        }

        if (username.length() < 3 || username.length() > 10 || !username.matches("[A-Za-z1-9]+$")) {
            map.put("msg", "用户名必须为3-10位字母或数字!");
            return map;
        }

        if (!email.matches("^[a-zA-Z_]+[0-9]*@(([a-zA-z0-9]-*)+\\.){1,3}[a-zA-z\\-]+$")) {
            map.put("msg", "邮箱格式不正确!");
            return map;
        }

        User user = userDAO.selectByName(username);
        if (user != null) {
            map.put("msg", "用户名已存在!");
            return map;
        }

        user = userDAO.selectByEmail(email);
        if (user != null) {
            map.put("msg", "邮箱已注册!");
            return map;
        }

        // 注册用户
        user = new User();
        user.setName(username);
        user.setSalt(UUID.randomUUID().toString().substring(0, 5));
        user.setPassword(WendaUtils.MD5(password + user.getSalt()));
        String head = String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000));
        user.setHeadUrl(head);
        user.setStatus(0);  //需激活
        user.setEmail(email);
        userDAO.addUser(user);

        String checkCode = user.getId() + "------" + WendaUtils.MD5(email + user.getSalt());
        System.out.println(checkCode);

        //邮件发送成功才算注册成功
        //if (WendaUtils.sendActivateMail(email, user.getId(), WendaUtils.MD5(email + user.getSalt()))) {
        map.put("userId", user.getId());
        map.put("checkCode", WendaUtils.MD5(email + user.getSalt()));
        if (new MailSender().sendWithHTMLTemplate(email, "账号激活", "mails/activate.html", map)) {
            String ticket = addLoginTicket(user.getId());
            map.put("ticket", ticket);
        } else {
            userDAO.deleteById(user.getId());
            map.put("msg", "服务器错误，注册失败！");
        }
        return map;
    }

    public void deleteByName(String username) {
        User user = userDAO.selectByName(username);
        if (user != null) {
            userDAO.deleteById(user.getId());
        }
    }

    public Map<String, Object> login(String username, String password) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(username)) {
            map.put("msg", "用户名不能为空！");
            return map;
        }

        if (StringUtils.isBlank(password)) {
            map.put("msg", "密码不能为空！");
            return map;
        }

        User user = userDAO.selectByName(username);
        if (user == null || !Objects.equals(WendaUtils.MD5(password + user.getSalt()), user.getPassword())) {
            map.put("msg", "账号或密码错误！");
            return map;
        }

        if (user.getStatus() == 0) {
            map.put("msg", "请激活账号后再登录！");
            return map;
        }

        String ticket = addLoginTicket(user.getId());
        map.put("ticket", ticket);
        map.put("userId", user.getId());
        return map;
    }


    // 为登录的userId 添加 ticket
    private String addLoginTicket(int userId) {
        LoginTicket ticket = new LoginTicket();
        ticket.setUserId(userId);
        Date date = new Date();
        date.setTime(date.getTime() + 1000*3600*24);    //一天有效时间
        ticket.setExpired(date);
        ticket.setStatus(0);    //有效
        ticket.setTicket(UUID.randomUUID().toString().replaceAll("-", ""));
        loginTicketDAO.addTicket(ticket);
        return ticket.getTicket();
    }

    public User getUser(int id) {
        return userDAO.selectById(id);
    }

    public void logout(String ticket) {
        loginTicketDAO.updateStatus(ticket, 1);
    }

    public Map<String, Object> activate(int id, String checkCode) {
        Map<String, Object> map = new HashMap<>();
        User user = userDAO.selectById(id);
        if (user == null || !Objects.equals(WendaUtils.MD5(user.getEmail() + user.getSalt()), checkCode)) {
            map.put("msg", "激活失败，请点击邮件中的链接重新激活！");
            return map;
        }
        //激活成功，直接登录并跳转到首页
        userDAO.updateStatus(id, 1);
        String ticket = addLoginTicket(user.getId());
        map.put("ticket", ticket);
        return map;
    }

    public User selectByName(String username) {
        return userDAO.selectByName(username);
    }
}
