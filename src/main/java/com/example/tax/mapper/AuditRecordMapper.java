package com.example.tax.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tax.entity.AuditRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditRecordMapper extends BaseMapper<AuditRecord> {
}
