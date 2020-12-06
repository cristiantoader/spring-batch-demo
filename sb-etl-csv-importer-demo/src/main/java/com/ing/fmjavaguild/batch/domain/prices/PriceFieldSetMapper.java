package com.ing.fmjavaguild.batch.domain.prices;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

public class PriceFieldSetMapper implements FieldSetMapper<Price> {

    @Override
    public Price mapFieldSet(FieldSet fieldSet) {
        Price price = new Price();
        price.setSymbol(fieldSet.readString("symbol"));
        price.setDate(fieldSet.readString("date"));
        price.setOpen(fieldSet.readDouble("open"));
        price.setClose(fieldSet.readDouble("close"));
        price.setHigh(fieldSet.readDouble("high"));
        price.setLow(fieldSet.readDouble("low"));
        price.setVolume(fieldSet.readLong("volume"));
        return price;
    }
}
