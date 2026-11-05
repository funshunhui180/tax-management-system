package com.example.tax.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tax.entity.Declaration;
import com.example.tax.mapper.DeclarationMapper;
import com.example.tax.service.DeclarationService;
import org.springframework.stereotype.Service;

@Service
public class DeclarationServiceImpl extends ServiceImpl<DeclarationMapper, Declaration> implements DeclarationService {
}
