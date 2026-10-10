package com.example.posapp.exception;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(Long id) {
        super("Item with id " + id + " was not found");
    }
}