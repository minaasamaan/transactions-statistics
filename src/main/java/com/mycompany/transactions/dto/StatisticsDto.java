package com.mycompany.transactions.dto;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public class StatisticsDto {

    private long   count;
    private String sum;
    private String avg;
    private String min;
    private String max;

    public StatisticsDto(long count, BigDecimal sum, BigDecimal avg, BigDecimal min, BigDecimal max) {
        this.count = count;
        this.sum = sum.toString();
        this.avg = avg.toString();
        this.min = min.toString();
        this.max = max.toString();
    }
}
