package com.aimpl.domain.training.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.training.dto.RequiredCourseCreateDTO;
import com.aimpl.domain.training.dto.RequiredCourseUpdateDTO;
import com.aimpl.domain.training.entity.RequiredCourse;
import com.aimpl.domain.training.mapper.RequiredCourseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequiredCourseService extends ServiceImpl<RequiredCourseMapper, RequiredCourse> {

    private static final List<String> VALID_INDUSTRY_TYPES = Arrays.asList(
            "STEEL_TRADER", "STEEL_MILL", "PROCESSING_CENTER", "INTEGRATED_SERVICE");

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
        LambdaQueryWrapper<RequiredCourse> qw = new LambdaQueryWrapper<>();
        if (industryType != null && !industryType.isBlank()) {
            qw.eq(RequiredCourse::getIndustryType, industryType);
        }
        qw.orderByAsc(RequiredCourse::getIndustryType)
          .orderByAsc(RequiredCourse::getSortOrder);
        return list(qw);
    }

    @Transactional
    public RequiredCourse createCourse(RequiredCourseCreateDTO dto) {
        if (!VALID_INDUSTRY_TYPES.contains(dto.getIndustryType())) {
            throw new BizException("无效的行业类型: " + dto.getIndustryType()
                    + "，有效值: " + String.join(", ", VALID_INDUSTRY_TYPES));
        }

        boolean dup = count(new LambdaQueryWrapper<RequiredCourse>()
                .eq(RequiredCourse::getIndustryType, dto.getIndustryType())
                .eq(RequiredCourse::getCourseModule, dto.getCourseModule())) > 0;
        if (dup) {
            throw new BizException("该行业类型下课程模块已存在: "
                    + dto.getIndustryType() + " / " + dto.getCourseModule());
        }

        RequiredCourse course = new RequiredCourse();
        course.setIndustryType(dto.getIndustryType());
        course.setCourseModule(dto.getCourseModule());
        course.setKaRequired(dto.getKaRequired() != null ? dto.getKaRequired() : false);
        course.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        save(course);
        return course;
    }

    @Transactional
    public RequiredCourse updateCourse(Long id, RequiredCourseUpdateDTO dto) {
        RequiredCourse course = getById(id);
        if (course == null) {
            throw new BizException("课程配置不存在: " + id);
        }
        course.setKaRequired(dto.getKaRequired());
        if (dto.getSortOrder() != null) {
            course.setSortOrder(dto.getSortOrder());
        }
        updateById(course);
        return course;
    }

    @Transactional
    public void deleteCourse(Long id) {
        RequiredCourse course = getById(id);
        if (course == null) {
            throw new BizException("课程配置不存在: " + id);
        }
        removeById(id);
    }

    /**
     * 批量设置某行业类型下指定课程模块为必学/选学。
     */
    @Transactional
    public void batchToggle(String industryType, List<String> courseModules, boolean kaRequired) {
        if (!VALID_INDUSTRY_TYPES.contains(industryType)) {
            throw new BizException("无效的行业类型: " + industryType);
        }
        if (courseModules == null || courseModules.isEmpty()) {
            throw new BizException("课程模块列表不能为空");
        }

        List<RequiredCourse> courses = list(new LambdaQueryWrapper<RequiredCourse>()
                .eq(RequiredCourse::getIndustryType, industryType)
                .in(RequiredCourse::getCourseModule, courseModules));

        for (RequiredCourse c : courses) {
            c.setKaRequired(kaRequired);
        }
        updateBatchById(courses);
    }
}
