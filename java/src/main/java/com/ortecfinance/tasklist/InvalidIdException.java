package com.ortecfinance.tasklist;

/**
 * Exception thrown when an invalid id is passed to a methog
 */
public class InvalidIdException extends Exception {
    public InvalidIdException(String s) {
        super(s);
    }
}
