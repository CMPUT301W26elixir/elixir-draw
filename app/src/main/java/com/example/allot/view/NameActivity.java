package com.example.allot.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;

/**
 * Activity that collects the user's first and last name during the sign-up flow.
 *
 * <p>Once both fields are filled in, this screen passes the entered name values
 * to {@link EmailActivity} for the next step.
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
     * Initializes the activity, binds the input fields and button, and handles
     * validation before moving to the next screen.
     *
     * @param savedInstanceState the previously saved activity state, if one exists
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        EditText firstNameInput = findViewById(R.id.firstNameInput);
        EditText lastNameInput = findViewById(R.id.lastNameInput);
        Button nameNextButton = findViewById(R.id.nameNextButton);

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
            startActivity(intent);
        });
    }
}