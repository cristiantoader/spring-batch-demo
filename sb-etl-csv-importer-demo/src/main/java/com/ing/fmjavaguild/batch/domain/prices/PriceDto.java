package com.ing.fmjavaguild.batch.domain.prices;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class PriceDto {
    private Date date;
    private String symbol;
    private Double open;
    private Double close;
    private Double low;
    private Double high;
    private Long volume;
}
