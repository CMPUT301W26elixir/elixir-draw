package com.example.allot.common;
public class AppResult<T> {
    private final boolean success;
    private final T data;
    private final String message;
    private final Integer messageResId;

    /**
     * Creates a result object for an application operation.
     *
     * @param success true if the operation succeeded, otherwise false
     * @param data the returned data, or null if none is available
     * @param message an optional message describing the result
     */
    public AppResult(boolean success, T data, String message) {
        this(success, data, message, null);
    }

    /**
     * Creates a result object for an application operation using a string resource message.
     *
     * @param success true if the operation succeeded, otherwise false
     * @param data the returned data, or null if none is available
     * @param message the optional literal result message
     * @param messageResId the optional string resource ID describing the result
     */
    public AppResult(boolean success, T data, String message, Integer messageResId) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.messageResId = messageResId;
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
     * Creates a successful result with a string resource message.
     *
     * @param data the returned data
     * @param messageResId the result message resource
     * @param <T> the type of data returned
     * @return a successful result object
     */
    public static <T> AppResult<T> success(T data, int messageResId) {
        return new AppResult<>(true, data, null, messageResId);
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
     * Creates a failed result with a string resource message.
     *
     * @param messageResId the message resource describing the failure
     * @param <T> the type of data returned
     * @return a failed result object
     */
    public static <T> AppResult<T> failure(int messageResId) {
        return new AppResult<>(false, null, null, messageResId);
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

    /**
     * Returns the result message resource ID when one is available.
     *
     * @return the optional result message resource ID
     */
    public Integer getMessageResId() {
        return messageResId;
    }
}







