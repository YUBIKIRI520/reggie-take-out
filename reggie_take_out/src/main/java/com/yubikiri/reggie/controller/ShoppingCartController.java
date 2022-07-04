package com.yubikiri.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yubikiri.reggie.common.BaseContext;
import com.yubikiri.reggie.common.CustomException;
import com.yubikiri.reggie.common.R;
import com.yubikiri.reggie.entity.ShoppingCart;
import com.yubikiri.reggie.service.ShoppingCartService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {

        // 设置当前用户id
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        // 查询当前菜品或套餐是否已经在购物车中
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);

        // 如果可以获取到dishId，证明前端发的req是一道菜品
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();
        if (dishId != null) {
            // 菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            // 套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, setmealId);
        }

        // 如果已经存在，在原来的数量基础上+1
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        if (cartServiceOne != null) {
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        } else {
            // 如果不存在，加入购物车，数量默认为1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }

        return R.success(cartServiceOne);
    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return R.success(list);
    }

    @DeleteMapping("/clean")
    public R<String> clean() {

        LambdaQueryWrapper<ShoppingCart>  queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        shoppingCartService.remove(queryWrapper);

        return R.success("清空购物车成功");
    }

    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart) {

        // 判断减少的是套餐还是菜品
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        if (dishId != null) {
            // 确认数据库确实存在对应item，否则抛出异常
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());

            ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
            if (cartServiceOne == null || cartServiceOne.getNumber() == null || cartServiceOne.getNumber() <= 0) {
                throw new CustomException("购物车数据异常，无法进行减少操作");
            }

            // 一切正常，减少数据
            Integer number = cartServiceOne.getNumber();
            if (number == 1) {
                shoppingCartService.remove(queryWrapper);
                return R.success("商品已从购物车中移除");
            }
            cartServiceOne.setNumber(number - 1);
            shoppingCartService.updateById(cartServiceOne);

        } else {
            // 确认数据库确实存在对应item，否则抛出异常
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());

            ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
            if (cartServiceOne == null || cartServiceOne.getNumber() == null || cartServiceOne.getNumber() <= 0) {
                throw new CustomException("购物车数据异常，无法进行减少操作");
            }

            // 一切正常，减少数据
            Integer number = cartServiceOne.getNumber();
            // 如果商品数量减少后已经为0，从数据库中移除
            if (number == 1) {
                shoppingCartService.remove(queryWrapper);
                return R.success("商品已从购物车中移除");
            }
            cartServiceOne.setNumber(number - 1);
            shoppingCartService.updateById(cartServiceOne);
        }

        return R.success("商品数量减少成功");
    }
}
