package com.example.allot.common;
/**
 * Holds the result of an app action plus any data and message info.
 *
 * @param <T> the type of data carried by the result
 */
public class AppResult<T> {
    private final boolean success;
    private final T data;
    private final String message;
    private final Integer messageResId;

    /**
     * Creates a new AppResult instance.
     *
     * @param success the success
     * @param data the data
     * @param message the message
     */
    public AppResult(boolean success, T data, String message) {
        this(success, data, message, null);
    }

    /**
     * Creates a new AppResult instance.
     *
     * @param success the success
     * @param data the data
     * @param message the message
     * @param messageResId the message res id
     */
    public AppResult(boolean success, T data, String message, Integer messageResId) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.messageResId = messageResId;
    }

    /**
     * Returns the result of success.
     *
     * @param <T> the t type parameter
     * @param data the data
     * @return the result of this call
     */
    public static <T> AppResult<T> success(T data) {
        return new AppResult<>(true, data, null);
    }

    /**
     * Returns the result of success.
     *
     * @param <T> the t type parameter
     * @param data the data
     * @param messageResId the message res id
     * @return the result of this call
     */
    public static <T> AppResult<T> success(T data, int messageResId) {
        return new AppResult<>(true, data, null, messageResId);
    }

    /**
     * Returns the result of failure.
     *
     * @param <T> the t type parameter
     * @param message the message
     * @return the result of this call
     */
    public static <T> AppResult<T> failure(String message) {
        return new AppResult<>(false, null, message);
    }

    /**
     * Returns the result of failure.
     *
     * @param <T> the t type parameter
     * @param messageResId the message res id
     * @return the result of this call
     */
    public static <T> AppResult<T> failure(int messageResId) {
        return new AppResult<>(false, null, null, messageResId);
    }

    /**
     * Returns whether success.
     *
     * @return whether success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the data.
     *
     * @return the data
     */
    public T getData() {
        return data;
    }

    /**
     * Returns the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the message res id.
     *
     * @return the message res id
     */
    public Integer getMessageResId() {
        return messageResId;
    }
}







