package com.mycompany.common;

import com.mycompany.core.AbstractBusinessException;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class BusinessExceptionMatcher<E extends AbstractBusinessException> extends TypeSafeMatcher<E> {

    private final int expectedErrorCode;
    private       int foundErrorCode;

    private BusinessExceptionMatcher(int expectedErrorCode) {
        this.expectedErrorCode = expectedErrorCode;
    }

    public static <E extends AbstractBusinessException> BusinessExceptionMatcher hasCode(int httpCode) {
        return new BusinessExceptionMatcher<E>(httpCode);
    }

    @Override
    protected boolean matchesSafely(final E exception) {
        foundErrorCode = exception.getHttpStatus();
        return foundErrorCode == expectedErrorCode;
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(expectedErrorCode)
                   .appendText(" was expected, but found ")
                   .appendValue(foundErrorCode);
    }
}
