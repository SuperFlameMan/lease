package com.atguigu.lease.web.app.vo.user;



import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
@Schema(description = "APP端登录实体")
public class LoginVo {
    @Schema(description = "手机号码")
    @NotBlank
    private String phone;
    @Schema(description = "短信验证码")
    @NotBlank
    private String code;
}
