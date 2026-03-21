package com.example.allot.common;

/**
 * Represents the result of an application operation, including whether it
 * succeeded, the returned data, and an optional message.
 *
 * @param <T> the type of data returned by the operation
 */
public class AppResult<T> {
    private final boolean success;
    private final T data;
    private final String message;

    /**
     * Creates a result object for an application operation.
     *
     * @param success true if the operation succeeded, otherwise false
     * @param data the returned data, or null if none is available
     * @param message an optional message describing the result
     */
    public AppResult(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    /**
     * Creates a successful result.
     *
     * @param data the returned data
     * @param <T> the type of data returned
     * @return a successful result object
     */
    public static <T> AppResult<T> success(T data) {
        return new AppResult<>(true, data, null);
    }

    /**
     * Creates a failed result.
     *
     * @param message the message describing the failure
     * @param <T> the type of data returned
     * @return a failed result object
     */
    public static <T> AppResult<T> failure(String message) {
        return new AppResult<>(false, null, message);
    }

    /**
     * Returns whether the operation succeeded.
     *
     * @return true if the operation succeeded, otherwise false
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the result data.
     *
     * @return the returned data, or null if none is available
     */
    public T getData() {
        return data;
    }

    /**
     * Returns the result message.
     *
     * @return the optional result message
     */
    public String getMessage() {
        return message;
    }
}
