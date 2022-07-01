package com.yubikiri.reggie.controller;

import com.yubikiri.reggie.common.R;
import com.yubikiri.reggie.dto.DishDto;
import com.yubikiri.reggie.service.DishFlavorService;
import com.yubikiri.reggie.service.DishService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dish")
public class DishController {

    private final DishService dishService;

    private final DishFlavorService dishFlavorService;

    public DishController(DishService dishService, DishFlavorService dishFlavorService) {
        this.dishService = dishService;
        this.dishFlavorService = dishFlavorService;
    }

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {

        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

}
