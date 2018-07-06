package com.yixuwang.util;

import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.util.Map;
import java.util.Properties;

/**
 *
 * Created by yixu on 2018/6/29.
 */
@Service
public class MailSender implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(MailSender.class);
    private JavaMailSenderImpl mailSender;

    @Autowired
    private VelocityEngine velocityEngine;

    public boolean sendWithHTMLTemplate(String to, String subject,
                                        String template, Map<String, Object> model) {
        try {
            afterPropertiesSet();   //TODO:
            String nick = MimeUtility.encodeText("问达网");
            InternetAddress from = new InternetAddress(nick + "<603659153@qq.com>");
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setSubject(subject);
            //String result = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, template, "UTF-8", model);
            String result = "出问题了...";
            if (subject.equals("账号激活")) {
                result = "<h1>此邮件为官方激活邮件！请点击下面链接完成激活操作！</h1><h3>" +
                        "<a href='http://localhost:8080/activate?id=" + model.get("userId")
                        + "&checkCode=" + model.get("checkCode") + "'> 点击此链接激活账号！</a></h3>";
            } else if (subject.equals("登陆成功")){
                result = "你好【" + model.get("username") + "】，你刚刚登陆了问达网!";
            }
            mimeMessageHelper.setText(result, true);
            mailSender.send(mimeMessage);
            return true;
        } catch (Exception e) {
            logger.error("发送邮件失败" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 设定mail server
        mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.qq.com");
        mailSender.setPort(465);
        mailSender.setUsername("603659153@qq.com");               // 根据自己的情况,设置发件邮箱地址
        mailSender.setPassword("ejuhxsralymbbcbh");          // 根据自己的情况, 设置 验证password
        mailSender.setDefaultEncoding("UTF-8");
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", "true");                 // 将这个参数设为true，让服务器进行认证,认证用户名和密码是否正确
        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        mailSender.setJavaMailProperties(prop);
    }
}
