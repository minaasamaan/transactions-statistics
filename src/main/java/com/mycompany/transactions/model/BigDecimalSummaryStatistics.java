package com.mycompany.transactions.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Like {@code DoubleSummaryStatistics} but for {@link BigDecimal}.
 *
 * <ul> Implementation quoted from https://stackoverflow.com/questions/51645432/bigdecimal-summary-statistics,
 * however was adjusted to meet the acceptance criteria.
 */
public class BigDecimalSummaryStatistics implements Consumer<BigDecimal> {

    private BigDecimal sum = BigDecimal.ZERO, min = BigDecimal.ZERO, max = BigDecimal.ZERO;
    private long count;
    private int          scale        = 2;
    private RoundingMode roundingMode = RoundingMode.HALF_UP;

    public BigDecimalSummaryStatistics(int scale,
                                       RoundingMode roundingMode) {
        this.scale = scale;
        this.roundingMode = roundingMode;
    }

    public void reset() {
        sum = BigDecimal.ZERO;
        min = BigDecimal.ZERO;
        max = BigDecimal.ZERO;
        count = 0;
    }

    @Override
    public void accept(BigDecimal t) {
        if (count == 0) {
            Objects.requireNonNull(t);
            count = 1;
            sum = t;
            min = t;
            max = t;
        }
        else {
            sum = sum.add(t);
            if (min.compareTo(t) > 0) { min = t; }
            if (max.compareTo(t) < 0) { max = t; }
            count++;
        }
    }

    public BigDecimalSummaryStatistics combine(BigDecimalSummaryStatistics otherSummaryStatistics) {
        if (otherSummaryStatistics.count > 0) {
            if (count == 0) {
                count = otherSummaryStatistics.count;
                sum = otherSummaryStatistics.sum;
                min = otherSummaryStatistics.min;
                max = otherSummaryStatistics.max;
            }
            else {
                sum = sum.add(otherSummaryStatistics.sum);
                if (min.compareTo(otherSummaryStatistics.min) > 0) { min = otherSummaryStatistics.min; }
                if (max.compareTo(otherSummaryStatistics.max) < 0) { max = otherSummaryStatistics.max; }
                count += otherSummaryStatistics.count;
            }
        }
        return this;
    }

    public long getCount() {
        return count;
    }

    public BigDecimal getSum() {
        return sum.setScale(scale, roundingMode);
    }

    public BigDecimal getAvg() {
        return count < 2 ? sum.setScale(scale, roundingMode) : sum.divide(BigDecimal.valueOf(count),
                                                                          scale,
                                                                          roundingMode);
    }

    public BigDecimal getMin() {
        return min.setScale(scale, roundingMode);
    }

    public BigDecimal getMax() {
        return max.setScale(scale, roundingMode);
    }

    @Override
    public String toString() {
        return count == 0 ? "empty" : (count + " elements between " + min + " and " + max + ", sum=" + sum);
    }
}
