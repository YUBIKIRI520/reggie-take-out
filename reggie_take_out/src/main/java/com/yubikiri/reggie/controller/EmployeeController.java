package com.yubikiri.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yubikiri.reggie.common.R;
import com.yubikiri.reggie.entity.Employee;
import com.yubikiri.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/login")
    public R<Employee> login (HttpServletRequest request, @RequestBody Employee employee) {

        // 1. 前端提交的密码进行md5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2. 根据页面提交的用户名进行数据库查询
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        // 3. 如果没有查到对应用户
        if (emp == null) {
            return R.error("用户名或密码错误");
        }

        // 4. 密码比对
        if (!emp.getPassword().equals(password)) {
            return R.error("用户名或密码错误");
        }

        // 5. 查看员工状态是否禁用
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }

        // 6. 登陆成功，将用户id存入session
        request.getSession().setAttribute("employee", emp.getId());

        return R.success(emp);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        return R.success("退出登录成功");
    }
}
