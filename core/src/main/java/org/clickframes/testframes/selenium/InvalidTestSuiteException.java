package org.clickframes.testframes.selenium;

/**
 * @author Vineet Manohar
 */
public class InvalidTestSuiteException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidTestSuiteException(String message, TestCaseNotFoundException e) {
        super(message, e);
    }
}
