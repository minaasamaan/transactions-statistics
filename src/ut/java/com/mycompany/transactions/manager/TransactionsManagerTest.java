package com.mycompany.transactions.manager;

import com.mycompany.common.BusinessExceptionMatcher;
import com.mycompany.transactions.exception.TransactionProcessingException;
import com.mycompany.transactions.model.BigDecimalSummaryStatistics;
import com.mycompany.transactions.model.Transaction;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.math.RoundingMode.HALF_UP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TransactionsManagerTest {

    @Rule
    public  ExpectedException   exceptionRule = ExpectedException.none();
    private TransactionsManager testee        = new TransactionsManagerImpl();

    @Before
    public void before() {
        testee.resetAll();
    }

    @Test
    public void shouldHaveValidEntryState() {
        verifyTransaction(0, 0, 0, 0, 0);
    }

    @Test
    public void shouldAddValidTransactions() {
        testee.createTransaction(new Transaction(BigDecimal.valueOf(200), Instant.now()));
        verifyTransaction(200, 200, 200, 200, 1);
    }

    @Test
    public void shouldRejectOldTransactions() {
        exceptionRule.expect(TransactionProcessingException.class);
        exceptionRule.expect(BusinessExceptionMatcher.hasCode(204));

        testee.createTransaction(new Transaction(BigDecimal.valueOf(200), Instant.now().minusSeconds(61)));
        verifyTransaction(0, 0, 0, 0, 0);

        testee.createTransaction(new Transaction(BigDecimal.valueOf(200), Instant.now().minusSeconds(60)));
        verifyTransaction(0, 0, 0, 0, 0);
    }

    @Test
    public void shouldRejectTransactionsInFuture() {
        exceptionRule.expect(TransactionProcessingException.class);
        exceptionRule.expect(BusinessExceptionMatcher.hasCode(422));

        testee.createTransaction(new Transaction(BigDecimal.valueOf(200), Instant.now().plusSeconds(1)));
        verifyTransaction(0, 0, 0, 0, 0);
    }

    @Test
    public void shouldAccumulateMultipleTransactions() {

        testee.createTransaction(new Transaction(BigDecimal.valueOf(200.5), Instant.now()));
        testee.createTransaction(new Transaction(BigDecimal.valueOf(100.25), Instant.now()));
        testee.createTransaction(new Transaction(BigDecimal.valueOf(50.25), Instant.now()));

        verifyTransaction(117, 200.5, 50.25, 351, 3);
    }

    @Test
    public void shouldAccumulateMultipleParallelTransactions() throws InterruptedException {

        parallelProcess(
                () -> {
                    testee.createTransaction(new Transaction(BigDecimal.valueOf(200.5), Instant.now()));
                    return null;
                },
                () -> {
                    testee.createTransaction(new Transaction(BigDecimal.valueOf(100.25), Instant.now()));
                    return null;
                },
                () -> {
                    testee.createTransaction(new Transaction(BigDecimal.valueOf(50.25), Instant.now()));
                    return null;
                }
        );

        verifyTransaction(117, 200.5, 50.25, 351, 3);
    }

    @Test
    public void getTransactionStatisticsShouldBeIdempotent() {
        testee.createTransaction(new Transaction(BigDecimal.valueOf(200), Instant.now()));
        verifyTransaction(200, 200, 200, 200, 1);
        verifyTransaction(200, 200, 200, 200, 1);
    }

    @Test
    public void shouldHandleUnorderedTimesWithDifferentSeconds() {

        Instant now = Instant.now();

        testee.createTransaction(new Transaction(BigDecimal.valueOf(200.5), now.minusSeconds(1)));

        testee.createTransaction(new Transaction(BigDecimal.valueOf(100.5), now));

        verifyTransaction(150.5, 200.5, 100.5, 301, 2);
    }

    @Test
    public void shouldEliminateStatisticsOlderThanLast60Seconds() throws InterruptedException {

        Instant now = Instant.now();

        testee.createTransaction(new Transaction(BigDecimal.valueOf(200.5), now));

        testee.createTransaction(new Transaction(BigDecimal.valueOf(100.5), now.minusSeconds(59)));

        Thread.sleep(1000);

        verifyTransaction(200.5, 200.5, 200.5, 200.5, 1);
    }

    @Test
    public void shouldHandleUnorderedTimesWithinSameSecond() {

        Instant now = Instant.now();

        testee.createTransaction(new Transaction(BigDecimal.valueOf(200.5), now));

        testee.createTransaction(new Transaction(BigDecimal.valueOf(100.5), now.minusMillis(10)));

        verifyTransaction(150.5, 200.5, 100.5, 301, 2);
    }

    @Test
    public void shouldOverwriteOldTransactionsIfParallelTransactionsWithinSameSecond() throws InterruptedException {
        Instant now = Instant.now();

        testee.createTransaction(new Transaction(BigDecimal.valueOf(100.5), now.minusSeconds(59)));

        Thread.sleep(1000);

        parallelProcess(
                () -> {
                    testee.createTransaction(new Transaction(BigDecimal.valueOf(200.5), now.plusSeconds(1)));
                    return null;
                },
                () -> {
                    testee.createTransaction(new Transaction(BigDecimal.valueOf(100.25), now.plusSeconds(1)));
                    return null;
                },
                () -> {
                    testee.createTransaction(new Transaction(BigDecimal.valueOf(50.25), now.plusSeconds(1)));
                    return null;
                }
        );
        verifyTransaction(117, 200.5, 50.25, 351, 3);
    }

    @SafeVarargs
    private final void parallelProcess(Callable<Void>... callables) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(callables.length);
        executor.invokeAll(Arrays.asList(callables));
    }

    private void verifyTransaction(double avg, double max, double min, double sum, long count) {

        BigDecimalSummaryStatistics summaryStatistics = testee.getTransactionStatistics();

        assertNotNull(summaryStatistics);

        assertEquals(BigDecimal.valueOf(avg).setScale(2, HALF_UP).toString(), summaryStatistics.getAvg().toString());
        assertEquals(BigDecimal.valueOf(max).setScale(2, HALF_UP).toString(), summaryStatistics.getMax().toString());
        assertEquals(BigDecimal.valueOf(min).setScale(2, HALF_UP).toString(), summaryStatistics.getMin().toString());
        assertEquals(BigDecimal.valueOf(sum).setScale(2, HALF_UP).toString(), summaryStatistics.getSum().toString());
        assertEquals(count, summaryStatistics.getCount());
    }
}
