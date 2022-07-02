package com.yubikiri.reggie.dto;

import com.yubikiri.reggie.entity.Setmeal;
import com.yubikiri.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
