package com.example.allot.view.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import com.example.allot.view.shared.DeferredOnboardingNavigator;
/**
 * Collects the user's first and last name during profile setup.
 */
public class NameActivity extends AppCompatActivity {

    /**
     * Intent extra key used to store the user's first name.
     */
    public static final String EXTRA_FIRST_NAME = "com.example.allot.EXTRA_FIRST_NAME";

    /**
     * Intent extra key used to store the user's last name.
     */
    public static final String EXTRA_LAST_NAME = "com.example.allot.EXTRA_LAST_NAME";

    /**
     * Intent extra key used to store the user's email.
     */
    public static final String EXTRA_EMAIL = "com.example.allot.EXTRA_EMAIL";

    /**
     * Intent extra key used to store the user's phone number.
     */
    public static final String EXTRA_PHONE = "com.example.allot.EXTRA_PHONE";

    /**
     * Handles the create callback.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        EditText firstNameInput = findViewById(R.id.firstNameInput);
        EditText lastNameInput = findViewById(R.id.lastNameInput);
        Button nameNextButton = findViewById(R.id.nameNextButton);
        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        nameNextButton.setOnClickListener(view -> {
            String firstName = firstNameInput.getText().toString().trim();
            String lastName = lastNameInput.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(this, "Enter your first and last name.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(NameActivity.this, EmailActivity.class);
            intent.putExtra(EXTRA_FIRST_NAME, firstName);
            intent.putExtra(EXTRA_LAST_NAME, lastName);
            DeferredOnboardingNavigator.copyDeferredExtras(getIntent(), intent);
            startActivity(intent);
        });
    }
}








