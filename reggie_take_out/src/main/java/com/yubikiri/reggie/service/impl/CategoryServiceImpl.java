package com.yubikiri.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yubikiri.reggie.entity.Category;
import com.yubikiri.reggie.mapper.CategoryMapper;
import com.yubikiri.reggie.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
}
