package com.atguigu.lease.web.app.controller.login;


import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.web.app.service.LoginService;
import com.atguigu.lease.web.app.vo.user.LoginVo;
import com.atguigu.lease.web.app.vo.user.UserInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;


import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
@Tag(name = "登录管理")
@RestController
@RequestMapping("/app/")

public class LoginController {
    @Autowired
    private LoginService service;
    @GetMapping("login/getCode")
    @Operation(summary = "获取短信验证码")
    public Result getCode(@RequestParam String phone) {
        service.getCode(phone);
        return Result.ok();
    }

    @PostMapping("login")
    @Operation(summary = "登录")
    public Result<String> login(@Validated @RequestBody LoginVo loginVo, BindingResult bindingResult) {
        System.out.println("bindingResult.hasErrors() = " + bindingResult.hasErrors());
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            System.out.println("fieldError = " + fieldError);
            String field = fieldError.getField();
            System.out.println("field = " + field);
            if (field.equals("phone")){
                throw new LeaseException(ResultCodeEnum.APP_LOGIN_PHONE_EMPTY);
            }
            else if (field.equals("code")) {
                throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EMPTY);
            }
        }


        String jwt=service.login(loginVo);

        return Result.ok(jwt);
    }

    @GetMapping("info")
    @Operation(summary = "获取登录用户信息")
    public Result<UserInfoVo> info() {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        System.out.println("loginUser.getUsername() = " + loginUser.getUsername());
        UserInfoVo userInfoVo=service.getUserInfoById(loginUser.getUserId());

        return Result.ok(userInfoVo);
    }
}

