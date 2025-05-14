package com.marcopolo.hima01.entity.query;

// 课程查询类 适用于工具调用

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

// ToolParam 描述会作为Function参数注解通过提示词发给模型
@Data
public class CourseQuery {

    @ToolParam(description = "课程类型：编程、设计、自媒体、其它", required = false)
    private String type; // 课程类型

    @ToolParam(required = false, description = "学历要求：0-无、1-初中、2-高中、3-大专、4-本科及本科以上")
    private Integer edu; // 学历要求

    @ToolParam(required = false,description = "排序方式")
    private List<Sort> sort;

    @Data
    public static class Sort {

        @ToolParam(required = false, description = "排序字段: price或duration")
        private String field;  // 排序字段

        @ToolParam(required = false, description = "是否是升序: true/false")
        private Boolean asc;   // 是否是升序
    }
}
