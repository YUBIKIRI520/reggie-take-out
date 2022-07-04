package com.yubikiri.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yubikiri.reggie.common.BaseContext;
import com.yubikiri.reggie.common.R;
import com.yubikiri.reggie.dto.OrdersDto;
import com.yubikiri.reggie.entity.Orders;
import com.yubikiri.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/order")
@Slf4j
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
    public R<Page> userPage(int page, int pageSize, HttpSession session){
        log.info(session.getAttribute("user").toString());
        log.info(BaseContext.getCurrentId().toString());
        Page pageInfo = new Page<>(page,pageSize);
        return orderService.userPage(pageInfo);
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number, String beginTime, String endTime){

        Page pageInfo = new Page<>(page,pageSize);
        return orderService.pageQuery(pageInfo, number, beginTime, endTime);
    }

    @PutMapping
    public R<String> update(@RequestBody OrdersDto ordersDto) {

        Long orderId = ordersDto.getId();
        Orders orderInfo = orderService.getById(orderId);
        orderInfo.setStatus(ordersDto.getStatus());

        orderService.updateById(orderInfo);

        return R.success("更新订单状态成功");
    }

    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders) {

        if (orders.getId() == null) return R.error("订单信息缺失，请重试");
        return R.success("再来一单成功");
    }
}
