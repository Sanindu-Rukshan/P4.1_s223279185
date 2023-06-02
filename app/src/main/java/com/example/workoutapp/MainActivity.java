package com.example.workoutapp;

import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;

import androidx.appcompat.app.AppCompatActivity;

public class 3600MainActivity extends AppCompatActivity {

    private TextView workoutTextView;
    private EditText workoutEditText, restEditText;
    private Button startButton, stopButton;
    private ProgressBar progressBar;
    private CountDownTimer workoutTimer, restTimer;
    private Handler handler;
    private Runnable runnable;

    private int workoutDuration, restDuration;
    private boolean isWorkoutRunning, isRestRunning;
    private int currentWorkoutTime, currentRestTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup ids
        workoutTextView = findViewById(R.id.workoutTextView);
        workoutEditText = findViewById(R.id.workoutEditText);
        restEditText = findViewById(R.id.restEditText);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        progressBar = findViewById(R.id.progressBar);

        handler = new Handler();

        // set event listeners on buttons
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWorkout();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopWorkout();
            }
        });
    }

    // start workout timer
    private void startWorkout() {
        if (!isWorkoutRunning && !isRestRunning) {
            workoutDuration = Integer.parseInt(workoutEditText.getText().toString());
            restDuration = Integer.parseInt(restEditText.getText().toString());

            if (workoutDuration <= 0 || restDuration <= 0) {
                Toast.makeText(this, "Invalid duration", Toast.LENGTH_SHORT).show();
                return;
            }

            isWorkoutRunning = true;
            // this makes the number start from the same no.
            currentWorkoutTime = workoutDuration + 1;
            updateUI();

            workoutTimer = new CountDownTimer((workoutDuration + 1) * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    currentWorkoutTime--;
                    updateUI();
                }

                @Override
                public void onFinish() {
                    isWorkoutRunning = false;
                    startRest();

                    // notification + sound
                    showNotification("Workout Complete", "Get ready for rest!", getApplicationContext());


                    // Vibrate
                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            vibrator.vibrate(1000);
                        }
                    }
                }
            };

            // start timer
            workoutTimer.start();

            // set button states
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        }
    }

    private void startRest() {
        isRestRunning = true;
        currentRestTime = restDuration + 1;
        updateUI();

        restTimer = new CountDownTimer((restDuration + 1) * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                currentRestTime--;
                updateUI();
            }

            @Override
            public void onFinish() {
                isRestRunning = false;
                startWorkout();

                // notification + sound
                showNotification("Rest Complete", "Get ready for the next set!", getApplicationContext());


                // Vibrate device
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                if (vibrator != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(1000);
                    }
                }
            }
        };

        restTimer.start();
    }

    private void stopWorkout() {
        if (isWorkoutRunning || isRestRunning) {
            if (workoutTimer != null) {
                workoutTimer.cancel();
            }
            if (restTimer != null) {
                restTimer.cancel();
            }

            isWorkoutRunning = false;
            isRestRunning = false;
            currentWorkoutTime = 0;
            currentRestTime = 0;
            updateUI();

            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }

    // update UI func
    private void updateUI() {
        String workoutText = "Workout: " + formatTime(currentWorkoutTime);
        String restText = "Rest: " + formatTime(currentRestTime);

        // set text based on what is currently gouing on
        workoutTextView.setText(isWorkoutRunning ? workoutText : restText);

        int progress;
        int totalDuration;
        if (isWorkoutRunning) {
            progress = (workoutDuration - currentWorkoutTime) * 100 / workoutDuration;
            totalDuration = workoutDuration;
        } else {
            progress = (restDuration - currentRestTime) * 100 / restDuration;
            totalDuration = restDuration;
        }

        // set progress bar
        progressBar.setProgress(progress);
        progressBar.setMax(100);

        if (currentWorkoutTime == 0 && currentRestTime == 0) {
            workoutTextView.setText("Workout Complete");
            progressBar.setProgress(100);
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    private void showNotification(String title, String message, Context context) {
        // notification channel create
        String channelId = "timer_channel";
        String channelName = "Timer Channel";

        // set notif sound
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri);

        // access android notif service
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // create notif channel for newer devices
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // show notif
        if (notificationManager != null) {
            notificationManager.notify(0, builder.build());
        }
    }

}




