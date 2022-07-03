package com.yubikiri.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yubikiri.reggie.common.CustomException;
import com.yubikiri.reggie.entity.Category;
import com.yubikiri.reggie.entity.Dish;
import com.yubikiri.reggie.entity.Setmeal;
import com.yubikiri.reggie.service.mapper.CategoryMapper;
import com.yubikiri.reggie.service.CategoryService;
import com.yubikiri.reggie.service.DishService;
import com.yubikiri.reggie.service.SetmealService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final DishService dishService;

    private final SetmealService setmealService;

    public CategoryServiceImpl(SetmealService setmealService, DishService dishService) {
        this.setmealService = setmealService;
        this.dishService = dishService;
    }

    @Override
    public void remove(Long id) {

        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件，根据分类id进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int count1 = dishService.count(dishLambdaQueryWrapper);

        // 查询当前分类是否已经关联了菜品，如果已经关联，则直接抛出一个业务异常
        if (count1 > 0) {
            throw new CustomException("当前分类已关联菜品，无法进行删除");
        }


        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件，根据分类id进行查询
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);

        // 查询当前分类是否已经关联了套餐，如果已经关联，则直接抛出一个业务异常
        if (count2 > 0) {
            throw new CustomException("当前分类已关联套餐，无法进行删除");
        }

        // 正常删除分类
        super.removeById(id);
    }
}
