package com.example.allot.view;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;

public class ManageEventActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "event_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_event);

        ImageButton backButton = findViewById(R.id.backButton);
        TextView placeholderText = findViewById(R.id.manageEventPlaceholderText);

        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
        placeholderText.setText(getString(R.string.manage_event_placeholder));
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
