package com.example.tax.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tax.entity.Taxpayer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaxpayerMapper extends BaseMapper<Taxpayer> {
}
