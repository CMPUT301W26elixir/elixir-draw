package com.example.allot.view.shared;

import android.text.TextWatcher;

/**
 * Gives empty default methods for the TextWatcher callbacks you may not need.
 */
public abstract class SimpleTextWatcher implements TextWatcher {
    /**
     * Performs before text changed.
     *
     * @param s the s
     * @param start the start
     * @param count the count
     * @param after the after
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    /**
     * Handles the text changed callback.
     *
     * @param s the s
     * @param start the start
     * @param before the before
     * @param count the count
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}









