package edu.gmu.mendel.homewand;

import android.content.Context;
import android.content.Intent;
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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class CaptureActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    public static final String ACCEL_HEADER = "x,y,z\n";
    public static final String GYRO_HEADER = "x,y,z\n";

    private SensorManager sensorManager;
    private View view;
    protected TextView mTextField;
    protected EditText editText;
    private boolean color = false;
    private boolean writing = false;
    private long lastUpdate;

    private File accelFile;
    private File gyroFile;
    private BufferedWriter accelOutputStream;
    private BufferedWriter gyroOutputStream;

    private int xx = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("3","start_activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();

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
        String message = editText.getText().toString();
        Log.i("2",message);

        File dir = new File(getFilesDir(), message);
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
            accelOutputStream.write(ACCEL_HEADER);


            gyroFile = new File(dir, time + "-gyro.csv");
            gyroOutputStream = new BufferedWriter(new FileWriter(gyroFile));
            gyroOutputStream.write(GYRO_HEADER);
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
            writing = false;

            try {
                accelOutputStream.close();
                gyroOutputStream.close();
            } catch (IOException e) {
                Log.e("error", "failed to save file", e);
            }
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            getGyroscope(event);
        }

    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        float accelationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        long actualTime = event.timestamp;
        if (accelationSquareRoot >= 2 || true) //
        {
            if (actualTime - lastUpdate < 200) {
                return;
            }
            lastUpdate = actualTime;
            if (color) {
                view.setBackgroundColor(Color.GREEN);
            } else {
                view.setBackgroundColor(Color.RED);
            }
            color = !color;

            if(writing) {
                try {
                    accelOutputStream.write(getValuesAsCsvRow(values));
                } catch (Exception e) {
                    Log.e("error", "failed to save file", e);
                }
            }
        }

    }

    private void getGyroscope(SensorEvent event) {
        float[] values = event.values;
        String row = getValuesAsCsvRow(values);

        if(writing) {
            try {
                if (xx < 10) {
                    view.setBackgroundColor(Color.DKGRAY);
                    gyroOutputStream.write(row);
                    xx++;
                } else {
                    view.setBackgroundColor(Color.CYAN);
                    gyroOutputStream.write(row);
                }
            } catch (Exception e) {
                Log.e("error", "failed to save file", e);
            }
        }
    }

    public String getValuesAsCsvRow(float[] values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            builder.append(values[i]);
            if(i < values.length - 1) {
                builder.append(",");
            }
        }
        builder.append("\n");

        return builder.toString();
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
                SensorManager.SENSOR_DELAY_NORMAL);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
