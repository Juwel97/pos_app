package com.example.posapp.exception;

public class DuplicateSerialNumberException extends RuntimeException {
    public DuplicateSerialNumberException() {
        super("An item with this serial number already exists");
    }
}