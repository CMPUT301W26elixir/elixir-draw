package com.example.allot.common;

/**
 * Receives the result of an asynchronous operation and whether it succeeded.
 *
 * @param <T> the type of result returned to the caller
 */
public interface OnCompleteListener<T> {
    /**
     * Delivers the finished result back to the caller.
     *
     * @param result the value produced by the operation
     * @param success true when the operation finished successfully
     */
    void onComplete(T result, boolean success);
}







