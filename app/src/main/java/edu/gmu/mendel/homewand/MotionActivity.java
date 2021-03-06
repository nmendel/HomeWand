package edu.gmu.mendel.homewand;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Capture the user's motion
 * classify it
 * play the associated audio file
 */
public class MotionActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    protected TextView mTextField;

    private boolean writing = false;
    private long timestamp;
    private List<List<Float>> accelVals;
    private List<List<Float>> gyroVals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("3","start_activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motion);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mTextField = findViewById(R.id.motionView);
    }

    /**
     * Capture motion for 3 seconds
     */
    public void startMotion(View view) {
        Log.i("1","Pressed Start");
        if(writing) {
            return;
        }

        if(accelVals != null) {
            accelVals.clear();
        }
        if(gyroVals != null) {
            gyroVals.clear();
        }
        accelVals = new ArrayList<List<Float>>();
        gyroVals = new ArrayList<List<Float>>();

        timestamp = System.currentTimeMillis();
        writing = true;

        // Start up a timer for the capture
        new CountDownTimer(3000, 100) {
            public void onTick(long millisUntilFinished) {
                mTextField.setText(String.format("%.1f",  (double)millisUntilFinished / 1000));
            }

            public void onFinish() {
                mTextField.setText("Stopped");
                if(writing) {
                    stopMotion();
                }
            }
        }.start();
    }

    public void stopMotion(View view) {
        stopMotion();
    }

    /**
     * classify the motion and play the correct audio file
     */
    public void stopMotion() {
        if(writing) {
            writing = false;
            String command = classifyMotion();
            issueCommand(command);
        }
    }

    /**
     * Play the audio file with the passed in name
     * No real error checking, it is assumed that the file exists
     */
    public void issueCommand(String motion) {
        int soundId = this.getResources().getIdentifier(motion, "raw", this.getPackageName());
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), soundId);
        try {
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a Motion object with the collected sensor data
     * Use the decision tree to classify it
     * Return a String of the motion name
     */
    public String classifyMotion() {
        // what motion was it??
        Motion motion = new Motion("unknown", timestamp + "");
        motion.addData(Motion.ACCEL_TYPE, accelVals);
        motion.addData(Motion.GYRO_TYPE, gyroVals);

        Log.i("motion_object", motion.toString());

        String result = DecisionTree.classify(motion);
        Log.i("result", result);

        return result;
    }

    /**
     * Capture sensor data, copied from CaptureActivity
     * Doesn't write to a file though
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(writing) {
            List<Float> vals = new ArrayList<Float>();
            vals.add(new Float(event.timestamp));
            vals.add(event.values[0]);
            vals.add(event.values[1]);
            vals.add(event.values[2]);

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelVals.add(vals);
            }

            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                gyroVals.add(vals);
            }
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
                CaptureActivity.SAMPLING_128HZ);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                CaptureActivity.SAMPLING_128HZ);
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
