package com.ing.fmjavaguild.batch.domain.prices;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

/*
 * Cheating so i don't have to write ddl for this table
 */
@Entity
@Table(name = "javafm_sb_prices")
@Data
@NoArgsConstructor
public class PriceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Date date;

    @Column(nullable = false)
    @NotEmpty
    private String symbol;

    private Double open;
    private Double close;
    private Double high;
    private Double low;
    private Long volume;
}
