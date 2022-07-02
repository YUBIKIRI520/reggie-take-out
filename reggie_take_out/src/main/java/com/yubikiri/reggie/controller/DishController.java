package com.yubikiri.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yubikiri.reggie.common.R;
import com.yubikiri.reggie.dto.DishDto;
import com.yubikiri.reggie.entity.Category;
import com.yubikiri.reggie.entity.Dish;
import com.yubikiri.reggie.service.CategoryService;
import com.yubikiri.reggie.service.DishFlavorService;
import com.yubikiri.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
public class DishController {

    private final DishService dishService;

    private final DishFlavorService dishFlavorService;

    @Autowired
    private final CategoryService categoryService;

    public DishController(DishService dishService, DishFlavorService dishFlavorService, CategoryService categoryService) {
        this.dishService = dishService;
        this.dishFlavorService = dishFlavorService;
        this.categoryService = categoryService;
    }

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {

        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {

        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.orderByDesc((Dish::getUpdateTime));

        // 执行分页查询后，pageInfo已经有了所需的Dish数据
        dishService.page(pageInfo, queryWrapper);

        // 对象拷贝 - 除了records之外的所有属性
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            // 当前的DTO对象
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            // 根据categoryId获取对应的的Name
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);

            if (category != null) {
                String categoryName = category.getName();
                // 赋值给DishDTO
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success((dishDtoPage));
    }
}
