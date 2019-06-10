package com.mycompany.transactions.exception;

import com.mycompany.core.AbstractBusinessException;

public class TransactionProcessingException extends AbstractBusinessException {

    private ProcessingError processingError;

    public TransactionProcessingException(ProcessingError processingError) {
        this.processingError = processingError;
    }

    @Override
    public int getHttpStatus() {
        return processingError.getHttpStatus();
    }
}
