package com.example.uaipapo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    Button loginPhoneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginPhoneButton = findViewById(R.id.login_phone_button);

        loginPhoneButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, LoginPhoneNumberActivity.class));
        });
    }
}