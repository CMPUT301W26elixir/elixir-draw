package com.example.allot.testutil;

import android.app.UiAutomation;
import android.os.ParcelFileDescriptor;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.IOException;

/**
 * Disables system animations for instrumentation tests that interact with Espresso.
 */
public final class SystemAnimations {

    private SystemAnimations() {
    }

    public static void disableAll() {
        setAnimationScale("window_animation_scale", 0f);
        setAnimationScale("transition_animation_scale", 0f);
        setAnimationScale("animator_duration_scale", 0f);
    }

    private static void setAnimationScale(String setting, float value) {
        UiAutomation automation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        ParcelFileDescriptor descriptor = automation.executeShellCommand(
                "settings put global " + setting + " " + value
        );
        if (descriptor == null) {
            return;
        }
        try {
            descriptor.close();
        } catch (IOException ignored) {
        }
    }
}
