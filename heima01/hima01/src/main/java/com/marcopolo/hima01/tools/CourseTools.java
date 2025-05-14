package com.marcopolo.hima01.tools;


import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.marcopolo.hima01.entity.po.Course;
import com.marcopolo.hima01.entity.po.CourseReservation;
import com.marcopolo.hima01.entity.po.School;
import com.marcopolo.hima01.entity.query.CourseQuery;
import com.marcopolo.hima01.service.ICourseReservationService;
import com.marcopolo.hima01.service.ICourseService;
import com.marcopolo.hima01.service.ISchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

// 课程查询工具
@Component
@RequiredArgsConstructor
public class CourseTools {


    private final ICourseService courseService;
    private final ISchoolService schoolService;
    private final ICourseReservationService courseReservationService;

    @Tool(description = "根据条件查询课程")
    public List<Course> getCourses(@ToolParam(required = false,description = "课程查询条件") CourseQuery query) {
        QueryChainWrapper<Course> wrapper = courseService.query();
        wrapper
                .eq(query.getType() != null, "type", query.getType())
                .le(query.getEdu() != null, "edu", query.getEdu());
        if(query.getSort() != null){
            query.getSort().forEach(sort -> {wrapper.orderBy(true,sort.getAsc(),sort.getField());});
        }
        return wrapper.list();
    }

    @Tool(description = "查询所有校区")
    public List<School> queryAllSchools() {
        return schoolService.list();
    }

    @Tool(description = "生成课程预约单,并返回生成的预约单号")
    public String generateCourseReservation(
            String courseName,String studentName, String contactInfo, String school, String remark){
        CourseReservation courseReservation = new CourseReservation();
        courseReservation.setCourse(courseName);
        courseReservation.setStudentName(studentName);
        courseReservation.setContactInfo(contactInfo);
        courseReservation.setSchool(school);
        courseReservation.setRemark(remark);
        // 写入到数据库
        courseReservationService.save(courseReservation);
        return String.valueOf(courseReservation.getId()); // 返回写入的预约id
    }
}
