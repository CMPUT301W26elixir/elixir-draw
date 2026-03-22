package com.example.allot.view.shared;

import android.text.TextWatcher;

/**
 * Gives empty default methods for the TextWatcher callbacks you may not need.
 */
public abstract class SimpleTextWatcher implements TextWatcher {
    /**
     * Handles work that should happen before text changes.
     *
     * @param s the original text
     * @param start the start index of the change
     * @param count the number of characters that will be replaced
     * @param after the number of characters that will be added
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    /**
     * Handles work that should happen while text is changing.
     *
     * @param s the text after the current change
     * @param start the start index of the change
     * @param before the number of characters that were replaced
     * @param count the number of characters that were added
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}









