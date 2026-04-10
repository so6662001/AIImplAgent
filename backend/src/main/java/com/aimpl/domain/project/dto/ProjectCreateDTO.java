package com.aimpl.domain.project.dto;

import com.aimpl.common.enums.IndustryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProjectCreateDTO {

    @NotBlank(message = "项目编号不能为空")
    @Size(max = 32, message = "项目编号长度不能超过32位")
    private String projectCode;

    @NotBlank(message = "客户名称不能为空")
    @Size(max = 100, message = "客户名称长度不能超过100")
    private String customerName;

    @NotNull(message = "行业类型不能为空")
    private IndustryType industryType;

    @NotBlank(message = "企业规模不能为空")
    @Size(max = 20, message = "企业规模长度不能超过20")
    private String scale;

    @NotNull(message = "请指定项目经理")
    private Long pmId;

    private List<String> modules;

    @Size(max = 50, message = "区域长度不能超过50")
    private String region;

    private LocalDate startDate;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
