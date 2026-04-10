package com.aimpl.domain.archive.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.archive.dto.CustomerCreateDTO;
import com.aimpl.domain.archive.entity.Customer;
import com.aimpl.domain.archive.mapper.CustomerMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class CustomerService extends ServiceImpl<CustomerMapper, Customer> {

    public Customer createCustomer(CustomerCreateDTO dto) {
        if (count(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getCustomerCode, dto.getCustomerCode())) > 0) {
            throw new BizException("客户编码已存在: " + dto.getCustomerCode());
        }
        if (dto.getFullName() != null && count(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getFullName, dto.getFullName())) > 0) {
            throw new BizException("客户名称已存在: " + dto.getFullName());
        }

        Customer c = new Customer();
        c.setCustomerCode(dto.getCustomerCode());
        c.setFullName(dto.getFullName());
        c.setShortName(dto.getShortName());
        c.setCustomerType(dto.getCustomerType());
        c.setCreditCode(dto.getCreditCode());
        c.setContact(dto.getContact());
        c.setPhone(dto.getPhone());
        c.setProvince(dto.getProvince());
        c.setCity(dto.getCity());
        c.setDistrict(dto.getDistrict());
        c.setAddress(dto.getAddress());
        c.setCategory(dto.getCategory());
        c.setSettlementMethod(dto.getSettlementMethod());
        c.setCreditLimit(dto.getCreditLimit());
        c.setTaxRate(dto.getTaxRate());
        c.setSalesRepId(dto.getSalesRepId());
        c.setEnabled(true);
        save(c);
        return c;
    }
}
