package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import com.atguigu.lease.web.admin.vo.fee.FeeValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private FeeValueMapper feeValueMapper;
    @Autowired
    private GraphInfoService graphInfoService;
    @Autowired
    private ApartmentFacilityService apartmentFacilityService;
    @Autowired
    private ApartmentLabelService apartmentLabelService;
    @Autowired
    private ApartmentFeeValueService apartmentFeeValueService;
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Override
    public void saveOrUpdateApartment(ApartmentSubmitVo apartmentSubmitVo) {
        boolean isUpdate=apartmentSubmitVo.getId()!=null;

        super.saveOrUpdate(apartmentSubmitVo);
        if (isUpdate){
//            1.删除图标列表
            LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
            graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId,apartmentSubmitVo.getId());
            graphInfoService.remove(graphInfoLambdaQueryWrapper);
//            2.删除配套列表
            LambdaQueryWrapper<ApartmentFacility> apartmentFacilityLambdaQueryWrapper = new LambdaQueryWrapper<>();
            apartmentFacilityLambdaQueryWrapper.eq(ApartmentFacility::getApartmentId,apartmentSubmitVo.getId());
            apartmentFacilityService.remove(apartmentFacilityLambdaQueryWrapper);
//            3.删除标签列表
            LambdaQueryWrapper<ApartmentLabel> apartmentLabelLambdaQueryWrapper = new LambdaQueryWrapper<>();
            apartmentLabelLambdaQueryWrapper.eq(ApartmentLabel::getApartmentId,apartmentSubmitVo.getId());
            apartmentLabelService.remove(apartmentLabelLambdaQueryWrapper);
//            4.删除杂费列表
            LambdaQueryWrapper<ApartmentFeeValue> feeKeyLambdaQueryWrapper = new LambdaQueryWrapper<>();
            feeKeyLambdaQueryWrapper.eq(ApartmentFeeValue::getApartmentId,apartmentSubmitVo.getId());
            apartmentFeeValueService.remove(feeKeyLambdaQueryWrapper);
        }
//            1.插入图标列表
        List<GraphVo> graphVoList = apartmentSubmitVo.getGraphVoList();
        if (!CollectionUtils.isEmpty(graphVoList)) {
            ArrayList<GraphInfo> graphInfoArrayList = new ArrayList<>();
            for (GraphVo graphVo : graphVoList) {
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setItemType(ItemType.APARTMENT);
                graphInfo.setItemId(apartmentSubmitVo.getId());
                graphInfo.setName(graphVo.getName());
                graphInfo.setUrl(graphVo.getUrl());
                graphInfoArrayList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfoArrayList);
        }
//            2.插入配套列表
        List<Long> facilityInfoIdList = apartmentSubmitVo.getFacilityInfoIds();
        if (!CollectionUtils.isEmpty(facilityInfoIdList)){
            ArrayList<ApartmentFacility> apartmentFacilities = new ArrayList<>();
            for (Long l : facilityInfoIdList) {
                ApartmentFacility build = ApartmentFacility.builder().build();
                build.setFacilityId(l);
                build.setApartmentId(apartmentSubmitVo.getId());
                apartmentFacilities.add(build);
            }
            apartmentFacilityService.saveBatch(apartmentFacilities);
        }
//            3.插入标签列表
        List<Long> labelIds = apartmentSubmitVo.getLabelIds();
        ArrayList<ApartmentLabel> apartmentLabels = new ArrayList<>();
        if (!CollectionUtils.isEmpty(labelIds)){
            for (Long labelId : labelIds) {
                ApartmentLabel build = ApartmentLabel.builder().build();
                build.setApartmentId(apartmentSubmitVo.getId());
                build.setLabelId(labelId);
                apartmentLabels.add(build);
            }
        }
        apartmentLabelService.saveBatch(apartmentLabels);
//            4.插入杂费列表
        List<Long> feeValueIds = apartmentSubmitVo.getFeeValueIds();
        ArrayList<ApartmentFeeValue> apartmentFeeValues= new ArrayList<>();
        if (!CollectionUtils.isEmpty(facilityInfoIdList))
        {
            for (Long feeValueId : feeValueIds) {
                ApartmentFeeValue build = ApartmentFeeValue.builder().build();
                build.setFeeValueId(feeValueId);
                build.setApartmentId(apartmentSubmitVo.getId());
                apartmentFeeValues.add(build);
            }
        }
        apartmentFeeValueService.saveBatch(apartmentFeeValues);
    }

    @Override
    public IPage<ApartmentItemVo> pageItems(IPage<ApartmentItemVo> page, ApartmentQueryVo queryVo) {
        return apartmentInfoMapper.pageItems(page,queryVo);
    }

    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        //1.查询公寓的信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);

        //2.查询你图片列表
        List<GraphVo> graphVolist =graphInfoMapper.selectListByItemTypeAndId(ItemType.APARTMENT,id);
        //3.查询标签列表
        List<LabelInfo> labelInfoList=labelInfoMapper.selectListByApartmentId(id);
        //4.查询配套列表
        List<FacilityInfo> facilityInfoList=facilityInfoMapper.selectListByFacilityByApartmentId(id);
        //5.查询杂费列表
        List<FeeValueVo> feeValueVoList=feeValueMapper.selectListByFeeByApartmentId(id);
        //6.组装结果
        ApartmentDetailVo apartmentDetailVo = new ApartmentDetailVo();
        BeanUtils.copyProperties(apartmentInfo,apartmentDetailVo);
        apartmentDetailVo.setGraphVoList(graphVolist);
        apartmentDetailVo.setLabelInfoList(labelInfoList);
        apartmentDetailVo.setFacilityInfoList(facilityInfoList);
        apartmentDetailVo.setFeeValueVoList(feeValueVoList);
        return apartmentDetailVo;
    }

    @Override
    public void removeByApartmentByid(Long id) {

        LambdaQueryWrapper<RoomInfo> roomInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomInfoLambdaQueryWrapper.eq(RoomInfo::getApartmentId,id);
        Long count = roomInfoMapper.selectCount(roomInfoLambdaQueryWrapper);
        if (count>0){
//            终止删除，给用户返回一个提示信息
            throw new LeaseException(ResultCodeEnum.ADMIN_APARTMENT_DELETE_ERROR);
        }else {
            super.removeById(id);
//            1.删除图标列表
            LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
            graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId,id);
            graphInfoService.remove(graphInfoLambdaQueryWrapper);
//            2.删除配套列表
            LambdaQueryWrapper<ApartmentFacility> apartmentFacilityLambdaQueryWrapper = new LambdaQueryWrapper<>();
            apartmentFacilityLambdaQueryWrapper.eq(ApartmentFacility::getApartmentId,id);
            apartmentFacilityService.remove(apartmentFacilityLambdaQueryWrapper);
//            3.删除标签列表
            LambdaQueryWrapper<ApartmentLabel> apartmentLabelLambdaQueryWrapper = new LambdaQueryWrapper<>();
            apartmentLabelLambdaQueryWrapper.eq(ApartmentLabel::getApartmentId,id);
            apartmentLabelService.remove(apartmentLabelLambdaQueryWrapper);
//            4.删除杂费列表
            LambdaQueryWrapper<ApartmentFeeValue> feeKeyLambdaQueryWrapper = new LambdaQueryWrapper<>();
            feeKeyLambdaQueryWrapper.eq(ApartmentFeeValue::getApartmentId,id);
            apartmentFeeValueService.remove(feeKeyLambdaQueryWrapper);
        }
    }
}




