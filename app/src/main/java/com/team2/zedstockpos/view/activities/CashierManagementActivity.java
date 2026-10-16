
package com.team2.zedstockpos.view.activities;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.team2.zedstockpos.DatabaseHelper;
import com.team2.zedstockpos.R;

public class CashierManagementActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private TextView cashierList;
    private TextView cashierPlaceholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cashier_management);

        databaseHelper = new DatabaseHelper(this);

        cashierPlaceholder =
                findViewById(R.id.txtCashierListPlaceholder);

        MaterialButton addCashierButton =
                findViewById(R.id.btnAddCashier);

        cashierList = cashierPlaceholder;

        addCashierButton.setOnClickListener(
                view -> showCreateCashierDialog()
        );

        loadCashiers();
    }

    private void showCreateCashierDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);

        int padding = (int) (20 * getResources()
                .getDisplayMetrics().density);

        form.setPadding(padding, padding, padding, 0);

        EditText nameInput = new EditText(this);
        nameInput.setHint("Cashier full name");
        nameInput.setSingleLine(true);
        form.addView(nameInput);

        EditText emailInput = new EditText(this);
        emailInput.setHint("Email address");
        emailInput.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );
        emailInput.setSingleLine(true);
        form.addView(emailInput);

        EditText passwordInput = new EditText(this);
        passwordInput.setHint("Temporary password (minimum 6 characters)");
        passwordInput.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD
        );
        passwordInput.setSingleLine(true);
        form.addView(passwordInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Create Cashier Account")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Create", null)
                .create();

        dialog.setOnShowListener(ignored ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(view -> {
                            String name = nameInput.getText()
                                    .toString().trim();
                            String email = emailInput.getText()
                                    .toString().trim();
                            String password = passwordInput.getText()
                                    .toString();

                            if (name.isEmpty()
                                    || email.isEmpty()
                                    || password.length() < 6
                                    || !android.util.Patterns.EMAIL_ADDRESS
                                    .matcher(email).matches()) {
                                Toast.makeText(
                                        this,
                                        "Enter a name, valid email, and password of at least 6 characters.",
                                        Toast.LENGTH_LONG
                                ).show();
                                return;
                            }

                            boolean created = databaseHelper.createCashier(
                                    name,
                                    email,
                                    password
                            );

                            if (created) {
                                Toast.makeText(
                                        this,
                                        "Cashier account created.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                loadCashiers();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(
                                        this,
                                        "Account could not be created. The email may already exist.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        })
        );

        dialog.show();
    }


    private void loadCashiers() {
        java.util.List<String[]> cashiers =
                databaseHelper.getAllCashiers();

        if (cashiers.isEmpty()) {
            cashierPlaceholder.setText(
                    "No cashier accounts yet.\n"
                            + "Tap Create Cashier Account to add one."
            );
            return;
        }

        StringBuilder text = new StringBuilder();

        text.append("Total cashiers: ")
                .append(cashiers.size())
                .append("\n\n");

        for (String[] cashier : cashiers) {
            text.append(cashier[0])
                    .append("\n")
                    .append(cashier[1])
                    .append("\n\n");
        }

        cashierPlaceholder.setText(text.toString().trim());
    }

}
