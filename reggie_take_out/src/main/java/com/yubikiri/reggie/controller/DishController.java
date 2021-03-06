package com.yubikiri.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yubikiri.reggie.common.R;
import com.yubikiri.reggie.dto.DishDto;
import com.yubikiri.reggie.entity.Category;
import com.yubikiri.reggie.entity.Dish;
import com.yubikiri.reggie.entity.DishFlavor;
import com.yubikiri.reggie.service.CategoryService;
import com.yubikiri.reggie.service.DishFlavorService;
import com.yubikiri.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
public class DishController {

    private final DishService dishService;
    private final DishFlavorService dishFlavorService;
    private final RedisTemplate redisTemplate;
    private final CategoryService categoryService;

    public DishController(DishService dishService, DishFlavorService dishFlavorService, CategoryService categoryService, RedisTemplate redisTemplate) {
        this.dishService = dishService;
        this.dishFlavorService = dishFlavorService;
        this.categoryService = categoryService;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {

        dishService.saveWithFlavor(dishDto);

        // 清理对应分类下菜品缓存数据
        String key = "dish:" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

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

    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {

        dishService.updateWithFlavor(dishDto);

        // 清理对应分类下菜品缓存数据
        String key = "dish:" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("菜品信息更新成功");
    }

//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish) {
//
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
//        queryWrapper.eq(Dish::getStatus, 1);
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> list = dishService.list(queryWrapper);
//
//        return R.success(list);
//    }

    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {

        List<DishDto> dishDtoList = null;

        String key = "dish:" + dish.getCategoryId() + "_" + dish.getStatus();

        // 先从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        // 如果存在，直接返回数据，无需查询数据库
        if (dishDtoList != null) {
            return R.success(dishDtoList);
        }

        // 如果不存在，需要查询数据库，并将查询到的菜品数据缓存到redis
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> dishes = dishService.list(queryWrapper);

        dishDtoList = dishes.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            LambdaQueryWrapper<DishFlavor> flavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            flavorLambdaQueryWrapper.eq(DishFlavor::getDishId, item.getId());
            List<DishFlavor> dishFlavors = dishFlavorService.list(flavorLambdaQueryWrapper);

            dishDto.setFlavors(dishFlavors);

            return dishDto;
        }).collect(Collectors.toList());

        // 存入redis
        redisTemplate.opsForValue().set(key, dishDtoList, 30, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }
}
