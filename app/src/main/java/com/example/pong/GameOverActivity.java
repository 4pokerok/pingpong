package com.example.pong;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class GameOverActivity extends Activity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        TextView resultText = findViewById(R.id.text_result);
        TextView highScoreText = findViewById(R.id.text_high_score);
        Button restartButton = findViewById(R.id.btn_restart);
        Button exitButton = findViewById(R.id.btn_exit);

        String winner = getIntent().getStringExtra("winner");
        int score1 = getIntent().getIntExtra("score1", 0);
        int score2 = getIntent().getIntExtra("score2", 0);

        resultText.setText("Победитель: " + winner + "\nСчёт: " + score1 + " — " + score2);

        SharedPreferences sharedPref = getSharedPreferences("game_data", MODE_PRIVATE);
        int highScore = sharedPref.getInt("high_score", 0);
        highScoreText.setText("Рекорд: " + highScore);

        restartButton.setOnClickListener(v -> {
            finish(); // Закрываем GameOverActivity
            startActivity(new Intent(this, GameActivity.class)); // Перезапускаем игру
        });

        exitButton.setOnClickListener(v -> {
            finishAffinity(); // Закрывает все активити и выходит из приложения
        });
    }
}