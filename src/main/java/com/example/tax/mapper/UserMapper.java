package com.example.tax.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.tax.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
