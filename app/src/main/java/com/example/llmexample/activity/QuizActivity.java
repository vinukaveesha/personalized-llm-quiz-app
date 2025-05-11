package com.example.llmexample.activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.example.llmexample.helper.DatabaseHelper;

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
        String url = "http://192.168.1.147:5000/getQuiz?topic=" + topic;
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
        for (int i = 0; i < quizArray.length(); i++) {
            JSONObject q = quizArray.getJSONObject(i);
            Question question = new Question(
                    q.getString("question"),
                    q.getJSONArray("options"),
                    q.getString("correct_answer")
            );
            questions.add(question);
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

        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(currentQuestion.getOptions().optString(i));
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

        RadioButton selected = findViewById(selectedId);
        String selectedAnswer = selected.getText().toString();
        String correctAnswer = questions.get(currentQuestionIndex).getCorrectAnswer();

        if (selectedAnswer.startsWith(correctAnswer)) {
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
        private final String correctAnswer;

        public Question(String question, JSONArray options, String correctAnswer) {
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }

        public String getQuestion() { return question; }
        public JSONArray getOptions() { return options; }
        public String getCorrectAnswer() { return correctAnswer; }
    }
}
