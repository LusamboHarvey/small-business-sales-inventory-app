package com.team2.zedstockpos.view.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.team2.zedstockpos.R;

public class FeatureActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "feature_title";
    public static final String EXTRA_DESCRIPTION = "feature_description";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature);

        TextView title = findViewById(R.id.featureTitle);
        TextView description = findViewById(R.id.featureDescription);
        Button backButton = findViewById(R.id.btnFeatureBack);

        String featureTitle = getIntent().getStringExtra(EXTRA_TITLE);
        String featureDescription =
                getIntent().getStringExtra(EXTRA_DESCRIPTION);

        if (featureTitle != null) {
            title.setText(featureTitle);
        }

        if (featureDescription != null) {
            description.setText(featureDescription);
        }

        backButton.setOnClickListener(view -> finish());
    }
}