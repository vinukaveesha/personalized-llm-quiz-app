package com.example.llmexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.llmexample.activity.DashboardActivity;
import com.example.llmexample.activity.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String username = prefs.getString("username", null);

        Intent intent;
        if (username != null && !username.isEmpty()) {
            // User is logged in, go directly to dashboard
            intent = new Intent(this, DashboardActivity.class);
        } else {
            // No logged in user, show login screen
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish(); // Close MainActivity to prevent returning to it
    }
}