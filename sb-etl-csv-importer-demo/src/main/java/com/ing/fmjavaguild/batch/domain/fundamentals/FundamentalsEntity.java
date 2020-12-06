package com.ing.fmjavaguild.batch.domain.fundamentals;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "javafm_sb_fundamentals")
@Data
@NoArgsConstructor
public class FundamentalsEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotEmpty
    private String tickerSymbol;

    private Date periodEnding;
    private Double accountsPayable;
    private Double accountsReceivable;
    private Double addlIncomeExpenseItems;
    private Double afterTaxRoe;
    private Double capitalExpenditures;
    private Double capitalSurplus;
    private Double cashRatio;
    private Double cashAndCashEquivalents;
    private Double changeInInventories;
    private Double commonStocks;
    private Double costOfRevenue;
    private Double currentRatio;
    private Double deferredLiabilityCharges;
    private Double depreciation;
    private Double earningsBeforeInterestandTax;
    private Double earningsBeforeTax;
    private Double effectofExchangeRate;
    private Double equityEarningsLossUnconsolidatedSubsidiary;
    private Double fixedAssets;
    private Double goodwill;
    private Double grossMargin;
    private Double grossProfit;
    private Double incomeTax;
    private Double intangibleAssets;
    private Double interestExpense;
    private Double inventory;
    private Double investments;
    private Double liabilities;
    private Double longTermDebt;
    private Double longTermInvestments;
    private Double minorityInterest;
    private Double miscStocks;
    private Double netBorrowings;
    private Double netCashFlow;
    private Double netCashFlowOperating;
    private Double netCashFlowsFinancing;
    private Double netCashFlowsInvesting;
    private Double netIncome;
    private Double netIncomeAdjustments;
    private Double netIncomeApplicabletoCommonShareholders;
    private Double netIncomeContOperations;
    private Double netReceivables;
    private Double nonRecurringItems;
    private Double operatingIncome;
    private Double operatingMargin;
    private Double otherAssets;
    private Double otherCurrentAssets;
    private Double otherCurrentLiabilities;
    private Double otherEquity;
    private Double otherFinancingActivities;
    private Double otherInvestingActivities;
    private Double otherLiabilities;
    private Double otherOperatingActivities;
    private Double otherOperatingItems;
    private Double preTaxMargin;
    private Double preTaxROE;
    private Double profitMargin;
    private Double quickRatio;
    private Double researchandDevelopment;
    private Double retainedEarnings;
    private Double saleandPurchaseofStock;
    private Double sales;
    private Double generalandAdmin;
    private Double shortTermDebtCurrentPortionofLongTermDebt;
    private Double shortTermInvestments;
    private Double totalAssets;
    private Double totalCurrentAssets;
    private Double totalCurrentLiabilities;
    private Double totalEquity;
    private Double totalLiabilities;
    private Double totalLiabilitiesEquity;
    private Double totalRevenue;
    private Double treasuryStock;
    private Double forYear;
    private Double earningsPerShare;
    private Double estimatedSharesOutstanding;
}
