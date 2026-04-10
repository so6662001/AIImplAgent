package com.aimpl.domain.org.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.org.dto.EmployeeCreateDTO;
import com.aimpl.domain.org.entity.Department;
import com.aimpl.domain.org.entity.Employee;
import com.aimpl.domain.org.mapper.DepartmentMapper;
import com.aimpl.domain.org.mapper.EmployeeMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeService extends ServiceImpl<EmployeeMapper, Employee> {

    private final DepartmentMapper departmentMapper;

    @Transactional
    public Employee createEmployee(EmployeeCreateDTO dto) {
        if (count(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getEmployeeCode, dto.getEmployeeCode())) > 0) {
            throw new BizException("员工编码已存在: " + dto.getEmployeeCode());
        }

        if (departmentMapper.selectById(dto.getDeptId()) == null) {
            throw new BizException("部门不存在: " + dto.getDeptId());
        }

        Employee e = new Employee();
        e.setEmployeeCode(dto.getEmployeeCode());
        e.setName(dto.getName());
        e.setGender(dto.getGender());
        e.setIdCard(dto.getIdCard());
        e.setPhone(dto.getPhone());
        e.setEmail(dto.getEmail());
        e.setDeptId(dto.getDeptId());
        e.setPosition(dto.getPosition());
        e.setJoinDate(dto.getJoinDate());
        e.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        save(e);
        return e;
    }
}
