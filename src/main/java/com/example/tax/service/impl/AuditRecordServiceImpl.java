package com.example.tax.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tax.entity.AuditRecord;
import com.example.tax.mapper.AuditRecordMapper;
import com.example.tax.service.AuditRecordService;
import org.springframework.stereotype.Service;

@Service
public class AuditRecordServiceImpl extends ServiceImpl<AuditRecordMapper, AuditRecord> implements AuditRecordService {
}
