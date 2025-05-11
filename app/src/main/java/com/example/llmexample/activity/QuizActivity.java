package com.example.llmexample.activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.llmexample.R;
import com.example.llmexample.adapter.QuestionAdapter;
import com.example.llmexample.helper.DatabaseHelper;
import com.example.llmexample.model.Question;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {
    private List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;

    // UI Components
    private TextView tvQuestion, tvProgress;
    private RadioGroup radioGroup;
    private RadioButton[] optionButtons;
    private Button btnNext;
    private ProgressBar progressBar;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        dbHelper = new DatabaseHelper(this);
        initializeUIComponents();
        String topic = getIntent().getStringExtra("TOPIC");
        fetchQuizQuestions(topic);
    }

    private void initializeUIComponents() {
        tvQuestion = findViewById(R.id.tvQuestion);
        tvProgress = findViewById(R.id.tvProgress);
        radioGroup = findViewById(R.id.radioGroup);
        btnNext = findViewById(R.id.btnNext);
        progressBar = findViewById(R.id.progressBar);

        optionButtons = new RadioButton[4];
        optionButtons[0] = findViewById(R.id.optionA);
        optionButtons[1] = findViewById(R.id.optionB);
        optionButtons[2] = findViewById(R.id.optionC);
        optionButtons[3] = findViewById(R.id.optionD);

        btnNext.setOnClickListener(v -> handleNextButton());
    }

    private void fetchQuizQuestions(String topic) {

        System.out.println("Topics ----> "+topic);
        progressBar.setVisibility(View.VISIBLE);
        String url = "http://192.168.8.155:5000/getQuiz?topic=" + topic;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONArray quizArray = response.getJSONArray("quiz");
                        parseQuestions(quizArray);
                        showQuestion(currentQuestionIndex);
                    } catch (Exception e) {
                        showError("Error parsing questions");
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    showError("Failed to load questions");
                }
        );

        queue.add(jsonObjectRequest);
    }

    private void parseQuestions(JSONArray quizArray) throws Exception {
        questions.clear(); // Clear previous questions first



        for (int i = 0; i < quizArray.length(); i++) {
            JSONObject q = quizArray.getJSONObject(i);

            // Add null checks
            String questionText = q.optString("question", "").replace("`", "").trim();
            JSONArray originalOptions = q.optJSONArray("options");
            String correctAnswer = q.optString("correct_answer", "").trim().toUpperCase();

            if (questionText.isEmpty() || originalOptions == null || correctAnswer.isEmpty()) {
                throw new Exception("Invalid question format at index " + i);
            }

            JSONArray cleanedOptions = new JSONArray();
            for (int j = 0; j < originalOptions.length(); j++) {
                String cleanedOption = originalOptions.getString(j)
                        .replace("`", "")
                        .trim();
                cleanedOptions.put(cleanedOption);
            }

            if (correctAnswer.isEmpty() || correctAnswer.charAt(0) < 'A' || correctAnswer.charAt(0) > 'D') {
                throw new Exception("Invalid correct answer: " + correctAnswer);
            }

            int correctIndex = correctAnswer.charAt(0) - 'A';

            // Add to questions list
            questions.add(new Question(
                    questionText,
                    cleanedOptions,
                    correctIndex
            ));
        }
    }

    private void showQuestion(int index) {
        if (index >= questions.size()) {
            showResults();
            return;
        }

        Question currentQuestion = questions.get(index);
        tvQuestion.setText(currentQuestion.getQuestion());
        tvProgress.setText((index + 1) + "/" + questions.size());

        JSONArray options = currentQuestion.getOptions();
        try {
            for (int i = 0; i < 4; i++) {
                if (i < options.length()) {
                    optionButtons[i].setVisibility(View.VISIBLE);
                    optionButtons[i].setText(options.getString(i));
                } else {
                    optionButtons[i].setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            Log.e("QuizActivity", "Error setting options", e);
        }

        radioGroup.clearCheck();
        updateButtonText();
    }

    private void handleNextButton() {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the position of the selected radio button
        int selectedIndex = radioGroup.indexOfChild(findViewById(selectedId));

        // Compare with stored correct index
        if (selectedIndex == questions.get(currentQuestionIndex).getCorrectIndex()) {
            score++;
        }

        currentQuestionIndex++;
        showQuestion(currentQuestionIndex);
    }

    private void updateButtonText() {
        btnNext.setText(currentQuestionIndex < questions.size() - 1 ? "Next" : "Finish");
    }

    private void showResults() {
        if (questions.isEmpty()) {
            Toast.makeText(this, "No questions attempted!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Save result to database
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String username = prefs.getString("username", "");
        int userId = dbHelper.getUserId(username);
        dbHelper.addQuizResult(userId, score, questions.size());

        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra("SCORE", score);
        intent.putExtra("TOTAL", questions.size());
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    public static class Question {
        private final String question;
        private final JSONArray options;
        private final int correctIndex; // Changed from String to int

        public Question(String question, JSONArray options, int correctIndex) {
            this.question = question;
            this.options = options;
            this.correctIndex = correctIndex;
        }

        public String getQuestion() { return question; }
        public JSONArray getOptions() { return options; }
        public int getCorrectIndex() { return correctIndex; }
    }
}
