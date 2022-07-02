package com.yubikiri.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yubikiri.reggie.dto.SetmealDto;
import com.yubikiri.reggie.entity.Setmeal;

public interface SetmealService extends IService<Setmeal> {

    public void saveWithDish(SetmealDto setmealDto);
}
