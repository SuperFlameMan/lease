package com.atguigu.lease.common.exception;

import com.atguigu.lease.common.result.ResultCodeEnum;
import lombok.Data;
import org.springframework.stereotype.Component;
@Data
public class LeaseException extends RuntimeException{
    private Integer code;
    public LeaseException(ResultCodeEnum resultCodeEnum){
        super(resultCodeEnum.getMessage());
        this.code=resultCodeEnum.getCode();
    }
}
