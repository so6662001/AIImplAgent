package com.aimpl.domain.accountset.vo;

import lombok.Data;

import java.util.List;

@Data
public class AccountSetRecommendVO {

    private Long projectId;
    private String recommendedSetName;
    private String recommendedAccountingSystem;
    private String recommendedPricingMethod;
    private Boolean recommendedUseWeight;
    private Integer qtyDecimals;
    private Integer wgtDecimals;
    private Integer prcDecimals;
    private Integer amtDecimals;
    private Integer fiscalYearStart;
    private String currency;
    private List<String> recommendations;
    private String confidence;
}
