package com.yubikiri.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yubikiri.reggie.dto.SetmealDto;
import com.yubikiri.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    public void saveWithDish(SetmealDto setmealDto);

    public void removeWithDish(List<Long> ids);

    public void updateWithDish(SetmealDto setmealDto);
}
