package com.yubikiri.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yubikiri.reggie.common.CustomException;
import com.yubikiri.reggie.common.R;
import com.yubikiri.reggie.entity.User;
import com.yubikiri.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    private final UserService userService;
    private final StringRedisTemplate stringRedisTemplate;

    public UserController(UserService userService, StringRedisTemplate stringRedisTemplate) {
        this.userService = userService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {

        // 获取邮箱
        String phone = map.get("phone").toString();

        // 获取验证码
        String code = map.get("code").toString();

        // 从Redis获取保存的验证码
        String codeInRedis = stringRedisTemplate.opsForValue().get(phone);
        if (codeInRedis == null) {
            throw new CustomException("当前验证码已过期，请重新发送");
        }

        // 进行验证码比对
        if (codeInRedis.equals(code)) {
            // 如果比对成功，登陆成功
            // 判断当前邮箱是否为新用户，如果是就自动完成注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);

            User user = userService.getOne(queryWrapper);
            if (user == null) {
                // 当前邮箱确实是新用户
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user", user.getId());
            return R.success(user);
        }

        return R.error("登陆失败");
    }

    @PostMapping("/loginout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("user");
        return R.success("退出登录成功");
    }
}
