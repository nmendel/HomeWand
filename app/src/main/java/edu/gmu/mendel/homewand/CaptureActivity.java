package edu.gmu.mendel.homewand;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CaptureActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    public static final int SAMPLING_128HZ = 7812;

    private SensorManager sensorManager;
    private View view;
    protected TextView mTextField;
    protected EditText editText;
    private boolean writing = false;

    private File accelFile;
    private File gyroFile;
    private BufferedWriter accelOutputStream;
    private BufferedWriter gyroOutputStream;

    private List<String> accelVals = new ArrayList<String>();
    private List<String> gyroVals = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("3","start_activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        view = findViewById(R.id.captureTextView);
        view.setBackgroundColor(Color.WHITE);

        // Capture the layout's TextView and set the string as its text
        mTextField = findViewById(R.id.captureTextView);
        mTextField.setText("Create capture files to profile a motion");

        editText = (EditText) findViewById(R.id.editText);
        editText.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        editText.getText().clear();
    }

    public void startCapture(View view) {
        Log.i("1","Pressed Start");
        if(writing) {
            return;
        }

        EditText editText = (EditText) findViewById(R.id.editText);
        String motion = editText.getText().toString();
        Log.i("2",motion);

        File dir = new File(getFilesDir(), motion);
        if(!dir.exists()) {
            Log.i("3","mkdir");
            dir.mkdir();
        }

        long time = System.currentTimeMillis();
        try {
            Log.i("4","save_file");

            Log.i("4",dir.toString());

            accelFile = new File(dir, time + "-accel.csv");
            accelOutputStream = new BufferedWriter(new FileWriter(accelFile));

            gyroFile = new File(dir, time + "-gyro.csv");
            gyroOutputStream = new BufferedWriter(new FileWriter(gyroFile));
            writing = true;
        } catch (Exception e) {
            Log.e("error","failed to save file", e);
        }

        // Start up a timer for the capture
        new CountDownTimer(3000, 100) {
            public void onTick(long millisUntilFinished) {
                mTextField.setText(String.format("%.1f",  (double)millisUntilFinished / 1000));
            }

            public void onFinish() {
                mTextField.setText("Stopped");
                if(writing) {
                    stopCapture();
                }
            }
        }.start();
    }

    /** Called when the user taps the Stop Capture button */
    public void stopCapture(View view) {
        stopCapture();
    }

    public void stopCapture() {
        Log.i("1","Pressed Stop");

        if(writing) {
            try {
                accelOutputStream.close();
                gyroOutputStream.close();
            } catch (IOException e) {
                Log.e("error", "failed to save file", e);
            }

            printVals();
            accelVals.clear();
            gyroVals.clear();
            writing = false;
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if(writing) {
            writeEvent(event);
        }
    }

    private void writeEvent(SensorEvent event) {
        BufferedWriter writer = null;
        String sensor = "";

        String row = getValuesAsCsvRow(event.values, event.timestamp);

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            writer = accelOutputStream;
            sensor = "accelerometer";
            accelVals.add(row);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            writer = gyroOutputStream;
            sensor = "gyro";
            gyroVals.add(row);
        }

        //Log.i(sensor, row);

        try {
            writer.write(row);
        } catch (IOException e) {
            Log.e("error", "failed to write to file", e);
        }
    }

    public String getValuesAsCsvRow(float[] values, long timestamp) {
        StringBuilder builder = new StringBuilder();
        builder.append(timestamp + ",");

        for (int i = 0; i < values.length; i++) {
            builder.append(values[i]);
            if(i < values.length - 1) {
                builder.append(",");
            }
        }
        builder.append("\n");

        return builder.toString();
    }

    public void printVals() {
        for(String line : accelVals) {
            Log.i("accel_vals", line);
        }
        for(String line : gyroVals) {
            Log.i("gyro_vals", line);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        // register this class as a listener for the orientation and
        // accelerometer sensors
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SAMPLING_128HZ);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SAMPLING_128HZ);
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
