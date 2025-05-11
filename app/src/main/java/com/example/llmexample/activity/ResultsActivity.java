package com.example.llmexample.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.llmexample.R;

public class ResultsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        TextView tvScore = findViewById(R.id.tvScore);
        Button btnContinue = findViewById(R.id.btnContinue);
        Button btnNewQuiz = findViewById(R.id.btnNewQuiz);

        int score = getIntent().getIntExtra("SCORE", 0);
        int total = getIntent().getIntExtra("TOTAL", 0);

        tvScore.setText(String.format("Score: %d/%d", score, total));

        btnContinue.setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });

        btnNewQuiz.setOnClickListener(v -> {
            startActivity(new Intent(this, TopicSelectionActivity.class));
            finish();
        });
    }
}