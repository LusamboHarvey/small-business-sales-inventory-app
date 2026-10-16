package com.team2.zedstockpos.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.team2.zedstockpos.R;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailLayout = findViewById(R.id.layoutLoginEmail);
        passwordLayout = findViewById(R.id.layoutLoginPassword);
        emailInput = findViewById(R.id.editLoginEmail);
        passwordInput = findViewById(R.id.editLoginPassword);

        MaterialButton loginButton = findViewById(R.id.btnLogin);
        TextView createAccount = findViewById(R.id.txtCreateAccount);
        TextView forgotPassword = findViewById(R.id.txtForgotPassword);

        String registeredEmail =
                getIntent().getStringExtra("registered_email");

        if (registeredEmail != null) {
            emailInput.setText(registeredEmail);
        }

        loginButton.setOnClickListener(view -> validateLogin());

        createAccount.setOnClickListener(view -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    RegisterActivity.class
            );

            startActivity(intent);
        });

        forgotPassword.setOnClickListener(view ->
                Toast.makeText(
                        LoginActivity.this,
                        "Password recovery will be available after API integration.",
                        Toast.LENGTH_SHORT
                ).show()
        );
    }

    private void validateLogin() {
        clearErrors();

        String email = getText(emailInput);
        String password = getText(passwordInput);

        boolean valid = true;

        if (email.isEmpty()) {
            emailLayout.setError("Email address is required");
            valid = false;

        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address");
            valid = false;
        }

        if (password.isEmpty()) {
            passwordLayout.setError("Password is required");
            valid = false;

        } else if (password.length() < 6) {
            passwordLayout.setError(
                    "Password must have at least 6 characters"
            );
            valid = false;
        }

        if (!valid) {
            return;
        }

        if (email.equalsIgnoreCase("admin@zedstock.com")
                && password.equals("admin123")) {

            Intent intent = new Intent(
                    LoginActivity.this,
                    AdminDashboardActivity.class
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();

        } else {
            passwordLayout.setError("Incorrect email or password");

            Toast.makeText(
                    LoginActivity.this,
                    "Use the administrator test account.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private String getText(TextInputEditText input) {
        return input.getText() == null
                ? ""
                : input.getText().toString().trim();
    }

    private void clearErrors() {
        emailLayout.setError(null);
        passwordLayout.setError(null);
    }
}