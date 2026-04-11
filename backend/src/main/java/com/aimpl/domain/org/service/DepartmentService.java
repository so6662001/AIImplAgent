package com.aimpl.domain.org.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.org.dto.DepartmentCreateDTO;
import com.aimpl.domain.org.entity.Department;
import com.aimpl.domain.org.mapper.DepartmentMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentService extends ServiceImpl<DepartmentMapper, Department> {

    @Transactional
    public Department createDepartment(DepartmentCreateDTO dto) {
        if (count(new LambdaQueryWrapper<Department>()
                .eq(Department::getDeptCode, dto.getDeptCode())) > 0) {
            throw new BizException("部门编码已存在: " + dto.getDeptCode());
        }

        if (dto.getParentId() != null && getById(dto.getParentId()) == null) {
            throw new BizException("上级部门不存在: " + dto.getParentId());
        }

        Department d = new Department();
        d.setDeptCode(dto.getDeptCode());
        d.setDeptName(dto.getDeptName());
        d.setParentId(dto.getParentId());
        d.setManagerId(dto.getManagerId());
        d.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        d.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        save(d);
        return d;
    }
}
