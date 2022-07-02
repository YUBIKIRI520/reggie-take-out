package com.yubikiri.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yubikiri.reggie.common.R;
import com.yubikiri.reggie.dto.SetmealDto;
import com.yubikiri.reggie.entity.Category;
import com.yubikiri.reggie.entity.Setmeal;
import com.yubikiri.reggie.service.CategoryService;
import com.yubikiri.reggie.service.SetmealDishService;
import com.yubikiri.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    private final SetmealService setmealService;

    private final SetmealDishService setmealDishService;

    private final CategoryService categoryService;

    public SetmealController(SetmealService setmealService, SetmealDishService setmealDishService, CategoryService categoryService) {
        this.setmealService = setmealService;
        this.setmealDishService = setmealDishService;
        this.categoryService = categoryService;
    }

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {

        setmealService.saveWithDish(setmealDto);
        return R.success("添加套餐成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {

        Page<Setmeal> pageInfo = new Page<>();
        Page<SetmealDto> dtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo, queryWrapper);

        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map((item) -> {

            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);

            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }

            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);

        return R.success(dtoPage);
    }

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {

        setmealService.removeWithDish(ids);
        return R.success("套餐数据删除成功");
    }

    @PostMapping("/status/{status}")
    public R<String> changeStatus(@PathVariable int status, String ids) {

        String[] idList = ids.split(",");
        for (String id : idList) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(Long.parseLong((id)));
            setmeal.setStatus(status);

            setmealService.updateById(setmeal);
        }

        return R.success("更新状态成功");
    }
}
