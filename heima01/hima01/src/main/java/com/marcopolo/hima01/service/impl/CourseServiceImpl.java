package com.marcopolo.hima01.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marcopolo.hima01.entity.po.Course;
import com.marcopolo.hima01.mapper.CourseMapper;
import com.marcopolo.hima01.service.ICourseService;
import org.springframework.stereotype.Service;


// 学科表 服务实现类: extends ServiceImpl<Mapper,Po> implements IxxServie
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements ICourseService {

}
