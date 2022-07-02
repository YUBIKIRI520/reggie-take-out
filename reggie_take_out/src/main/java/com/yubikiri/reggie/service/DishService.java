package com.yubikiri.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yubikiri.reggie.dto.DishDto;
import com.yubikiri.reggie.entity.Dish;

public interface DishService extends IService<Dish> {

    public void saveWithFlavor(DishDto dishDto);

    public DishDto getByIdWithFlavor(Long id);

    public void updateWithFlavor(DishDto dishDto);
}
