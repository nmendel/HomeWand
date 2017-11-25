package edu.gmu.mendel.homewand;

/**
 * Created by 575724 on 11/24/2017.
 */

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Motion {

    public static final int NUM_SENSOR_DIMENSIONS = 3;

    public String motionTime;
    public List<float[]> values = new ArrayList<float[]>();

    // low-pass filtered (1 Hz)
    public float[] DCArea = new float[6];
    public float[] DCMean = new float[6];
    public float[] DCTotalMean = new float[2]; // single value per sensor
    public float[] DCPostureDist = new float[6];

    // band-pass filtered (0-0.7 Hz)
    public float[] ACLowEnergy;

    // band-pass filtered (0.1-20 Hz)
    public float[] ACAbsArea = new float[6];
    public float[] ACTotalAbsArea = new float[2]; // single value per sensor
    public float[] ACAbsMean = new float[6];
    public float[] ACEnergy;
    public float[] ACVar = new float[6];
    public float[] ACAbsCV;
    public float[] ACIQR;
    public float[] ACRange = new float[6];

    Motion(String motionTime) {
        this.motionTime = motionTime;
    }

    public void addData(String type, String fileData) {

        String[] lines = fileData.split("\n");
        for(int i = 0; i < lines.length; i++) {
            Log.i("line", lines[i]);
            String[] splitLine = lines[i].split(",");
            Long time = new Long(splitLine[0]);
            Float x = new Float(splitLine[1]);
            Float y = new Float(splitLine[2]);
            Float z = new Float(splitLine[3]);

            values.add(new float[] {x.floatValue(), y.floatValue(), z.floatValue()});
            //allX.add(x);
            //allY.add(y);
            //allZ.add(z);
        }

        int offset = 0;
        if(type.equals("gyro")) {
            Log.i("fileType", "gyro");
            offset = NUM_SENSOR_DIMENSIONS;
        }

        calculateLowPass(offset);
        calculateBandPass(offset);
    }


    // TODO:
    // low-pass filtered (1 Hz)
    public void calculateLowPass(int offset) {
        float sumX = 0;
        float sumY = 0;
        float sumZ = 0;

        for (float[] line : values) {
            sumX += line[0];
            sumY += line[1];
            sumZ += line[2];
        }

        // The area under the signal simply computed by summing the acceleration samples contained in a given window.
        DCArea[0 + offset] = sumX;
        DCArea[1 + offset] = sumY;
        DCArea[2 + offset] = sumZ;

        // Mean or average over the signal window
        DCMean[0 + offset] = sumX / values.size();
        DCMean[1 + offset] = sumY / values.size();
        DCMean[2 + offset] = sumZ / values.size();

        // Same as DCMean but computed over the summation of all the acceleration signals over all axis
        DCTotalMean[0 + (offset % 2)] = (sumX + sumY + sumZ) / values.size();

        // The differences between the mean values of the X-Y, X-Z, and Y-Z acceleration axis per sensor
        DCPostureDist[0 + offset] = DCMean[0 + offset] - DCMean[1 + offset];
        DCPostureDist[1 + offset] = DCMean[0 + offset] - DCMean[2 + offset];
        DCPostureDist[2 + offset] = DCMean[1 + offset] - DCMean[2 + offset];
    }

    // band-pass filtered (0.1-20 Hz)
    public void calculateBandPass(int offset) {
        float x,y,z;
        float maxX = -1000;
        float minX = 10000;
        float maxY = -1000;
        float minY = 10000;
        float maxZ = -1000;
        float minZ = 10000;

        float absSumX = 0;
        float absSumY = 0;
        float absSumZ = 0;

        float sumX = 0;
        float sumY = 0;
        float sumZ = 0;

        for(float[] line : values) {
            x = line[0];
            y = line[1];
            z = line[2];

            if(x < minX) {
                minX = x;
            }
            if(x > maxX) {
                maxX = x;
            }
            if(y < minY) {
                minY = y;
            }
            if(y > maxY) {
                maxY = y;
            }
            if(z < minZ) {
                minZ = z;
            }
            if(z > maxZ) {
                maxZ = z;
            }

            absSumX += Math.abs(x);
            absSumY += Math.abs(y);
            absSumZ += Math.abs(z);

            sumX += x;
            sumY += y;
            sumZ += z;
        }

        // The area under the absolute value of the signal computed by simply summing the accelerometer samples
        ACAbsArea[0 + offset] = absSumX;
        ACAbsArea[1 + offset] = absSumY;
        ACAbsArea[2 + offset] = absSumZ;

        // Same as ACAbsArea but computed over the summation of all the signals over all axis
        ACTotalAbsArea[0 + (offset % 2)] = absSumX + absSumY + absSumZ;

        // Mean or average over the absolute value
        ACAbsMean[0 + offset] = absSumX / values.size();
        ACAbsMean[1 + offset] = absSumY / values.size();
        ACAbsMean[2 + offset] = absSumZ / values.size();

        // Difference between the maximum and minimum values
        ACRange[0 + offset] = maxX - minX;
        ACRange[1 + offset] = maxY - minY;
        ACRange[2 + offset] = maxZ - minZ;

        float meanX = sumX / values.size();
        float meanY = sumY / values.size();
        float meanZ = sumZ / values.size();
        float sqDiffX = 0;
        float sqDiffY = 0;
        float sqDiffZ = 0;

        // loop again to compute the variance
        for(float[] line : values) {
            sqDiffX += Math.pow(line[0] - meanX, 2);
            sqDiffY += Math.pow(line[1] - meanY, 2);
            sqDiffZ += Math.pow(line[2] - meanZ, 2);
        }

        // The variance of the accelerometer signal
        ACVar[0 + offset] = sqDiffX / values.size();
        ACVar[1 + offset] = sqDiffY / values.size();
        ACVar[2 + offset] = sqDiffZ / values.size();

        // Computed as the ratio of the standard deviation and the mean over each signal window multiplied by 100
        //ACAbsCV
    }

}
