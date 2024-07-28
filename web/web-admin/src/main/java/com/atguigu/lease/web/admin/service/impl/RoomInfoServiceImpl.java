package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.attr.AttrValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.atguigu.lease.web.admin.vo.room.RoomDetailVo;
import com.atguigu.lease.web.admin.vo.room.RoomItemVo;
import com.atguigu.lease.web.admin.vo.room.RoomQueryVo;
import com.atguigu.lease.web.admin.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo> implements RoomInfoService {
    @Autowired
    private GraphInfoService graphInfoService;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private RoomFacilityService roomFacilityService;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private RoomLabelService roomLabelService;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private RoomAttrValueService roomAttrValueService;
    @Autowired
    private AttrValueMapper attrValueMapper;
    @Autowired
    private RoomPaymentTypeService roomPaymentTypeService;
    @Autowired
    private PaymentTypeMapper paymentTypeMapper;
    @Autowired
    private RoomLeaseTermService roomLeaseTermService;
    @Autowired
    private LeaseTermMapper leaseTermMapper;
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Override
    public void saveOrUpdateRoom(RoomSubmitVo roomSubmitVo) {
        String key =RedisConstant.APP_ROOM_PREFIX+roomSubmitVo.getId();
        redisTemplate.delete(key);
        boolean isUpdate = roomSubmitVo.getId() != null;
        super.saveOrUpdate(roomSubmitVo);
        if (isUpdate) {
//            删除房间图片信息
            LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
            graphInfoQueryWrapper.eq(GraphInfo::getItemId, roomSubmitVo.getId());
            graphInfoQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
            graphInfoService.remove(graphInfoQueryWrapper);
//            删除配套信息列表
            LambdaQueryWrapper<RoomFacility> facilityInfoQueryWrapper = new LambdaQueryWrapper<>();
            facilityInfoQueryWrapper.eq(RoomFacility::getRoomId, roomSubmitVo.getId());
            roomFacilityService.remove(facilityInfoQueryWrapper);
//            删除标签信息列表
            LambdaQueryWrapper<RoomLabel> roomLabelQueryWrapper = new LambdaQueryWrapper<>();
            roomLabelQueryWrapper.eq(RoomLabel::getRoomId, roomSubmitVo.getId());
            roomLabelService.remove(roomLabelQueryWrapper);
//          删除属性信息列表
            LambdaQueryWrapper<RoomAttrValue> roomAttrValueQueryWrapper = new LambdaQueryWrapper<>();
            roomAttrValueQueryWrapper.eq(RoomAttrValue::getAttrValueId, roomSubmitVo.getId());
            roomAttrValueService.remove(roomAttrValueQueryWrapper);
//            删除支付方式列表
            LambdaQueryWrapper<RoomPaymentType> roomPaymentQueryWrapper = new LambdaQueryWrapper<>();
            roomPaymentQueryWrapper.eq(RoomPaymentType::getRoomId, roomSubmitVo.getId());
            roomPaymentTypeService.remove(roomPaymentQueryWrapper);
//            删除可选租期列表
            LambdaQueryWrapper<RoomLeaseTerm> roomLeaseQueryWrapper = new LambdaQueryWrapper<>();
            roomLeaseQueryWrapper.eq(RoomLeaseTerm::getRoomId, roomSubmitVo.getId());
            roomLeaseTermService.remove(roomLeaseQueryWrapper);

        }
        //        保存房间图片信息
        List<GraphVo> graphVoList = roomSubmitVo.getGraphVoList();
        if (!CollectionUtils.isEmpty(graphVoList)){
            ArrayList<GraphInfo> graphInfos = new ArrayList<>();
            for (GraphVo graphVo : graphVoList) {
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setName(graphVo.getName());
                graphInfo.setUrl(graphVo.getUrl());
                graphInfo.setItemId(roomSubmitVo.getId());
                graphInfo.setItemType(ItemType.ROOM);
                graphInfos.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfos);
        }


//       保存配套信息列表
        List<Long> facilityInfoIds = roomSubmitVo.getFacilityInfoIds();
        if (!CollectionUtils.isEmpty(facilityInfoIds)){
            ArrayList<RoomFacility> roomFacilities = new ArrayList<>();
            for (Long facilityInfoId : facilityInfoIds) {
                RoomFacility build = RoomFacility.builder().build();
                build.setRoomId(roomSubmitVo.getId());
                build.setFacilityId(facilityInfoId);
                roomFacilities.add(build);
            }
            roomFacilityService.saveBatch(roomFacilities);
        }
//        保存标签信息列表
        List<Long> labelInfoIds = roomSubmitVo.getLabelInfoIds();
        if (!CollectionUtils.isEmpty(labelInfoIds)){
            ArrayList<RoomLabel> roomLabels = new ArrayList<>();
            for (Long labelInfoId : labelInfoIds) {
                RoomLabel build = RoomLabel.builder().build();
                build.setRoomId(roomSubmitVo.getId());
                build.setLabelId(labelInfoId);
                roomLabels.add(build);
            }
            roomLabelService.saveBatch(roomLabels);
        }

//        保存属性信息列表
        List<Long> attrValueIds = roomSubmitVo.getAttrValueIds();
        if (!CollectionUtils.isEmpty(attrValueIds)){
            ArrayList<RoomAttrValue> roomAttrValues = new ArrayList<>();
            for (Long attrValueId : attrValueIds) {
                RoomAttrValue build = RoomAttrValue.builder().build();
                build.setRoomId(roomSubmitVo.getId());
                build.setAttrValueId(attrValueId);
                roomAttrValues.add(build);
            }
            roomAttrValueService.saveBatch(roomAttrValues);
        }

//        保存支付方式列表
        List<Long> paymentTypeIds = roomSubmitVo.getPaymentTypeIds();
        if (!CollectionUtils.isEmpty(paymentTypeIds)){
            ArrayList<RoomPaymentType> roomPaymentTypes = new ArrayList<>();
            for (Long paymentTypeId : paymentTypeIds) {
                RoomPaymentType build = RoomPaymentType.builder().build();
                build.setRoomId(roomSubmitVo.getId());
                build.setPaymentTypeId(paymentTypeId);
                roomPaymentTypes.add(build);
            }
            roomPaymentTypeService.saveBatch(roomPaymentTypes);
        }

//        保存可选租期列表
        List<Long> leaseTermIds = roomSubmitVo.getLeaseTermIds();
        if (!CollectionUtils.isEmpty(leaseTermIds)){
            ArrayList<RoomLeaseTerm> roomLeaseTerms = new ArrayList<>();
            for (Long leaseTermId : leaseTermIds) {
                RoomLeaseTerm roomLeaseTerm = new RoomLeaseTerm();
                roomLeaseTerm.setRoomId(roomSubmitVo.getId());
                roomLeaseTerm.setLeaseTermId(leaseTermId);
                roomLeaseTerms.add(roomLeaseTerm);
            }
            roomLeaseTermService.saveBatch(roomLeaseTerms);
        }
    }

    @Override
    public IPage<RoomItemVo> pageItem(IPage<RoomItemVo> roomItemVoPage, RoomQueryVo queryVo) {
        IPage<RoomItemVo> roomItemVoIPage =roomInfoMapper.pageItem(roomItemVoPage,queryVo);
        return roomItemVoIPage;
    }

    @Override
    public void removeByRoomByid(Long id) {
        String key = RedisConstant.APP_ROOM_PREFIX + id;
        RoomDetailVo roomDetailVo = (RoomDetailVo) redisTemplate.opsForValue().get(key);
        if (id==null){
            throw new LeaseException(ResultCodeEnum.DATA_ERROR);
        }
        super.removeById(id);
//            删除房间图片信息
            LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
            graphInfoQueryWrapper.eq(GraphInfo::getItemId, id);
            graphInfoQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
            graphInfoService.remove(graphInfoQueryWrapper);
//            删除配套信息列表
            LambdaQueryWrapper<RoomFacility> facilityInfoQueryWrapper = new LambdaQueryWrapper<>();
            facilityInfoQueryWrapper.eq(RoomFacility::getRoomId, id);
            roomFacilityService.remove(facilityInfoQueryWrapper);
//            删除标签信息列表
            LambdaQueryWrapper<RoomLabel> roomLabelQueryWrapper = new LambdaQueryWrapper<>();
            roomLabelQueryWrapper.eq(RoomLabel::getRoomId, id);
            roomLabelService.remove(roomLabelQueryWrapper);
//          删除属性信息列表
            LambdaQueryWrapper<RoomAttrValue> roomAttrValueQueryWrapper = new LambdaQueryWrapper<>();
            roomAttrValueQueryWrapper.eq(RoomAttrValue::getAttrValueId, id);
            roomAttrValueService.remove(roomAttrValueQueryWrapper);
//            删除支付方式列表
            LambdaQueryWrapper<RoomPaymentType> roomPaymentQueryWrapper = new LambdaQueryWrapper<>();
            roomPaymentQueryWrapper.eq(RoomPaymentType::getRoomId, id);
            roomPaymentTypeService.remove(roomPaymentQueryWrapper);
//            删除可选租期列表
            LambdaQueryWrapper<RoomLeaseTerm> roomLeaseQueryWrapper = new LambdaQueryWrapper<>();
            roomLeaseQueryWrapper.eq(RoomLeaseTerm::getRoomId, id);
            roomLeaseTermService.remove(roomLeaseQueryWrapper);

    }

    @Override
    public RoomDetailVo getDetailById(Long id) {
        RoomInfo roomInfo = roomInfoMapper.selectById(id);
        ApartmentInfo apartmentInfo =apartmentInfoMapper.selectListApartmentId(id);
        List<GraphVo> graphVoList= graphInfoMapper.selectListByItemTypeAndId(ItemType.ROOM, id);

        List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByFacilityByRoomId(id);
        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByRoomId(id);

        List<AttrValueVo> attrValueVoList = attrValueMapper.selectListAttrValueId(id);
        List<PaymentType> paymentTypeList = paymentTypeMapper.selectListPaymentId(id);
        List<LeaseTerm> leaseTermList = leaseTermMapper.selectListLeaseId(id);
        RoomDetailVo roomDetailVo = new RoomDetailVo();
        BeanUtils.copyProperties(roomInfo,roomDetailVo);
        roomDetailVo.setApartmentInfo(apartmentInfo);
        roomDetailVo.setGraphVoList(graphVoList);
        roomDetailVo.setFacilityInfoList(facilityInfoList);
        roomDetailVo.setLabelInfoList(labelInfoList);
        roomDetailVo.setAttrValueVoList(attrValueVoList);
        roomDetailVo.setPaymentTypeList(paymentTypeList);
        roomDetailVo.setLeaseTermList(leaseTermList);
        return roomDetailVo;
    }


}




