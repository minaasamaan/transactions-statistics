package com.mycompany.transactions.dto;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    @NotNull
    private String amount;
    @NotNull
    private String timestamp;
}
