package com.aimpl.domain.archive.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.archive.entity.ProductCategory;
import com.aimpl.domain.archive.mapper.ProductCategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ProductCategoryService extends ServiceImpl<ProductCategoryMapper, ProductCategory> {

    public ProductCategory createCategory(ProductCategory category) {
        boolean exists = count(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getCategoryCode, category.getCategoryCode())) > 0;
        if (exists) {
            throw new BizException("品类编码已存在: " + category.getCategoryCode());
        }
        if (category.getParentId() != null && getById(category.getParentId()) == null) {
            throw new BizException("上级品类不存在: " + category.getParentId());
        }
        category.setEnabled(true);
        save(category);
        return category;
    }
}
