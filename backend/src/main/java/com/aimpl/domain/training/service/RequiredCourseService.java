package com.aimpl.domain.training.service;

import com.aimpl.domain.training.entity.RequiredCourse;
import com.aimpl.domain.training.mapper.RequiredCourseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequiredCourseService extends ServiceImpl<RequiredCourseMapper, RequiredCourse> {

    public List<String> getRequiredCourseModules(String industryType) {
        return list(new LambdaQueryWrapper<RequiredCourse>()
                .eq(RequiredCourse::getIndustryType, industryType)
                .eq(RequiredCourse::getKaRequired, true)
                .orderByAsc(RequiredCourse::getSortOrder))
                .stream()
                .map(RequiredCourse::getCourseModule)
                .collect(Collectors.toList());
    }

    public List<RequiredCourse> listByIndustryType(String industryType) {
        return list(new LambdaQueryWrapper<RequiredCourse>()
                .eq(RequiredCourse::getIndustryType, industryType)
                .orderByAsc(RequiredCourse::getSortOrder));
    }
}
