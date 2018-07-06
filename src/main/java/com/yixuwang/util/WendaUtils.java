package com.yixuwang.util;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Properties;

/**
 *
 * Created by yixu on 2018/6/25.
 */
public class WendaUtils {
    private static final Logger logger = LoggerFactory.getLogger(WendaUtils.class);

    public static int ANONYMOUS_USER_ID = 1;
    public static int SYSTEM_USERID = 0;

    public static boolean sendActivateMail(String address, int id, String checkCode){
        String sender = "603659153@qq.com";

        try{
            JavaMailSenderImpl senderImpl = new JavaMailSenderImpl();

            // 设定mail server
            senderImpl.setHost("smtp.qq.com");
            senderImpl.setPort(465);
            senderImpl.setUsername("603659153@qq.com");               // 根据自己的情况,设置发件邮箱地址
            senderImpl.setPassword("opawkkfuuzogbbie");          // 根据自己的情况, 设置 验证password
            senderImpl.setDefaultEncoding("UTF-8");
            Properties prop = new Properties();
            prop.put("mail.smtp.auth", "true");                 // 将这个参数设为true，让服务器进行认证,认证用户名和密码是否正确
            prop.put("mail.smtp.ssl.enable", "true");
            prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            senderImpl.setJavaMailProperties(prop);

            // 建立邮件消息,发送简单邮件和html邮件的区别
            MimeMessage mailMessage = senderImpl.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage);

            // 设置收件人，寄件人
            messageHelper.setTo(address);
            messageHelper.setFrom(sender);
            messageHelper.setSubject("问答网 - 激活邮件");
            // true 表示启动HTML格式的邮件
            messageHelper.setText("<h1>此邮件为官方激活邮件！请点击下面链接完成激活操作！</h1><h3>" +
                    "<a href='http://localhost:8080/activate?id=" + id + "&checkCode=" + checkCode + "'>" +
                    "点击此链接激活账号！</a></h3>", true);

            // 发送邮件
            senderImpl.send(mailMessage);
            System.out.println("-------------------");
            return true;

        } catch (javax.mail.MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static String MD5(String key) {
        char hexDigits[] = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        };
        try {
            byte[] btInput = key.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            logger.error("生成MD5失败", e);
            return null;
        }
    }

    public static String getJSONString(int code) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        return json.toJSONString();
    }

    public static String getJSONString(int code, String msg) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        return json.toJSONString();
    }

    public static String getJSONString(int code, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.put(entry.getKey(), entry.getValue());
        }
        return json.toJSONString();
    }
}
