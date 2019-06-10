package com.mycompany.transactions.manager;

import com.mycompany.transactions.exception.TransactionProcessingException;
import com.mycompany.transactions.model.BigDecimalSummaryStatistics;
import com.mycompany.transactions.model.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.validation.Valid;

import static com.mycompany.transactions.exception.ProcessingError.FUTURE_TRANSACTION;
import static com.mycompany.transactions.exception.ProcessingError.OLD_TRANSACTION;

/**
 * In-memory implementation for {@link TransactionsManager}, where all operations guaranteed to be:
 * <ul>
 * <li> Threadsafe, which means parallel calls should have consistent results. This is ensured by synchronizing
 *      on the {@link BigDecimalSummaryStatistics} 60 objects created upon the Manager object creation, one object per each second.
 * <li> Operating in O(1) as a time & memory complexity, due to the fact that regardless of how many transactions we get,
 *      we accumulate them to 60 different buckets depends on which second of the last minute they belong to.
 * <li> Statistics' values are {@link java.math.BigDecimal} and always contain exactly two decimal places and use
 *      `HALF_ROUND_UP` rounding. eg: 10.345 is returned as 10.35, 10.8 is returned as 10.80
 */
@Service
public class TransactionsManagerImpl implements TransactionsManager {

    private final Log logger = LogFactory.getLog(getClass());

    private static final int                           scale        = 2;
    private static final RoundingMode                  roundingMode = RoundingMode.HALF_UP;
    private final        BigDecimalSummaryStatistics[] statistics   = new BigDecimalSummaryStatistics[60];
    private final        long[]                        lastModified = new long[60];

    public TransactionsManagerImpl() {
        resetAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetAll() {
        for (int i = 0; i < statistics.length; i++) {
            if (statistics[i] != null) {
                synchronized (statistics[i]) {
                    resetEntry(i);
                }
            }
            else {
                statistics[i] = new BigDecimalSummaryStatistics(scale, roundingMode);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createTransaction(@Valid Transaction transaction) {

        long nowEpoch = System.currentTimeMillis();

        if (logger.isTraceEnabled()) {
            logger.trace(String.format("Transaction received at %s with details: %s", Instant.ofEpochMilli(nowEpoch)
                    , transaction));
        }

        long timestamp = transaction.getTimestamp().toEpochMilli();

        if (nowEpoch < timestamp) {
            throw new TransactionProcessingException(FUTURE_TRANSACTION);
        }

        if ((nowEpoch - timestamp) / 1000 >= 60) {
            throw new TransactionProcessingException(OLD_TRANSACTION);
        }

        int second = ZonedDateTime.ofInstant(transaction.getTimestamp(), ZoneOffset.UTC).getSecond();

        synchronized (statistics[second]) {

            BigDecimalSummaryStatistics before = statistics[second];

            //check if existing entry already obsolete, and reset if needed accordingly
            if (lastModified[second] > 0 && (nowEpoch - lastModified[second]) / 1000 >= 60) {
                resetEntry(second);
            }

            //update this second statistics bucket, update last modified
            statistics[second].accept(transaction.getAmount());

            lastModified[second] = timestamp;

            BigDecimalSummaryStatistics after = statistics[second];

            if (logger.isTraceEnabled()) {
                logger.trace(String.format(
                        "Transaction persisted at %s with details: %s and statistics before was: %s while statistics after is: %s",
                        Instant.ofEpochMilli(nowEpoch)
                        ,
                        transaction,
                        before,
                        after));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimalSummaryStatistics getTransactionStatistics() {
        long nowEpoch = System.currentTimeMillis();

        BigDecimalSummaryStatistics result = new BigDecimalSummaryStatistics(scale, roundingMode);

        for (int i = 0; i < statistics.length; i++) {
            synchronized (statistics[i]) {
                if (lastModified[i] > 0 && (nowEpoch - lastModified[i]) / 1000 < 60) {
                    result.combine(statistics[i]);
                }
            }
        }
        return result;
    }

    private void resetEntry(int second) {
        statistics[second].reset();
        lastModified[second] = 0;
    }
}
