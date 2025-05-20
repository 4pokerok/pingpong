package com.example.pong;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Process;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

/** @noinspection ALL*/
public class GameActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new GameView(this));
    }

    class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

        private Thread gameThread = null;
        private boolean isPlaying = false;
        private final Paint paint;

        // Размеры экрана
        private int screenX;
        private int screenY;

        // Ракетки
        private final int racketWidth;
        private final int racketHeight;
        private float player1Y, player2Y;

        // Мяч
        private float ballX, ballY;
        private float ballSpeedX = 8, ballSpeedY = 8;

        // Касания
        private float touchPlayer1 = -1, touchPlayer2 = -1;

        // Счет
        private int scorePlayer1 = 0;
        private int scorePlayer2 = 0;
        private final int maxScore = 5;

        private final Context context;

        // Звук
        private SoundPool soundPool;
        private int bounceSoundId;

        public GameView(Context context) {
            super(context);
            this.context = context;
            getHolder().addCallback(this);
            paint = new Paint();
            racketWidth = 30;
            racketHeight = 200;

            // Инициализация SoundPool
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();

            // Загрузка звука
            bounceSoundId = soundPool.load(context, R.raw.bounce, 1); // загружаем звук
        }

        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            screenX = getWidth();
            screenY = getHeight();

            player1Y = (float) screenY / 2 - (float) racketHeight / 2;
            player2Y = (float) screenY / 2 - (float) racketHeight / 2;

            ballX = (float) screenX / 2;
            ballY = (float) screenY / 2;

            isPlaying = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public void run() {
            while (isPlaying) {
                update();
                draw();
                sleep();
            }
        }

        public void update() {
            ballX += ballSpeedX;
            ballY += ballSpeedY;

            if (ballY < 0 || ballY > screenY) {
                ballSpeedY *= -1;
            }

            if (ballX < racketWidth && ballY > player1Y && ballY < player1Y + racketHeight) {
                ballSpeedX *= -1;
                soundPool.play(bounceSoundId, 1, 1, 0, 0, 1); // Проигрываем звук
            }
            if (ballX > screenX - racketWidth && ballY > player2Y && ballY < player2Y + racketHeight) {
                ballSpeedX *= -1;
                soundPool.play(bounceSoundId, 1, 1, 0, 0, 1); // Проигрываем звук
            }

            if (ballX < 0) {
                scorePlayer2++;
                resetBall();
            } else if (ballX > screenX) {
                scorePlayer1++;
                resetBall();
            }

            if (scorePlayer1 >= maxScore || scorePlayer2 >= maxScore) {
                saveHighScore();
                ((Activity) context).runOnUiThread(() -> {
                    Intent intent = new Intent(context, GameOverActivity.class);
                    intent.putExtra("winner", scorePlayer1 >= maxScore ? "Игрок 1" : "Игрок 2");
                    intent.putExtra("score1", scorePlayer1);
                    intent.putExtra("score2", scorePlayer2);
                    context.startActivity(intent);
                });
                isPlaying = false;
            }

            if (touchPlayer1 != -1) {
                player1Y = touchPlayer1 - (float) racketHeight / 2;
            }
            if (touchPlayer2 != -1) {
                player2Y = touchPlayer2 - (float) racketHeight / 2;
            }
        }

        private void resetBall() {
            ballX = (float) screenX / 2;
            ballY = (float) screenY / 2;
            ballSpeedX = -ballSpeedX;
            ballSpeedY = (float) (Math.random() * 6 - 3);
        }

        private void saveHighScore() {
            SharedPreferences sharedPref = context.getSharedPreferences("game_data", Context.MODE_PRIVATE);
            int currentHigh = sharedPref.getInt("high_score", 0);

            int winnerScore = Math.max(scorePlayer1, scorePlayer2);
            if (winnerScore > currentHigh) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("high_score", winnerScore);
                editor.apply();
            }
        }

        public void draw() {
            SurfaceHolder holder = getHolder();
            if (holder.getSurface().isValid()) {
                Canvas canvas = holder.lockCanvas();
                canvas.drawColor(Color.BLACK);

                paint.setColor(Color.BLUE);
                canvas.drawRect(0, player1Y, racketWidth, player1Y + racketHeight, paint);

                paint.setColor(Color.RED);
                canvas.drawRect(screenX - racketWidth, player2Y, screenX, player2Y + racketHeight, paint);

                paint.setColor(Color.WHITE);
                int ballRadius = 20;
                canvas.drawCircle(ballX, ballY, ballRadius, paint);

                paint.setTextSize(48);
                paint.setColor(Color.GREEN);
                canvas.drawText("" + scorePlayer1, (float) screenX / 2 - 100, 80, paint);
                canvas.drawText("" + scorePlayer2, (float) screenX / 2 + 70, 80, paint);

                holder.unlockCanvasAndPost(canvas);
            }
        }

        public void sleep() {
            try {
                Thread.sleep(17); // ~60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    float x = event.getX();
                    float y = event.getY();

                    if (x < screenX / 2) {
                        touchPlayer1 = y;
                    } else {
                        touchPlayer2 = y;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    touchPlayer1 = -1;
                    touchPlayer2 = -1;
                    break;
            }
            return true;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            isPlaying = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (soundPool != null) {
                soundPool.release();
                soundPool = null;
            }
        }

        public void onBackPressed() {
            moveTaskToBack(true);
            Process.killProcess(Process.myPid());
            System.exit(1);
        }
    }
}