package com.atguigu.lease.web.app.controller.appointment;


import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.ViewAppointment;
import com.atguigu.lease.web.app.mapper.ViewAppointmentMapper;
import com.atguigu.lease.web.app.service.ApartmentInfoService;
import com.atguigu.lease.web.app.service.ViewAppointmentService;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.appointment.AppointmentDetailVo;
import com.atguigu.lease.web.app.vo.appointment.AppointmentItemVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "看房预约信息")
@RestController
@RequestMapping("/app/appointment")
public class ViewAppointmentController {
    @Autowired
    private ViewAppointmentService viewAppointmentService;
//    @Autowired
//    private ViewAppointmentService viewAppointmentService;
    @Autowired
    private ViewAppointmentMapper viewAppointmentMapper;

    @Autowired
    private ApartmentInfoService apartmentInfoService;
    @Operation(summary = "保存或更新看房预约")
    @PostMapping("/saveOrUpdate")
    public Result saveOrUpdate(@RequestBody ViewAppointment viewAppointment) {
        viewAppointment.setUserId(LoginUserHolder.getLoginUser().getUserId());
        LambdaQueryWrapper<ViewAppointment> viewAppointmentLambdaQueryWrapper = new LambdaQueryWrapper<>();
        viewAppointmentLambdaQueryWrapper.eq(ViewAppointment::getUserId,LoginUserHolder.getLoginUser().getUserId());
        viewAppointmentLambdaQueryWrapper.eq(ViewAppointment::getApartmentId,viewAppointment.getApartmentId());
        ViewAppointment one = viewAppointmentService.getOne(viewAppointmentLambdaQueryWrapper);
        if (one==null){
            viewAppointmentMapper.insert(viewAppointment);
        }else {
            LambdaUpdateWrapper<ViewAppointment> viewAppointmentLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            viewAppointmentLambdaUpdateWrapper.eq(ViewAppointment::getUserId,viewAppointment.getUserId());
            viewAppointmentMapper.update(viewAppointment,viewAppointmentLambdaUpdateWrapper);
        }
        return Result.ok();
//        viewAppointment.setUserId(LoginUserHolder.getLoginUser().getUserId());
//        viewAppointmentService.saveOrUpdate(viewAppointment);

    }

    @Operation(summary = "查询个人预约看房列表")
    @GetMapping("listItem")
    public Result<List<AppointmentItemVo>> listItem() {
        List<AppointmentItemVo> list =viewAppointmentService.listItem(LoginUserHolder.getLoginUser().getUserId());
        return Result.ok(list);
    }

    @GetMapping("getDetailById")
    @Operation(summary = "根据ID查询预约详情信息")
    public Result<AppointmentDetailVo> getDetailById(Long id) {
        AppointmentDetailVo appointmentDetailVo = new AppointmentDetailVo();
        ViewAppointment byId = viewAppointmentService.getById(id);
        ApartmentItemVo apartmentInfoServiceById = apartmentInfoService.getApartmentInfoServiceById(byId.getApartmentId());
        BeanUtils.copyProperties(byId,appointmentDetailVo);
        appointmentDetailVo.setApartmentItemVo(apartmentInfoServiceById);
        return Result.ok(appointmentDetailVo);
    }

}

