package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.utils.CodeUtil;
import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.app.mapper.UserInfoMapper;
import com.atguigu.lease.web.app.service.LoginService;
import com.atguigu.lease.web.app.service.SmsService;
import com.atguigu.lease.web.app.vo.user.LoginVo;
import com.atguigu.lease.web.app.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private SmsService smsService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Override
    public void getCode(String phone) {
        String randomCode = CodeUtil.getRandomCode(6);
        String key = RedisConstant.APP_LOGIN_PREFIX+phone;
        Boolean isKey = stringRedisTemplate.hasKey(key);
        if (isKey){

            Long TTL = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (RedisConstant.APP_LOGIN_CODE_TTL_SEC-TTL<RedisConstant.APP_LOGIN_CODE_RESEND_TIME_SEC){
                throw  new LeaseException(ResultCodeEnum.APP_SEND_SMS_TOO_OFTEN);
            }
        }
        smsService.sendCode(phone,randomCode);
        stringRedisTemplate.opsForValue().set(key,randomCode,RedisConstant.APP_LOGIN_CODE_TTL_SEC, TimeUnit.SECONDS);

    }

    @Override
    public String login(LoginVo loginVo) {
        String key=RedisConstant.APP_LOGIN_PREFIX+loginVo.getPhone();
        String code = stringRedisTemplate.opsForValue().get(key);
        if (code==null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EXPIRED);
        }
        if (!code.equals(loginVo.getCode()))
        {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_ERROR);
        }
        LambdaQueryWrapper<UserInfo> userInfoQuery = new LambdaQueryWrapper<UserInfo>();
        userInfoQuery.eq(UserInfo::getPhone,loginVo.getPhone());
        UserInfo userInfo = userInfoMapper.selectOne(userInfoQuery);

        if (userInfo==null){
            userInfo= new UserInfo();
            userInfo.setPhone(loginVo.getPhone());
            userInfo.setStatus(BaseStatus.ENABLE);
            userInfo.setNickname("用户-"+loginVo.getPhone().substring(7));
            userInfoMapper.insert(userInfo);
        }else {
            if (userInfo.getStatus()==BaseStatus.DISABLE){
                throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
            }
        }
        return JwtUtil.createToken(userInfo.getId(),userInfo.getPhone());
    }

    @Override
    public UserInfoVo getUserInfoById(Long id) {
        LambdaQueryWrapper<UserInfo> userInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userInfoLambdaQueryWrapper.eq(UserInfo::getId,id);
        UserInfo userInfo = userInfoMapper.selectOne(userInfoLambdaQueryWrapper);
        UserInfoVo userInfoVo = new UserInfoVo(userInfo.getNickname(), userInfo.getAvatarUrl());
        return userInfoVo;
    }


}
