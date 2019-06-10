package com.mycompany.transactions.resource;

import com.mycompany.transactions.dto.TransactionDto;
import com.mycompany.transactions.exception.TransactionProcessingException;
import com.mycompany.transactions.manager.TransactionsManager;
import com.mycompany.transactions.model.Transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;

import javax.validation.Valid;

import static com.mycompany.transactions.exception.ProcessingError.UNPARSEABLE_TRANSACTION;

@RestController("/transactions")
public class TransactionsResource {

    @Autowired
    private TransactionsManager transactionsManager;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createTransaction(@Valid @RequestBody TransactionDto transactionDto) {
        transactionsManager.createTransaction(validateAndTransform(transactionDto));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity deleteAllTransactions() {
        transactionsManager.resetAll();
        return ResponseEntity.noContent().build();
    }

    private Transaction validateAndTransform(TransactionDto transactionDto) {
        try {
            return new Transaction(new BigDecimal(transactionDto.getAmount()),
                                   Instant.parse(transactionDto.getTimestamp()));
        }
        catch (DateTimeParseException | NumberFormatException ex) {
            throw new TransactionProcessingException(UNPARSEABLE_TRANSACTION);
        }
    }
}
