package com.dinevista.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OperationResult<T> {
    private final T value;
    private final List<String> errors;

    private OperationResult(T value, List<String> errors) {
        this.value = value;
        this.errors = new ArrayList<>(errors);
    }

    public static <T> OperationResult<T> success(T value) {
        return new OperationResult<>(value, Collections.emptyList());
    }

    public static <T> OperationResult<T> failure(List<String> errors) {
        return new OperationResult<>(null, errors);
    }

    public static <T> OperationResult<T> failure(String error) {
        return new OperationResult<>(null, Collections.singletonList(error));
    }

    public boolean isSuccess() { return errors.isEmpty(); }
    public T getValue() { return value; }
    public List<String> getErrors() { return Collections.unmodifiableList(errors); }
}
