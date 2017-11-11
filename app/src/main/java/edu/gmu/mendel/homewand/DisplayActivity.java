package edu.gmu.mendel.homewand;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;

public class DisplayActivity extends AppCompatActivity implements SensorEventListener {

    public static final String ACCEL_HEADER = "x,y,z\n";
    public static final String GYRO_HEADER = "x,y,z\n";

    private SensorManager sensorManager;
    private View view;
    private boolean color = false;
    private long lastUpdate;

    private String accelFilename;
    private String gyroFilename;
    private FileOutputStream accelOutputStream;
    private FileOutputStream gyroOutputStream;

    private int xx = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("3","start_activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(HomeWandActivity.SENSOR_VALS);

        view = findViewById(R.id.textView2);
        view.setBackgroundColor(Color.BLUE);

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView2);
        textView.setText(message);

        long time = System.currentTimeMillis();
        try {
            Log.i("4","save_file");
            Log.i("4",getFilesDir().toString());

            accelFilename = time + "-accel.csv";
            accelOutputStream = openFileOutput(accelFilename, Context.MODE_PRIVATE);
            accelOutputStream.write(ACCEL_HEADER.getBytes());

            gyroFilename = time + "-accel.csv";
            gyroOutputStream = openFileOutput(gyroFilename, Context.MODE_PRIVATE);
            gyroOutputStream.write(GYRO_HEADER.getBytes());
            //outputStream.close();
        } catch (Exception e) {
            Log.e("error","failed to save file", e);
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

            try {
                accelOutputStream.write(getValuesAsCsvRow(values).getBytes());
            } catch (Exception e) {
                Log.e("error","failed to save file", e);
            }
        }

    }

    private void getGyroscope(SensorEvent event) {
        float[] values = event.values;
        String row = getValuesAsCsvRow(values);

        try {
            if(xx < 10) {
                view.setBackgroundColor(Color.DKGRAY);
                gyroOutputStream.write(row.getBytes());
                xx++;
            }
            else {
                view.setBackgroundColor(Color.CYAN);
                gyroOutputStream.write(row.getBytes());
            }
        } catch (Exception e) {
            Log.e("error","failed to save file", e);
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
