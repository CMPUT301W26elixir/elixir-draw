package com.example.allot.common;

/**
 * Receives the result of an asynchronous operation and whether it succeeded.
 *
 * @param <T> the type of result returned to the caller
 */
public interface OnCompleteListener<T> {
    /**
     * Handles the complete callback.
     *
     * @param result the result
     * @param success the success
     */
    void onComplete(T result, boolean success);
}







