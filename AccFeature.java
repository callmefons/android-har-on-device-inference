package jp.sozolab.fahlog.sensor.feature;

import java.util.ArrayList;

import jp.sozolab.fahlog.sensor.Sensors;
import jp.sozolab.fahlog.storage.FileIO;
import jp.sozolab.fahlog.storage.Preference;

import static jp.sozolab.fahlog.Utility.Utility.log;

public class AccFeature implements Runnable{

    public static int TIME_SHIFT = 5; // shift time window after every 1s
    public static int TIME_WINDOW_SIZE = 10; // 1 window contains 2 seconds' data = 10 data records
    public static int OVERLAPPING = TIME_WINDOW_SIZE / TIME_SHIFT; // we need 2 sensor buffers

    protected String f1;
    int sensorIndex;
    SensorBuffer[] sensorBuffers;

    public AccFeature(){
        f1 = "";
        sensorIndex = 0;
        sensorBuffers = new SensorBuffer[OVERLAPPING];

        for(int i = 0; i < OVERLAPPING; i++){
            sensorBuffers[i] = new SensorBuffer();
        }
    }

    public void push(String message) {

        if(message != null){

            String values[] = message.split(",");

            float Ax = Float.parseFloat(values[0]);
            float Ay = Float.parseFloat(values[1]);
            float Az = Float.parseFloat(values[2]);

            for(int i = 0; i < OVERLAPPING; i++){
                sensorBuffers[i].push(Ax, Ay, Az);
            }

            //Log.v(TAG, "Acc value : " + Ax + " , " + Ay + " , " + Az);
        }
    }

    @Override
    public void run() {
        sensorIndex = sensorIndex % OVERLAPPING;
        calcFeature(sensorIndex);
        sensorIndex++;
    }

    public void calcFeature(int sensorIndex){
        if (!Preference.accFeatOn()) return;

        SensorBuffer sensors = sensorBuffers[sensorIndex];

        ArrayList<SensorSample> sensorBuffer = sensors.getSensorBuffer();
        int accArrayListSize = sensorBuffer.size();

        double AxSum = 0;
        double AySum = 0;
        double AzSum = 0;

        for (SensorSample sensor : sensorBuffer) {
            AxSum = AxSum + sensor.x();
            AySum = AySum + sensor.y();
            AzSum = AzSum + sensor.z();
        }

        double meanAx = AxSum / accArrayListSize;
        double meanAy = AySum / accArrayListSize;
        double meanAz = AzSum / accArrayListSize;

        //Log.v(TAG, " Mean Acc value : " + meanAx + " , " + meanAy + " , " + meanAz);

        // calculate Features
        double min = Math.min(Math.min(meanAx, meanAy), meanAz);
        double max = Math.max(Math.max(meanAx, meanAy), meanAz);
        double mean = (meanAx + meanAy + meanAz) / 3;
        double rms = Math.sqrt((Math.pow(meanAx, 2) + Math.pow(meanAy, 2) + Math.pow(meanAz, 2)) / 3);
        double variance = (Math.pow((meanAx - mean), 2) + Math.pow((meanAy - mean), 2) + Math.pow((meanAz - mean), 2)) / 3;
        double std = Math.sqrt(variance);

        String featureString = min + "," + max + "," + mean + "," + rms + "," + variance + "," + std;
        f1 = FileIO.writeSensorSample(Sensors.FEAT_ACC, f1, featureString);

        log("Acc Features : " + " Min : " + min + " Max : " + max + " Mean : " + mean
                + " RMS : " + rms + " Variance : " + variance + " STD : " + std);
    }
}
