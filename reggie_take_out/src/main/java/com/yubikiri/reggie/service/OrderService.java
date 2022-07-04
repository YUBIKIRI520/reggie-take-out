package com.yubikiri.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yubikiri.reggie.common.R;
import com.yubikiri.reggie.entity.Orders;

public interface OrderService extends IService<Orders> {

    public void submit(Orders orders);

    public R<Page> userPage(Page pageInfo);

    R<Page> pageQuery(Page pageInfo, String number, String beginTime, String endTime);

}
