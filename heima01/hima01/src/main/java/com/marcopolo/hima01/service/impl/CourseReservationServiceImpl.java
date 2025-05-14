package com.marcopolo.hima01.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marcopolo.hima01.entity.po.CourseReservation;
import com.marcopolo.hima01.mapper.CourseReservationMapper;
import com.marcopolo.hima01.service.ICourseReservationService;
import org.springframework.stereotype.Service;

@Service
public class CourseReservationServiceImpl extends ServiceImpl<CourseReservationMapper, CourseReservation> implements ICourseReservationService {
}
