package com.ing.fmjavaguild.batch.domain.securities;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class Security {
    private String tickerSymbol;
    private String security;
    private String secFilings;
    private String gicsSector;
    private String gicsSubIndustry;
    private String headquartersAddress;
    private Date dateFirstAdded;
    private String cik;
}
