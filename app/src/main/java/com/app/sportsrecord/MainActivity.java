package com.app.sportsrecord;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app.sportsrecord.databinding.ActivityMainBinding;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends Activity {

    private Button recordShootingButton;
    private Button recordNonShootingButton;
    private TextView timerText;
    private GifImageView imageView;
    private ActivityMainBinding binding;
    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
    private List<String> accList = new ArrayList<>();
    private List<String> gyroList = new ArrayList<>();
    private SoundPlayer soundPlayer = new SoundPlayer(this);


    final SensorEventListener accSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            if ((y > 2 || y < -2) && !soundPlayer.isPlaying()) {
                enterBasketSoundState();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        initUI();

        recordShootingButton.setOnClickListener(view -> {
            enterStartCountDownState();
            getRecordingTaskChain(true).startChainTasks();
        });
        recordNonShootingButton.setOnClickListener(view -> {
            enterStartCountDownState();
            getRecordingTaskChain(false).startChainTasks();
        });
    }

    private void initUI() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        timerText = binding.timerText;
        timerText.setVisibility(View.GONE);
        imageView = binding.gifImageview;
        try {
            GifDrawable gifFromAssets = new GifDrawable(getAssets(), "giphy.gif");
            imageView.setImageDrawable(gifFromAssets);
        } catch (IOException e) {
            e.printStackTrace();
        }

        recordShootingButton = binding.recordShootingButton;
        recordNonShootingButton = binding.recordNonshootingButton;
    }

    private TimerTaskChain getRecordingTaskChain(boolean shootingBasket) {
        TimerTaskChain timerTaskChain = new TimerTaskChain();
        timerTaskChain.addTask(4000, new TimerTaskChain.Task() {
            @Override
            public void onTick(long l) {
                enterCountDownState(l);
            }

            @Override
            public void onFinished() {
                enterRecordingState();
                toneGen1.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);
                startRecordingAcc();
                startRecordingGyro();
            }
        }).addTask(2000, new TimerTaskChain.Task() {

            @Override
            public void onFinished() {
                stopReccordingAcc();
                stopReccordingGyro();
                new Handler(Looper.getMainLooper()).postDelayed(() -> writingDataIntoFile(shootingBasket), 3000);
            }
        });
        return timerTaskChain;
    }

    private void enterStartCountDownState() {
        timerText.setVisibility(View.VISIBLE);
        recordShootingButton.setVisibility(View.GONE);
        recordNonShootingButton.setVisibility(View.GONE);
    }

    private void enterCountDownState(long remainingTime) {
        timerText.setText("Starting recording in " + (remainingTime / 1000) + " !");
    }

    private void enterRecordingState() {
        timerText.setText("Recording !");
    }

    private void enterBasketSoundState() {
        soundPlayer.tryPlaySound();
        new Handler(Looper.getMainLooper()).post(() -> imageView.setVisibility(View.VISIBLE));
    }

    private void resetAllUI() {
        recordShootingButton.setText(R.string.start_recording_shooting_basket);
        recordNonShootingButton.setText(R.string.start_recording_non_shooting_basket);
        timerText.setVisibility(View.GONE);
        recordShootingButton.setVisibility(View.VISIBLE);
        recordNonShootingButton.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
        Toast.makeText(MainActivity.this, getString(R.string.finish_recording), Toast.LENGTH_LONG).show();
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

    private void writingDataIntoFile(boolean shootingBasket) {
        new Thread(() -> {

            Handler finishHandler = new Handler(Looper.getMainLooper());

            try {

                // write result file
                PersistentCounter counter = new PersistentCounter(MainActivity.this);
                int previousCount = counter.getCount();
                int currentCount = ++previousCount;
                CSVWriter resultFileWriter = new CSVWriter(new FileWriter(MainActivity.this.getFilesDir() + "/" + "result.csv", true));
                if (previousCount == 0) {
                    resultFileWriter.writeNext(new String[]{"1", shootingBasket ? "1" : "-1"});
                } else {
                    resultFileWriter.writeNext(new String[]{currentCount + "", shootingBasket ? "1" : "-1"});
                }

                resultFileWriter.close();


                // write gyro and acc data into one data file.
                CSVWriter writer = new CSVWriter(new FileWriter(MainActivity.this.getFilesDir() + "/" + "record_" + currentCount + ".csv", true));
                for (int i = 0; i < Math.min(accList.size(), gyroList.size()); i++) {
                    String accString = accList.get(0);
                    String gyroString = gyroList.get(0);
                    String[] record = (accString + "," + gyroString).split(",");
                    writer.writeNext(record);
                }
                writer.close();

                counter.increaseCounter();
            } catch (Exception e) {
                e.printStackTrace();
                finishHandler.post(() -> Toast.makeText(MainActivity.this, getString(R.string.finish_recording_failure), Toast.LENGTH_LONG).show());
            }
            clearData();
            finishHandler.post(() -> resetAllUI());
        }).start();
    }
}