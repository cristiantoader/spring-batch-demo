package com.ing.fmjavaguild.worker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    private Long id;
    private String ticker;
    private String security;
    private Integer year;
    private Double eps;
    private Double yearGrossProfit;
    private Double yearTotalAssets;
    private Double yearTotalLiabilities;
    private Double yearMaxHigh;
    private Double yearMinLow;
}
