package com.team2.zedstockpos.view.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.team2.zedstockpos.R;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        findViewById(R.id.splashRoot).postDelayed(() -> {
            Intent intent = new Intent(
                    SplashActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}