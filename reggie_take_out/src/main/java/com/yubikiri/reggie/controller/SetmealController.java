package com.yubikiri.reggie.controller;

import com.yubikiri.reggie.common.R;
import com.yubikiri.reggie.dto.SetmealDto;
import com.yubikiri.reggie.service.SetmealDishService;
import com.yubikiri.reggie.service.SetmealService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    private final SetmealService setmealService;

    private final SetmealDishService setmealDishService;

    public SetmealController(SetmealService setmealService, SetmealDishService setmealDishService) {
        this.setmealService = setmealService;
        this.setmealDishService = setmealDishService;
    }

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {

        setmealService.saveWithDish(setmealDto);
        return R.success("添加套餐成功");
    }
}
