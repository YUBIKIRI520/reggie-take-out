package com.yubikiri.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yubikiri.reggie.common.CustomException;
import com.yubikiri.reggie.dto.SetmealDto;
import com.yubikiri.reggie.entity.DishFlavor;
import com.yubikiri.reggie.entity.Setmeal;
import com.yubikiri.reggie.entity.SetmealDish;
import com.yubikiri.reggie.mapper.SetmealMapper;
import com.yubikiri.reggie.service.SetmealDishService;
import com.yubikiri.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    private final SetmealDishService setmealDishService;

    public SetmealServiceImpl(SetmealDishService setmealDishService) {
        this.setmealDishService = setmealDishService;
    }

    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {

        // 保存套餐的基本信息 - Setmeal表
        this.save(setmealDto);

        // 保存套餐和菜品的关联信息 - Setmeal_Dish表
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {

        // 查询套餐状态，确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);

        // 如果不能删除，抛出业务异常
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        // 如果可以删除，先删除套餐表中数据
        this.removeByIds(ids);

        // 再删除关系表中的数据
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);

        setmealDishService.remove(lambdaQueryWrapper);
    }

    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {

        // 更新Setmeal表信息
        this.updateById(setmealDto);

        // 更新Setmeal_Dish表信息
        // 先清理当前的dish数据
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());

        setmealDishService.remove(queryWrapper);

        // 添加当前提交过来的的Dish数据
        List<SetmealDish> setmealDishList = setmealDto.getSetmealDishes();
        List<SetmealDish> dishes = setmealDishList.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(dishes);
    }
}
