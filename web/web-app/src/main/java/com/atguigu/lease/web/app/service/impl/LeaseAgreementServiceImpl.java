package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.mapper.*;
import com.atguigu.lease.web.app.service.LeaseAgreementService;
import com.atguigu.lease.web.app.service.RoomInfoService;
import com.atguigu.lease.web.app.vo.agreement.AgreementDetailVo;
import com.atguigu.lease.web.app.vo.agreement.AgreementItemVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author liubo
 * @description 针对表【lease_agreement(租约信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class LeaseAgreementServiceImpl extends ServiceImpl<LeaseAgreementMapper, LeaseAgreement>
        implements LeaseAgreementService {
    @Autowired
    private LeaseAgreementMapper leaseAgreementMapper;
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private PaymentTypeMapper paymentTypeMapper;
    @Autowired
    private LeaseTermMapper leaseTermMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Override
    public AgreementDetailVo getAgreementDetailVoById(Long id) {

        LeaseAgreement leaseAgreement = leaseAgreementMapper.selectById(id);
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(leaseAgreement.getApartmentId());
        RoomInfo roomInfo = roomInfoMapper.selectById(leaseAgreement.getRoomId());
        PaymentType paymentType = paymentTypeMapper.selectById(leaseAgreement.getLeaseTermId());
        LeaseTerm leaseTerm = leaseTermMapper.selectById(leaseAgreement.getLeaseTermId());
//        LambdaQueryWrapper<GraphInfo> apartmentInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
//        apartmentInfoLambdaQueryWrapper.eq(GraphInfo::getItemType,1);
//        apartmentInfoLambdaQueryWrapper.eq(GraphInfo::getItemId,leaseAgreement.getRoomId());
        List<GraphVo> roomGraphVoList = graphInfoMapper.selectGraphVoList(leaseAgreement.getRoomId(), ItemType.ROOM);
        List<GraphVo> apartmentGraphVoList = graphInfoMapper.selectGraphVoList(leaseAgreement.getApartmentId(), ItemType.APARTMENT);
        AgreementDetailVo agreementDetailVo = new AgreementDetailVo();
        BeanUtils.copyProperties(leaseAgreement,agreementDetailVo);
        agreementDetailVo.setApartmentName(apartmentInfo.getName());
        agreementDetailVo.setApartmentGraphVoList(apartmentGraphVoList);
        agreementDetailVo.setRoomNumber(roomInfo.getRoomNumber());
        agreementDetailVo.setRoomGraphVoList(roomGraphVoList);
        agreementDetailVo.setPaymentTypeName(paymentType.getName());
        agreementDetailVo.setLeaseTermMonthCount(leaseTerm.getMonthCount());
        agreementDetailVo.setLeaseTermUnit(leaseTerm.getUnit());
        return agreementDetailVo;
    }

    @Override
    public List<AgreementItemVo> listItemByPhone(String phone) {
        List<AgreementItemVo> result=leaseAgreementMapper.listItemByPhone(phone);


        return result;
    }
}




