package com.yubikiri.reggie.service.impl;

import com.yubikiri.reggie.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class MailServiceImpl implements MailService {

    @Value("${spring.mail.username}")
    private String UserName;//获得配置文件中的username

    @Autowired
    private JavaMailSender mailSender;//注入发送邮件的bean

    @Override
    public boolean sendMail(String email, String code) {
        //判断邮箱是否为空
        if (StringUtils.isEmpty(email)) {
            return false;
        }
        //标题
        String subject = "Reggie邮箱登陆验证码";
        //正文内容
        String text = "你的验证码为" + code + "，有效时间为5分钟，请尽快使用并且不要告诉别人。";

        SimpleMailMessage msg = new SimpleMailMessage();
        //发送邮件的邮箱
        msg.setFrom(UserName);
        //发送到哪(邮箱)
        msg.setTo(email);
        //邮箱标题
        msg.setSubject(subject);
        //邮箱文本
        msg.setText(text);
        try {
            mailSender.send(msg);
            log.info("邮件已经发送至{}", email);
        } catch (MailException ex) {
            ex.getMessage();
        }
        return true;
    }
}

