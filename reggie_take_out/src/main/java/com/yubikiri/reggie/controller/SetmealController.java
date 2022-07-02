package com.yubikiri.reggie.controller;

import com.yubikiri.reggie.service.SetmealDishService;
import com.yubikiri.reggie.service.SetmealService;
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

    
}
