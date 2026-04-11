package com.aimpl.domain.dispatch.dto;

import com.aimpl.common.enums.IndustryType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DispatchRequestDTO {

    private Long projectId;

    private String customerName;

    private IndustryType industryType;

    private String scale;

    private List<String> modules;

    @Size(max = 500, message = "特殊需求长度不能超过500")
    private String specialRequirements;

    private LocalDate expectedStartDate;

    private String region;

    @AssertTrue(message = "必须提供projectId或者(customerName+industryType+scale)")
    public boolean isValid() {
        if (projectId != null) {
            return true;
        }
        return customerName != null && !customerName.isBlank()
                && industryType != null
                && scale != null && !scale.isBlank();
    }
}
