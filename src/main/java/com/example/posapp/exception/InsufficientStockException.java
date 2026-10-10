package com.example.posapp.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String itemName, int available) {
        super(itemName + " has only " + available + " unit(s) available");
    }
}