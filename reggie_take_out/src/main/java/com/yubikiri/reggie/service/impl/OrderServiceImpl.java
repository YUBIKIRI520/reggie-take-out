package com.yubikiri.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yubikiri.reggie.common.BaseContext;
import com.yubikiri.reggie.common.CustomException;
import com.yubikiri.reggie.common.R;
import com.yubikiri.reggie.dto.OrdersDto;
import com.yubikiri.reggie.entity.*;
import com.yubikiri.reggie.mapper.OrderDetailMapper;
import com.yubikiri.reggie.mapper.OrderMapper;
import com.yubikiri.reggie.mapper.UserMapper;
import com.yubikiri.reggie.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    private final ShoppingCartService shoppingCartService;
    private final UserService userService;
    private final AddressBookService addressBookService;
    private final OrderDetailService orderDetailService;
    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final UserMapper userMapper;

    public OrderServiceImpl(ShoppingCartService shoppingCartService, UserService userService, AddressBookService addressBookService, OrderDetailService orderDetailService, OrderMapper orderMapper, OrderDetailMapper orderDetailMapper, UserMapper userMapper) {
        this.shoppingCartService = shoppingCartService;
        this.userService = userService;
        this.addressBookService = addressBookService;
        this.orderDetailService = orderDetailService;
        this.orderMapper = orderMapper;
        this.orderDetailMapper = orderDetailMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public void submit(Orders orders) {

        // 取得当前用户的id
        Long userId = BaseContext.getCurrentId();

        // 取得数据库中当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);

        if (shoppingCarts == null || shoppingCarts.size() == 0) {
            throw new CustomException("购物车为空");
        }

        // 查询用户数据
        User user = userService.getById(userId);

        // 查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null) {
            throw new CustomException("地址信息有误");
        }

        long orderId = IdWorker.getId();//订单号

        AtomicInteger amount = new AtomicInteger(0);

        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());


        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        //向订单表插入数据，一条数据
        this.save(orders);

        //向订单明细表插入数据，多条数据
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }

    @Override
    public R<Page> userPage(Page pageInfo) {

        try {
            Page<OrdersDto> dtoPage = new Page<>();
            LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(Orders::getOrderTime);
            queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
            orderMapper.selectPage(pageInfo,queryWrapper);
            BeanUtils.copyProperties(pageInfo,dtoPage,"records");
            List<Orders> records = pageInfo.getRecords();
            List<OrdersDto> ordersDtoList = new ArrayList<>();
            for (Orders record : records) {
                OrdersDto ordersDto = new OrdersDto();
                BeanUtils.copyProperties(record,ordersDto);
                Long userId = record.getUserId();
                LambdaQueryWrapper<OrderDetail> queryWrapper1 = new LambdaQueryWrapper<>();
                queryWrapper1.eq(OrderDetail::getOrderId,record.getId());
                List<OrderDetail> orderDetails = orderDetailMapper.selectList(queryWrapper1);
                ordersDto.setOrderDetails(orderDetails);

                User user = userMapper.selectById(userId);

                if (user != null){
                    String name = user.getName();
                    ordersDto.setUserName(name);
                }
                ordersDtoList.add(ordersDto);
            }
            dtoPage.setRecords(ordersDtoList);
            return R.success(dtoPage);
        }catch (Exception e){
            e.printStackTrace();
            return R.error("订单数据加载失败");
        }

    }
}
