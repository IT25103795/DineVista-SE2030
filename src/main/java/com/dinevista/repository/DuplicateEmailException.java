package com.dinevista.repository;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {
        super("An account already uses that email address.");
    }
}
