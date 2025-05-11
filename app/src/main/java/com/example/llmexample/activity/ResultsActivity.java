package com.example.llmexample.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.llmexample.R;

public class ResultsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        TextView tvScore = findViewById(R.id.tvScore);
        int score = getIntent().getIntExtra("SCORE", 0);
        int total = getIntent().getIntExtra("TOTAL", 0);

        tvScore.setText(String.format("Score: %d/%d", score, total));

        findViewById(R.id.btnContinue).setOnClickListener(v ->
                startActivity(new Intent(this, DashboardActivity.class)));
    }
}