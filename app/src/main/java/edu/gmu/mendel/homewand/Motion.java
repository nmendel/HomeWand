package edu.gmu.mendel.homewand;

/**
 * Created by 575724 on 11/24/2017.
 */

import android.util.Log;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class Motion {

    public static final int NUM_SENSOR_DIMENSIONS = 3;
    public static final int TOTAL_NUM_FEATURES = 64;

    public String motionTime;
    public List<float[]> values = new ArrayList<float[]>();

    // low-pass filtered (1 Hz)
    public float[] DCArea = new float[6];
    public float[] DCMean = new float[6];
    public float[] DCTotalMean = new float[2]; // single value per sensor
    public float[] DCPostureDist = new float[6];

    // TODO:
    // band-pass filtered (0-0.7 Hz)
    public float[] ACLowEnergy;

    // band-pass filtered (0.1-20 Hz)
    public float[] ACAbsArea = new float[6];
    public float[] ACTotalAbsArea = new float[2]; // single value per sensor
    public float[] ACAbsMean = new float[6];
    public float[] ACEnergy = new float[6];
    public float[] ACVar = new float[6];
    public float[] ACAbsCV = new float[6];
    public float[] ACIQR = new float[6];
    public float[] ACRange = new float[6];

    Motion(String motionTime) {
        this.motionTime = motionTime;
    }

    public void addData(String type, String fileData) {

        String[] lines = fileData.split("\n");
        for(int i = 0; i < lines.length; i++) {
            //Log.d("line", lines[i]);
            String[] splitLine = lines[i].split(",");
            Long time = new Long(splitLine[0]);
            Float x = new Float(splitLine[1]);
            Float y = new Float(splitLine[2]);
            Float z = new Float(splitLine[3]);

            values.add(new float[] {x.floatValue(), y.floatValue(), z.floatValue()});
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

    // TODO:
    // band-pass filtered (0.1-20 Hz)
    public void calculateBandPass(int offset) {
        float x,y,z;
        Float[] xVals = new Float[values.size()];
        Float[] yVals = new Float[values.size()];
        Float[] zVals = new Float[values.size()];

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

        for(int i = 0; i < values.size(); i++) {
            x = values.get(i)[0];
            y = values.get(i)[1];
            z = values.get(i)[2];

            xVals[i] = x;
            yVals[i] = y;
            zVals[i] = z;

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
        float absMeanX = absSumX / values.size();
        float absMeanY = absSumY / values.size();
        float absMeanZ = absSumZ / values.size();
        ACAbsMean[0 + offset] = absMeanX;
        ACAbsMean[1 + offset] = absMeanY;
        ACAbsMean[2 + offset] = absMeanZ;

        // Difference between the maximum and minimum values
        ACRange[0 + offset] = maxX - minX;
        ACRange[1 + offset] = maxY - minY;
        ACRange[2 + offset] = maxZ - minZ;

        // Computed as the difference between quartiles three and one (Q3-Q1).
        int i = 0;
        Complex[][] fftInputs = new Complex[3][];

        for(Float[] vals : Arrays.asList(xVals, yVals, zVals)) {
            Float[] v = new Float[vals.length];
            System.arraycopy(vals, 0, v, 0, vals.length);
            Arrays.sort(v);

            // find indexes for first and third quartiles of the sorted array
            int n25 = (int) Math.round(v.length * 25 / 100);
            int n75 = (int) Math.round(v.length * 75 / 100);

            ACIQR[i + offset] = v[n75] - v[n25];

            int fftInputLength = vals.length;
            while(Integer.bitCount(fftInputLength) != 1) {
                fftInputLength++;
            }

            fftInputs[i] = new Complex[fftInputLength];
            for(int j = 0; j < vals.length; j++) {
                fftInputs[i][j] = new Complex(vals[j].doubleValue());
            }
            for(int j = vals.length; j < fftInputLength; j++) {
                fftInputs[i][j] = new Complex(0);
            }


            i++;
        }

        // It is computed as follows from the FFT coefficient magnitudes:
        //   for i=1 to size/2 : magnitude ^ 2
        FastFourierTransformer fftx = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] inputX = fftx.transform(fftInputs[0], TransformType.FORWARD);
        FastFourierTransformer ffty = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] inputY = ffty.transform(fftInputs[1], TransformType.FORWARD);
        FastFourierTransformer fftz = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] inputZ = fftz.transform(fftInputs[2], TransformType.FORWARD);

        ACEnergy[0 + offset] = 0;
        ACEnergy[1 + offset] = 0;
        ACEnergy[2 + offset] = 0;
        int j = 0;
        for(Complex[] vals : Arrays.asList(inputX, inputY, inputZ)) {
            for (int k = 1; k < (vals.length / 2); k++) {
                ACEnergy[j + offset] += Math.pow(vals[k].getReal(), 2);
            }
            j++;
        }

        float meanX = sumX / values.size();
        float meanY = sumY / values.size();
        float meanZ = sumZ / values.size();
        float sqDiffX = 0;
        float sqDiffY = 0;
        float sqDiffZ = 0;
        float absSqDiffX = 0;
        float absSqDiffY = 0;
        float absSqDiffZ = 0;

        // loop again to compute the variance
        for(float[] line : values) {
            sqDiffX += Math.pow(line[0] - meanX, 2);
            sqDiffY += Math.pow(line[1] - meanY, 2);
            sqDiffZ += Math.pow(line[2] - meanZ, 2);
            absSqDiffX += Math.pow(Math.abs(line[0]) - absMeanX, 2);
            absSqDiffY += Math.pow(Math.abs(line[1]) - absMeanY, 2);
            absSqDiffZ += Math.pow(Math.abs(line[2]) - absMeanZ, 2);
        }

        // The variance of the accelerometer signal
        ACVar[0 + offset] = sqDiffX / values.size();
        ACVar[1 + offset] = sqDiffY / values.size();
        ACVar[2 + offset] = sqDiffZ / values.size();

        // Computed as the ratio of the standard deviation and the mean over each signal window multiplied by 100
        //                             ( std dev (       variance        ) ) /  ( mean )
        double stdDevX = Math.sqrt(absSqDiffX / values.size());
        double stdDevY = Math.sqrt(absSqDiffY / values.size());
        double stdDevZ = Math.sqrt(absSqDiffZ / values.size());
        ACAbsCV[0 + offset] = (float)( stdDevX / absMeanX ) * 100;
        ACAbsCV[1 + offset] = (float)( stdDevY / absMeanY ) * 100;
        ACAbsCV[2 + offset] = (float)( stdDevZ / absMeanZ ) * 100;
        if(absMeanX == 0) { ACAbsCV[0 + offset] = 0.0f; }
        if(absMeanY == 0) { ACAbsCV[1 + offset] = 0.0f; }
        if(absMeanZ == 0) { ACAbsCV[2 + offset] = 0.0f; }
    }

    public String toString() {
        String output = "";
        output += Arrays.toString(DCArea) + "\n";
        output += Arrays.toString(DCMean) + "\n";
        output += Arrays.toString(DCTotalMean) + "\n";
        output += Arrays.toString(DCPostureDist) + "\n";
        //output += Arrays.toString(ACLowEnergy) + "\n";
        output += Arrays.toString(ACAbsArea) + "\n";
        output += Arrays.toString(ACTotalAbsArea) + "\n";
        output += Arrays.toString(ACAbsMean) + "\n";
        output += Arrays.toString(ACEnergy) + "\n";
        output += Arrays.toString(ACVar) + "\n";
        output += Arrays.toString(ACAbsCV) + "\n";
        output += Arrays.toString(ACIQR) + "\n";
        output += Arrays.toString(ACRange);

        return output;
    }

    public Instance getInstance() {
        double[] values = new double[TOTAL_NUM_FEATURES];
        int i = 0;
        // TODO: aclowenergy
        for(float[] vals : Arrays.asList(DCArea, DCMean, DCTotalMean, DCPostureDist,//        ACLowEnergy;
                                         ACAbsArea, ACTotalAbsArea, ACAbsMean, ACEnergy,
                                         ACVar, ACAbsCV, ACIQR, ACRange)) {
            for(int j = 0; j < vals.length; j++) {
                values[i] = vals[j];
                i++;
            }
        }

        return new DenseInstance(1, values);
    }

    public static Instances getDataset(String datasetName) {
        FastVector atts = new FastVector();
        //atts.addElement(new Attribute("filename", (FastVector) null));
        // - numeric
        atts.addElement(new Attribute("DCAreaAX"));
        atts.addElement(new Attribute("DCAreaAY"));
        atts.addElement(new Attribute("DCAreaAZ"));
        atts.addElement(new Attribute("DCAreaGX"));
        atts.addElement(new Attribute("DCAreaGY"));
        atts.addElement(new Attribute("DCAreaGZ"));
        atts.addElement(new Attribute("DCMeanAX"));
        atts.addElement(new Attribute("DCMeanAY"));
        atts.addElement(new Attribute("DCMeanAZ"));
        atts.addElement(new Attribute("DCMeanGX"));
        atts.addElement(new Attribute("DCMeanGY"));
        atts.addElement(new Attribute("DCMeanGZ"));
        atts.addElement(new Attribute("DCTotalMeanA"));
        atts.addElement(new Attribute("DCTotalMeanG"));
        atts.addElement(new Attribute("DCPostureDistAX"));
        atts.addElement(new Attribute("DCPostureDistAY"));
        atts.addElement(new Attribute("DCPostureDistAZ"));
        atts.addElement(new Attribute("DCPostureDistGX"));
        atts.addElement(new Attribute("DCPostureDistGY"));
        atts.addElement(new Attribute("DCPostureDistGZ"));
        /*
        atts.addElement(new Attribute("ACLowEnergyAX"));
        atts.addElement(new Attribute("ACLowEnergyAY"));
        atts.addElement(new Attribute("ACLowEnergyAZ"));
        atts.addElement(new Attribute("ACLowEnergyGX"));
        atts.addElement(new Attribute("ACLowEnergyGY"));
        atts.addElement(new Attribute("ACLowEnergyGZ"));
        */
        atts.addElement(new Attribute("ACAbsAreaAX"));
        atts.addElement(new Attribute("ACAbsAreaAY"));
        atts.addElement(new Attribute("ACAbsAreaAZ"));
        atts.addElement(new Attribute("ACAbsAreaGX"));
        atts.addElement(new Attribute("ACAbsAreaGY"));
        atts.addElement(new Attribute("ACAbsAreaGZ"));
        atts.addElement(new Attribute("ACTotalAbsAreaA"));
        atts.addElement(new Attribute("ACTotalAbsAreaG"));
        atts.addElement(new Attribute("ACAbsMeanAX"));
        atts.addElement(new Attribute("ACAbsMeanAY"));
        atts.addElement(new Attribute("ACAbsMeanAZ"));
        atts.addElement(new Attribute("ACAbsMeanGX"));
        atts.addElement(new Attribute("ACAbsMeanGY"));
        atts.addElement(new Attribute("ACAbsMeanGZ"));
        atts.addElement(new Attribute("ACEnergyAX"));
        atts.addElement(new Attribute("ACEnergyAY"));
        atts.addElement(new Attribute("ACEnergyAZ"));
        atts.addElement(new Attribute("ACEnergyGX"));
        atts.addElement(new Attribute("ACEnergyGY"));
        atts.addElement(new Attribute("ACEnergyGZ"));
        atts.addElement(new Attribute("ACVarAX"));
        atts.addElement(new Attribute("ACVarAY"));
        atts.addElement(new Attribute("ACVarAZ"));
        atts.addElement(new Attribute("ACVarGX"));
        atts.addElement(new Attribute("ACVarGY"));
        atts.addElement(new Attribute("ACVarGZ"));
        atts.addElement(new Attribute("ACAbsCVAX"));
        atts.addElement(new Attribute("ACAbsCVAY"));
        atts.addElement(new Attribute("ACAbsCVAZ"));
        atts.addElement(new Attribute("ACAbsCVGX"));
        atts.addElement(new Attribute("ACAbsCVGY"));
        atts.addElement(new Attribute("ACAbsCVGZ"));
        atts.addElement(new Attribute("ACIQRAX"));
        atts.addElement(new Attribute("ACIQRAY"));
        atts.addElement(new Attribute("ACIQRAZ"));
        atts.addElement(new Attribute("ACIQRGX"));
        atts.addElement(new Attribute("ACIQRGY"));
        atts.addElement(new Attribute("ACIQRGZ"));
        atts.addElement(new Attribute("ACRangeAX"));
        atts.addElement(new Attribute("ACRangeAY"));
        atts.addElement(new Attribute("ACRangeAZ"));
        atts.addElement(new Attribute("ACRangeGX"));
        atts.addElement(new Attribute("ACRangeGY"));
        atts.addElement(new Attribute("ACRangeGZ"));

        return new Instances(datasetName, atts, 0);
    }

}
