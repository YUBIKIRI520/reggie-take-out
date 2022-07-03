package com.yubikiri.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yubikiri.reggie.entity.User;
import com.yubikiri.reggie.mapper.UserMapper;
import com.yubikiri.reggie.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
