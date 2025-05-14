package com.marcopolo.hima01.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marcopolo.hima01.entity.po.School;
import com.marcopolo.hima01.mapper.SchoolMapper;
import com.marcopolo.hima01.service.ISchoolService;
import org.springframework.stereotype.Service;

@Service
public class SchoolServiceImpl extends ServiceImpl<SchoolMapper, School> implements ISchoolService {
}
