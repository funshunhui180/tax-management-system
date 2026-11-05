package com.example.tax.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tax.entity.Taxpayer;
import com.example.tax.mapper.TaxpayerMapper;
import com.example.tax.service.TaxpayerService;
import org.springframework.stereotype.Service;

@Service
public class TaxpayerServiceImpl extends ServiceImpl<TaxpayerMapper, Taxpayer> implements TaxpayerService {
}
