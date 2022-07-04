package com.yubikiri.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yubikiri.reggie.common.R;
import com.yubikiri.reggie.entity.Orders;
import com.yubikiri.reggie.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {

        orderService.submit(orders);
        return R.success("订单创建成功");
    }

    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize){
        Page pageInfo = new Page<>(page,pageSize);
        return orderService.userPage(pageInfo);
    }
}
