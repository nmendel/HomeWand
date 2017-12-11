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

/**
 * Capture the accelerometer and gyroscope data at 128 Hz
 * for 3 seconds upon the start button being pressed
 */
public class CaptureActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    // 128 Hz converted to ms
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

    /**
     * Initialization
     */
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

    /**
     * Clear the text when it is clicked
     */
    @Override
    public void onClick(View v) {
        editText.getText().clear();
    }

    /**
     * Start writing sensor data to file
     */
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
        // Show it on the screen
        new CountDownTimer(3000, 100) {
            public void onTick(long millisUntilFinished) {
                mTextField.setText(String.format("%.1f",  (double)millisUntilFinished / 1000));
            }

            // Stop capturing sensor data when it ends
            public void onFinish() {
                mTextField.setText("Stopped");
                if(writing) {
                    stopCapture();
                }
            }
        }.start();
    }

    /**
     * Stop Capture button
     * */
    public void stopCapture(View view) {
        stopCapture();
    }

    /**
     * Finish writing the sensor data files, set writing to false
     */
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

    /**
     * This is how sensor data comes in in android
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(writing) {
            writeEvent(event);
        }
    }

    /**
     * Write the sensor event to the right file
     * @param event
     */
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

    /**
     * Get sensor values as a String csv row
     */
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

    /**
     * Print the values to log
     */
    public void printVals() {
        for(String line : accelVals) {
            Log.i("accel_vals", line);
        }
        for(String line : gyroVals) {
            Log.i("gyro_vals", line);
        }
    }


    /**
     * required android method
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SAMPLING_128HZ);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SAMPLING_128HZ);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
