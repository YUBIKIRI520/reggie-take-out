package com.yubikiri.reggie.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

//用于生成随机验证码
public class RandomUtil {

    private static final Random random = new Random();
    private static final DecimalFormat fourdf = new DecimalFormat("0000");//4位验证码
    private static final DecimalFormat sixdf = new DecimalFormat("000000");//6位验证码

    public static String getFourBitRandom() {
        return fourdf.format(random.nextInt(10000));
    }

    public static String getSixBitRandom() {
        return sixdf.format(random.nextInt(1000000));
    }
}

