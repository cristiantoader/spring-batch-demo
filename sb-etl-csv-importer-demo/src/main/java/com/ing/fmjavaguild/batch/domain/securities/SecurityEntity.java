package com.ing.fmjavaguild.batch.domain.securities;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "javafm_sb_securities")
@Data
@NoArgsConstructor
public class SecurityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tickerSymbol;

    @Column(nullable = false)
    private String security;

    private String secFilings;
    private String gicsSector;
    private String gicsSubIndustry;
    private String headquartersAddress;
    private Date dateFirstAdded;
    private String cik;
}
