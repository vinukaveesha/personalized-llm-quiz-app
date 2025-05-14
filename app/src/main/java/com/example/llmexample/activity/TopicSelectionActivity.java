package com.example.llmexample.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.llmexample.MainActivity;
import com.example.llmexample.R;
import com.example.llmexample.helper.DatabaseHelper;
import android.view.HapticFeedbackConstants;

import java.util.ArrayList;
import java.util.List;

public class TopicSelectionActivity extends AppCompatActivity {
    private static final String[] ALL_TOPICS = {"Data Structures", "Web Development", "Testing", "AI", "Programming", "Neural Networks", "Algorithms", "Computer vision"};
    private DatabaseHelper dbHelper;
    private ArrayList<String> selectedTopics = new ArrayList<>();
    private TopicAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_selection);

        dbHelper = new DatabaseHelper(this);
        GridView gridView = findViewById(R.id.gridTopics);

        // Load existing selections
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String username = prefs.getString("username", "");
        int userId = dbHelper.getUserId(username);
        selectedTopics = new ArrayList<>(dbHelper.getUserTopics(userId)); // Load existing topics

        adapter = new TopicAdapter();
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            String topic = ALL_TOPICS[position];
            toggleTopicSelection(topic);
            adapter.notifyDataSetChanged();
        });

        findViewById(R.id.btnContinue).setOnClickListener(v -> saveTopicsAndContinue());
    }

    private void toggleTopicSelection(String topic) {
        if (selectedTopics.contains(topic)) {
            selectedTopics.remove(topic);
        } else {
            if (selectedTopics.size() < 10) {
                selectedTopics.add(topic);
                // Add haptic feedback with proper context
                findViewById(R.id.gridTopics).performHapticFeedback(
                        HapticFeedbackConstants.VIRTUAL_KEY
                );
            } else {
                Toast.makeText(this, "Maximum 10 topics allowed", Toast.LENGTH_SHORT).show();
            }
        }
        adapter.notifyDataSetChanged();
    }


    private class TopicAdapter extends BaseAdapter {
        @Override
        public int getCount() { return ALL_TOPICS.length; }

        @Override
        public Object getItem(int position) { return ALL_TOPICS[position]; }

        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Button button = (Button) (convertView != null ? convertView :
                    getLayoutInflater().inflate(R.layout.grid_item_topic, parent, false));

            String topic = ALL_TOPICS[position];
            button.setText(topic);

            // Update selection state with enhanced visual feedback
            boolean isSelected = selectedTopics.contains(topic);
            button.setSelected(isSelected);

            // Add visual indicators
            button.setCompoundDrawablesWithIntrinsicBounds(
                    0, isSelected ? R.drawable.ic_check : 0, 0, 0
            );
            button.setCompoundDrawablePadding(8);

            return button;
        }
    }

    private void saveTopicsAndContinue() {
        if (selectedTopics.isEmpty()) {
            Toast.makeText(this, "Please select at least 1 topic", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String username = prefs.getString("username", "");

        if (username.isEmpty()) {
            Toast.makeText(this, "User session expired!", Toast.LENGTH_SHORT).show();
            restartApp();
            return;
        }

        int userId = dbHelper.getUserId(username);
        if (userId == -1) {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
            restartApp();
            return;
        }

        new Thread(() -> {
            boolean success = dbHelper.addUserTopics(userId, selectedTopics);
            runOnUiThread(() -> {
                if (success) {
                    startActivity(new Intent(this, DashboardActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Failed to save topics!", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private void restartApp() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        startActivity(new Intent(this, MainActivity.class));
        finishAffinity();
    }

}