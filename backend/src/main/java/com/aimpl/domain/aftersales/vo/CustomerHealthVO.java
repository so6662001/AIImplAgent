package com.aimpl.domain.aftersales.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CustomerHealthVO {

    private Integer healthScore;

    private String healthLevel;

    private Integer ticketCount30d;

    private String ticketTrend;

    private Integer openTicketCount;

    private BigDecimal avgResolutionHours;

    private BigDecimal customerSatisfactionAvg;

    private List<String> careActions;

    private LocalDate checkDate;
}
