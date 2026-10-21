package com.team2.zedstockpos.view.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.team2.zedstockpos.R;


public class FeatureActivity extends AppCompatActivity {

    // Keys used to pass data into this activity via Intent
    public static final String EXTRA_TITLE = "feature_title";
    public static final String EXTRA_DESCRIPTION = "feature_description";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the layout for this screen
        setContentView(R.layout.activity_feature);

        // Find views in the layout
        TextView title = findViewById(R.id.featureTitle);
        TextView description = findViewById(R.id.featureDescription);
        Button backButton = findViewById(R.id.btnFeatureBack);

        // Read title and description sent from the previous screen
        String featureTitle = getIntent().getStringExtra(EXTRA_TITLE);
        String featureDescription =
                getIntent().getStringExtra(EXTRA_DESCRIPTION);

        // Update the title if one was passed in
        if (featureTitle != null) {
            title.setText(featureTitle);
        }

        // Update the description if one was passed in
        if (featureDescription != null) {
            description.setText(featureDescription);
        }

        // Close this screen when the back button is clicked
        backButton.setOnClickListener(view -> finish());
    }
}
