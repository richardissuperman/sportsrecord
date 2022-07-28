package com.app.sportsrecord;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app.sportsrecord.databinding.ActivityMainBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends Activity {

    private Button recordButton;
    private TextView timerText;
    private GifImageView imageView;
    private ActivityMainBinding binding;
    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
    private List<String> accList = new ArrayList<>();
    private List<String> gyroList = new ArrayList<>();
    MediaPlayer player = new MediaPlayer();


    final SensorEventListener accSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            if (y > 5) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setVisibility(View.VISIBLE);
                        playNetSwishSound();
                    }
                });
            }
            String accString = x + "," + y + "," + z;
            accList.add(accString);
            Log.e("richard", "current acc values x: " + x + " y: " + y + " z: " + z);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    final SensorEventListener gyroSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            String gyroString = x + "," + y + "," + z;
            gyroList.add(gyroString);
            Log.e("richard", "current gyro values x: " + x + " y: " + y + " z: " + z);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    CountDownTimer startTimer = new CountDownTimer(4000, 1) {
        @Override
        public void onTick(long l) {
            timerText.setText("Starting recording in " + (l / 1000) + " !");
        }

        @Override
        public void onFinish() {
            timerText.setText("Recording !");
            toneGen1.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);
            startRecordingAcc();
            startRecordingGyro();
            recordTimer.start();
        }
    };

    CountDownTimer recordTimer = new CountDownTimer(1500, 1) {
        @Override
        public void onTick(long l) {

        }

        @Override
        public void onFinish() {

            stopReccordingAcc();
            stopReccordingGyro();
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    writingDataIntoFile();
                }
            }, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        timerText = binding.timerText;
        timerText.setVisibility(View.GONE);
        imageView = binding.gifImageview;
        try {
            GifDrawable gifFromAssets = new GifDrawable( getAssets(), "giphy.gif" );
            imageView.setImageDrawable(gifFromAssets);
        } catch (IOException e) {
            e.printStackTrace();
        }

        recordButton = binding.recordButton;
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimer.start();
                timerText.setVisibility(View.VISIBLE);
                recordButton.setVisibility(View.GONE);
            }
        });
    }

    private void startRecordingAcc() {

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(accSensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void stopReccordingAcc() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(accSensorEventListener);
    }


    private void startRecordingGyro() {

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(gyroSensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void stopReccordingGyro() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(gyroSensorEventListener);
    }

    private void clearData() {
        gyroList.clear();
        accList.clear();
    }

    private void writingDataIntoFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Handler finishHandler = new Handler(Looper.getMainLooper());

                String fileNameAcc = MainActivity.this.getFilesDir()+"/" + System.currentTimeMillis() + "_" + "acc.txt";
                String fileNameGyro = MainActivity.this.getFilesDir()+"/" +System.currentTimeMillis() + "_" + "gyro.txt";


                try {
//                    FileWriter fileWriterAcc = new FileWriter(fileNameAcc, true);
//                    for (String s : accList) {
//                        fileWriterAcc.write(s + "\n");
//                    }
//                    fileWriterAcc.close();
//
//                    FileWriter fileWriterGyro = new FileWriter(fileNameGyro, true);
//                    for (String s : gyroList) {
//                        fileWriterGyro.write(s + "\n");
//                    }
//                    fileWriterGyro.close();
                    finishHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            recordButton.setText(R.string.start_recording);
                            timerText.setVisibility(View.GONE);
                            recordButton.setVisibility(View.VISIBLE);
                            imageView.setVisibility(View.GONE);
                            clearData();
                            Toast.makeText(MainActivity.this, getString(R.string.finish_recording), Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    Log.e("richard", e.toString());
                    finishHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, getString(R.string.finish_recording_failure), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    public void playNetSwishSound(){
        try {
            if (player != null) {
                player.release();
                player = null;
            }
            player = new MediaPlayer();
            AssetFileDescriptor afd = getAssets().openFd("haha.mp3");
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepare();
            player.start();
        }catch (Exception e) {
            Log.e("richard", e.toString());
        }
    }
}