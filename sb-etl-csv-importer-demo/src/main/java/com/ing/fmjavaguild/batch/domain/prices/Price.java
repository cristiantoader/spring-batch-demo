package com.ing.fmjavaguild.batch.domain.prices;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Price {
    private String date;
    private String symbol;
    private Double open;
    private Double close;
    private Double low;
    private Double high;
    private Long volume;
}
