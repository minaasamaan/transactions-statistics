package com.mycompany.transactions.manager;

import com.mycompany.transactions.model.BigDecimalSummaryStatistics;
import com.mycompany.transactions.model.Transaction;

/**
 * A <b>Manager</b> Responsible for basic transactions operations, like creation, statistics retrieval and resetting current statistics
 */
public interface TransactionsManager {

    /**
     * Creates a new Transaction by accumulating its value to statistics repository
     * @param transaction
     *
     * @throws com.mycompany.transactions.exception.TransactionProcessingException if passed transaction in future or
     * its {@code timestamp} older than 60 seconds.
     */
    void createTransaction(Transaction transaction);

    /**
     * Gets current transactions statistics for the last 60 seconds.
     * @return {@link BigDecimalSummaryStatistics}
     */
    BigDecimalSummaryStatistics getTransactionStatistics();

    /**
     * Reset the statistics state by deleting all existing transactions from the statistics repository
     */
    void resetAll();
}
