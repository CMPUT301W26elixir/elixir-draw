package com.example.allot.common.core;

/**
 * A simple listener for async operations.
 *
 * @param <T> the type returned when the operation finishes
 */
public interface OnCompleteListener<T> {
    void onComplete(T result, boolean success);
}
