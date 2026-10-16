
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
import com.team2.zedstockpos.DatabaseHelper;
import com.team2.zedstockpos.R;
import com.team2.zedstockpos.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Restore the saved session when the app is reopened.
        if (sessionManager.isLoggedIn()) {
            openSavedDashboard();
            return;
        }

        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);

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
                        "Password recovery is not yet available.",
                        Toast.LENGTH_SHORT
                ).show()
        );
    }

    private void validateLogin() {
        clearErrors();

        String email = getText(emailInput);
        String password = getPassword();

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

        // Existing administrator test account.
        if (email.equalsIgnoreCase("admin@zedstock.com")
                && password.equals("admin123")) {

            sessionManager.createLoginSession(
                    "admin@zedstock.com",
                    "ADMIN"
            );

            openDashboard(AdminDashboardActivity.class);
            return;
        }

        // Authenticate registered cashier accounts.
        boolean cashierAuthenticated =
                databaseHelper.authenticateUser(
                        email,
                        password,
                        "CASHIER"
                );

        if (cashierAuthenticated) {
            sessionManager.createLoginSession(email, "CASHIER");

            openDashboard(CashierSalesActivity.class);
        } else {
            passwordLayout.setError("Incorrect email or password");

            Toast.makeText(
                    LoginActivity.this,
                    "Incorrect email or password.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void openSavedDashboard() {
        String role = sessionManager.getUserRole();

        if ("ADMIN".equalsIgnoreCase(role)) {
            openDashboard(AdminDashboardActivity.class);

        } else if ("CASHIER".equalsIgnoreCase(role)) {
            openDashboard(CashierSalesActivity.class);

        } else {
            // Clear an invalid session and display the login screen.
            sessionManager.logout();
            setContentView(R.layout.activity_login);
            recreate();
        }
    }

    private void openDashboard(Class<?> destination) {
        Intent intent = new Intent(LoginActivity.this, destination);

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    private String getText(TextInputEditText input) {
        return input.getText() == null
                ? ""
                : input.getText().toString().trim();
    }

    private String getPassword() {
        return passwordInput.getText() == null
                ? ""
                : passwordInput.getText().toString();
    }

    private void clearErrors() {
        emailLayout.setError(null);
        passwordLayout.setError(null);
    }
}
