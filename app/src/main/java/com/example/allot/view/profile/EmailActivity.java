package com.example.allot.view.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
/**
 * Collects the user's email address during profile setup.
 */
public class EmailActivity extends AppCompatActivity {

    /**
     * Initializes the activity, binds the input field and button,
     * validates the entered email, and opens the phone input screen
     * when the email is valid.
     *
     * @param savedInstanceState the saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        EditText emailInput = findViewById(R.id.emailInput);
        Button emailNextButton = findViewById(R.id.emailNextButton);

        emailNextButton.setOnClickListener(view -> {
            String email = emailInput.getText().toString().trim();

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(EmailActivity.this, PhoneActivity.class);
            intent.putExtra(NameActivity.EXTRA_FIRST_NAME, getIntent().getStringExtra(NameActivity.EXTRA_FIRST_NAME));
            intent.putExtra(NameActivity.EXTRA_LAST_NAME, getIntent().getStringExtra(NameActivity.EXTRA_LAST_NAME));
            intent.putExtra(NameActivity.EXTRA_EMAIL, email);
            startActivity(intent);
        });
    }
}








