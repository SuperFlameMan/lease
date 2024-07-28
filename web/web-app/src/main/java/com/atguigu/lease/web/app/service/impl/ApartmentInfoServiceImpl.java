package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.mapper.*;
import com.atguigu.lease.web.app.service.ApartmentInfoService;
import com.atguigu.lease.web.app.service.LabelInfoService;
import com.atguigu.lease.web.app.service.RoomLabelService;
import com.atguigu.lease.web.app.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Override
    public ApartmentItemVo getApartmentInfoServiceById(Long id) {
//        ApartmentInfo
        ApartmentInfo apartmentInfo = super.getById(id);
//         List<LabelInfo>
        List<LabelInfo> labelInfoList=labelInfoMapper.selectLabelById(id);
//        List<GraphInfo>
        LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId,id);
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
        List<GraphInfo> graphInfos = graphInfoMapper.selectList(graphInfoLambdaQueryWrapper);
        ArrayList<GraphVo> graphVos = new ArrayList<>();
        for (GraphInfo graphInfo : graphInfos) {
            GraphVo graphVo = new GraphVo();
            graphVo.setName(graphInfo.getName());
            graphVo.setUrl(graphInfo.getUrl());
            graphVos.add(graphVo);
        }
//        BigDecimal minRent
        BigDecimal minRent=roomInfoMapper.getMinRent(id);
//        ApartmenItemVo
        ApartmentItemVo apartmentItemVo = new ApartmentItemVo();
        BeanUtils.copyProperties(apartmentInfo,apartmentItemVo);
        apartmentItemVo.setMinRent(minRent);
        apartmentItemVo.setLabelInfoList(labelInfoList);
        apartmentItemVo.setGraphVoList(graphVos);
        return apartmentItemVo;
    }

    @Override
    public ApartmentDetailVo getApartmentDetailById(Long id) {
        List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListAttrValueId(id);
        ApartmentItemVo apartmentInfoServiceById = getApartmentInfoServiceById(id);
        ApartmentDetailVo apartmentDetailVo = new ApartmentDetailVo();
        BeanUtils.copyProperties(apartmentInfoServiceById,apartmentDetailVo);
        apartmentDetailVo.setFacilityInfoList(facilityInfoList);
        return apartmentDetailVo;
    }
}




