package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.mapper.*;
import com.atguigu.lease.web.app.service.*;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.attr.AttrValueVo;
import com.atguigu.lease.web.app.vo.fee.FeeValueVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.atguigu.lease.web.app.vo.room.RoomDetailVo;
import com.atguigu.lease.web.app.vo.room.RoomItemVo;
import com.atguigu.lease.web.app.vo.room.RoomQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.events.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
@Slf4j
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private ApartmentInfoService apartmentInfoService;
    @Autowired
    private GraphInfoService graphInfoService;
    @Autowired
    private AttrValueMapper attrValueMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private PaymentTypeMapper paymentTypeMapper;
    @Autowired
    private LeaseTermMapper leaseTermMapper;
    @Autowired
    private FeeValueMapper feeValueMapper;
    @Autowired
    private BrowsingHistoryService browsingHistoryService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public IPage<RoomItemVo> pageItem(IPage<RoomItemVo> roomItemVoPage, RoomQueryVo queryVo) {
        return roomInfoMapper.pageItem(roomItemVoPage, queryVo);

    }

    @Override
    public IPage<RoomItemVo> pageItemByApartmentId(Page<RoomItemVo> roomItemVoPage, Long id) {

        return roomInfoMapper.pageItemByApartmentId(roomItemVoPage, id);
    }

    @Override
    public RoomDetailVo getDetailById(Long id) {
        String key = RedisConstant.APP_ROOM_PREFIX + id;
        RoomDetailVo roomDetailVo = (RoomDetailVo) redisTemplate.opsForValue().get(key);
        if (roomDetailVo == null) {
            //      RoomInfo
            RoomInfo roomInfo = roomInfoMapper.selectById(id);
            if (roomInfo == null) {
                return null;
            }
//      ApartmentItemVo
            ApartmentItemVo apartmentInfoServiceById = apartmentInfoService.getApartmentInfoServiceById(roomInfo.getApartmentId());
//      List<GraphVo>
            LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
            graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId, id);
            List<GraphInfo> list = graphInfoService.list(graphInfoLambdaQueryWrapper);
            ArrayList<GraphVo> graphVoArrayList = new ArrayList<>();
            for (GraphInfo graphInfo : list) {
                GraphVo graphVo = new GraphVo();
                graphVo.setUrl(graphInfo.getUrl());
                graphVo.setName(graphInfo.getName());
                graphVoArrayList.add(graphVo);
            }
            List<AttrValueVo> attrValueVoList = attrValueMapper.selectListAttrValueId(id);
            List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListAttrValueId(id);
            List<LabelInfo> labelInfoList = labelInfoMapper.selectRoomLabelById(id);
            List<PaymentType> paymentTypeList = paymentTypeMapper.selectListPaymentId(id);
            List<LeaseTerm> leaseTermList = leaseTermMapper.selectListLeaseId(id);
            List<FeeValueVo> feeValueVoList = feeValueMapper.selectListFeeId(roomInfo.getApartmentId());
            roomDetailVo = new RoomDetailVo();
            BeanUtils.copyProperties(roomInfo, roomDetailVo);
            roomDetailVo.setApartmentItemVo(apartmentInfoServiceById);
            roomDetailVo.setGraphVoList(graphVoArrayList);
            roomDetailVo.setAttrValueVoList(attrValueVoList);
            roomDetailVo.setFacilityInfoList(facilityInfoList);
            roomDetailVo.setLabelInfoList(labelInfoList);
            roomDetailVo.setPaymentTypeList(paymentTypeList);
            roomDetailVo.setLeaseTermList(leaseTermList);
            roomDetailVo.setFeeValueVoList(feeValueVoList);
//        System.out.println("获取房间详情"+Thread.currentThread().getName());
            redisTemplate.opsForValue().set(key, roomDetailVo);
        }


//        保存浏览历史
        browsingHistoryService.saveHistory(LoginUserHolder.getLoginUser().getUserId(), id);

        return roomDetailVo;
    }
}




