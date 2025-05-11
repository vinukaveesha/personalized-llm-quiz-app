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

import com.example.llmexample.R;
import com.example.llmexample.helper.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class TopicSelectionActivity extends AppCompatActivity {
    private static final String[] ALL_TOPICS = {"Data Structures", "Web Development", "Testing", "AI"};
    private DatabaseHelper dbHelper;
    private ArrayList<String> selectedTopics = new ArrayList<>();
    private TopicAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_selection);

        dbHelper = new DatabaseHelper(this);
        GridView gridView = findViewById(R.id.gridTopics);

        adapter = new TopicAdapter();
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            String topic = ALL_TOPICS[position];
            toggleTopicSelection(topic);
            adapter.notifyDataSetChanged(); // Update all views
        });

        findViewById(R.id.btnContinue).setOnClickListener(v -> saveTopicsAndContinue());
    }

    private void toggleTopicSelection(String topic) {
        if (selectedTopics.contains(topic)) {
            selectedTopics.remove(topic);
        } else {
            if (selectedTopics.size() < 10) {
                selectedTopics.add(topic);
            }
        }
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

            // Update both selection state and visual appearance
            boolean isSelected = selectedTopics.contains(topic);
            button.setSelected(isSelected);
            button.setActivated(isSelected); // Add this line

            return button;
        }
    }

    private void saveTopicsAndContinue() {
        if (selectedTopics.isEmpty()) {
            Toast.makeText(this, "Please select at least 1 topic", Toast.LENGTH_SHORT).show();
            return;
        }

        // Database operations
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String username = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("username", "");

        try (Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_ID},
                DatabaseHelper.COLUMN_USERNAME + " = ?",
                new String[]{username}, null, null, null)) {

            if (cursor.moveToFirst()) {
                int userId = cursor.getInt(0);
                Log.d("TOPIC_SAVE", "Saving " + selectedTopics.size() + " topics for user " + userId);

                // Clear existing topics
                db.delete(DatabaseHelper.TABLE_TOPICS,
                        DatabaseHelper.COLUMN_USER_ID + " = ?",
                        new String[]{String.valueOf(userId)});

                // Insert new topics
                for (String topic : selectedTopics) {
                    values.clear();
                    values.put(DatabaseHelper.COLUMN_USER_ID, userId);
                    values.put(DatabaseHelper.COLUMN_TOPIC, topic);
                    db.insert(DatabaseHelper.TABLE_TOPICS, null, values);
                }

                System.out.println(selectedTopics);

                List<String> savedTopics = dbHelper.getUserTopics(userId);
                Log.d("TOPIC_VERIFY", "Saved topics count: " + savedTopics.size());

                if(savedTopics.containsAll(selectedTopics)) {
                    startActivity(new Intent(this, DashboardActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Failed to save topics!", Toast.LENGTH_LONG).show();
                }

                startActivity(new Intent(this, DashboardActivity.class));
                finish();
            }
        }
    }
}