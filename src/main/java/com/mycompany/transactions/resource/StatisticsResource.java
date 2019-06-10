package com.mycompany.transactions.resource;

import com.mycompany.transactions.dto.StatisticsDto;
import com.mycompany.transactions.manager.TransactionsManager;
import com.mycompany.transactions.model.BigDecimalSummaryStatistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/statistics")
public class StatisticsResource {

    @Autowired
    private TransactionsManager transactionsManager;

    @GetMapping
    public ResponseEntity getStatistics() {
        return ResponseEntity.ok(transform(transactionsManager.getTransactionStatistics()));
    }

    private StatisticsDto transform(BigDecimalSummaryStatistics summaryStatistics) {
        return new StatisticsDto(summaryStatistics.getCount(),
                                 summaryStatistics.getSum(),
                                 summaryStatistics.getAvg(),
                                 summaryStatistics.getMin(),
                                 summaryStatistics.getMax());
    }
}
