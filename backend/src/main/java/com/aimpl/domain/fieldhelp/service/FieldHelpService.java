package com.aimpl.domain.fieldhelp.service;

import com.aimpl.domain.fieldhelp.dto.FieldHelpCreateDTO;
import com.aimpl.domain.fieldhelp.entity.FieldHelpContent;
import com.aimpl.domain.fieldhelp.mapper.FieldHelpContentMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FieldHelpService extends ServiceImpl<FieldHelpContentMapper, FieldHelpContent> {

    @Transactional
    public FieldHelpContent createHelp(FieldHelpCreateDTO dto) {
        FieldHelpContent help = new FieldHelpContent();
        help.setPage(dto.getPage());
        help.setFieldName(dto.getFieldName());
        help.setHelpText(dto.getHelpText());
        help.setFormatExample(dto.getFormatExample());
        help.setCommonErrors(dto.getCommonErrors());
        help.setRelatedVideoClipId(dto.getRelatedVideoClipId());
        help.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        help.setEnabled(true);
        save(help);
        return help;
    }

    public FieldHelpContent getHelp(String page, String fieldName) {
        return getOne(new LambdaQueryWrapper<FieldHelpContent>()
                .eq(FieldHelpContent::getPage, page)
                .eq(FieldHelpContent::getFieldName, fieldName)
                .eq(FieldHelpContent::getEnabled, true)
                .last("LIMIT 1"));
    }

    public List<FieldHelpContent> getPageHelps(String page) {
        return list(new LambdaQueryWrapper<FieldHelpContent>()
                .eq(FieldHelpContent::getPage, page)
                .eq(FieldHelpContent::getEnabled, true)
                .orderByAsc(FieldHelpContent::getSortOrder));
    }
}
