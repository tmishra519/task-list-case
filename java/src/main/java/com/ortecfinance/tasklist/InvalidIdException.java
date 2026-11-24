package com.ortecfinance.tasklist;

/**
 * Exception thrown when an invalid id is passed to a method
 */
public class InvalidIdException extends Exception {
    public InvalidIdException(String s) {
        super(s);
    }
}
