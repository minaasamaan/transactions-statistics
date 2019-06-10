package com.mycompany.core;

public abstract class AbstractBusinessException extends RuntimeException {
    public abstract int getHttpStatus();
}
