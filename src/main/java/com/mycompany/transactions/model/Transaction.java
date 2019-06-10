package com.mycompany.transactions.model;

import java.math.BigDecimal;
import java.time.Instant;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Transaction holder with the following attributes:
 * <ul>
 * <li>amount – transaction amount; a string of arbitrary length that is parsable as a BigDecimal
 <li>timestamp – transaction time in the ISO 8601 format YYYY-MM-DDThh:mm:ss.sssZ in the UTC timezone (this is not the current timestamp)
 */
@AllArgsConstructor
@Getter
@ToString
public class Transaction {
    @NotNull
    private BigDecimal amount;
    @NotNull
    private Instant    timestamp;
}
