package com.mycompany.transactions.exception;

public enum ProcessingError {
    OLD_TRANSACTION(204),
    FUTURE_TRANSACTION(422),
    UNPARSEABLE_TRANSACTION(422);

    private int httpStatus;

    ProcessingError(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
