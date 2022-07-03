package com.yubikiri.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yubikiri.reggie.dto.DishDto;
import com.yubikiri.reggie.entity.Dish;
import com.yubikiri.reggie.entity.DishFlavor;
import com.yubikiri.reggie.mapper.DishMapper;
import com.yubikiri.reggie.service.DishFlavorService;
import com.yubikiri.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    private final DishFlavorService dishFlavorService;

    public DishServiceImpl(DishFlavorService dishFlavorService) {
        this.dishFlavorService = dishFlavorService;
    }

    // 由于涉及到多张表的操作，需要加入事务控制
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {

        // 保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        Long dishId = dishDto.getId();

        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        // 保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {

        // 查询菜品的基本信息 - table Dish
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        // 查询当前菜品对应的口味信息 - table dish_flavor
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper);

        dishDto.setFlavors(dishFlavors);

        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {

        // 更新Dish表信息
        this.updateById(dishDto);

        // 更新Dish Flavor表信息
        // 先清理当前菜品对应口味数据
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());

        dishFlavorService.remove(queryWrapper);

        // 添加当前提交过来的的口味数据
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }
}
