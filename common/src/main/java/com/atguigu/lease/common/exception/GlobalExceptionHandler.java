package com.atguigu.lease.common.exception;

import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.result.ResultCodeEnum;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.rmi.dgc.Lease;

import static com.atguigu.lease.common.result.ResultCodeEnum.ADMIN_APARTMENT_DELETE_ERROR;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseBody
//    这里返回值当作接口发生异常时的返回值
    public Result ExceptionHandler(Exception e){
        e.printStackTrace();
        return Result.fail();
    }

//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public Result handleValidationExceptions(MethodArgumentNotValidException ex) {
//        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
//        return Result.ok(errorMessage);
//    }

    @ExceptionHandler(LeaseException.class)
    @ResponseBody
//    这里返回值当作接口发生异常时的返回值
    public Result LeaseException(LeaseException e){
        e.printStackTrace();
        return Result.fail(e.getCode(),e.getMessage());
    }

}
