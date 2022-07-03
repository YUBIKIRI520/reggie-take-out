package com.yubikiri.reggie.controller;

import com.yubikiri.reggie.common.CustomException;
import com.yubikiri.reggie.common.R;
import com.yubikiri.reggie.utils.RandomUtil;
import com.yubikiri.reggie.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/email")
@Slf4j
public class MailController {
    @Autowired
    private MailService mailService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    //发送邮箱验证码
    @GetMapping("send/{email}")
    public R<String> sendEmail(@PathVariable String email) {
        //key 邮箱号  value 验证码
        String code = redisTemplate.opsForValue().get(email);
        //从redis获取验证码，如果获取获取到，返回ok
        if (!StringUtils.isEmpty(code)) {
            return R.success("验证码仍有效");
        }
        //如果从redis获取不到，生成新的6位验证码
        code = RandomUtil.getSixBitRandom();
        //调用service方法，通过邮箱服务进行发送
        boolean isSend = mailService.sendMail(email, code);
        //生成验证码放到redis里面，设置有效时间
        if (isSend) {
            redisTemplate.opsForValue().set(email, code, 5, TimeUnit.MINUTES);
            return R.success("验证码发送成功");
        } else {
            throw new CustomException("邮箱验证码发送失败");
        }
    }
}

