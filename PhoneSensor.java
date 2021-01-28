package jp.sozolab.fahlog.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import jp.sozolab.fahlog.sensor.AccFeature;
import jp.sozolab.fahlog.storage.FileIO;
import jp.sozolab.fahlog.storage.Preference;

import java.text.DecimalFormat;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static jp.sozolab.fahlog.Utility.Utility.log;

public class PhoneSensor implements SensorEventListener {

    private static Preference prefs;

    private static Context context;
    ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    AccFeature accFeature;

    public PhoneSensor(){
        accFeature = new AccFeature();
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        scheduledThreadPoolExecutor.scheduleAtFixedRate(accFeature, 0, accFeature.TIME_SHIFT, TimeUnit.SECONDS);
    }

    public static void init(Context context) {
        PhoneSensor.context = context;
    }

    private static SensorManager sm1, sm2, sm3, sm4, sm5, sm6, sm7, sm8;

    public void start() {
        log( "start");

        //Acc
        sm1 = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (sm1 != null) {
            boolean result = sm1.registerListener(this,
                    sm1.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);
            log( "Sensor Acc registered "+result);
        }

        //Mag
        sm2 = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sm2!=null) {
            boolean result = sm2.registerListener(this,
                    sm2.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                    SensorManager.SENSOR_DELAY_NORMAL);

            log( "Sensor MAG registered "+result);
        }

        //Ori
        sm3 = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sm3 != null) {
            boolean result = sm3.registerListener(this,
                    sm3.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_DELAY_NORMAL);
            log( "Sensor Ori registered "+result);
        }

        //Gyro
        sm4 = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sm4 != null) {

            boolean result = sm4.registerListener(this,
                    sm4.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                    SensorManager.SENSOR_DELAY_NORMAL);
            log( "Sensor Gyro registered "+result);
        }

        //Light
        sm5 = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sm5!=null) {

            boolean result =sm5.registerListener(this,
                    sm5.getDefaultSensor(Sensor.TYPE_LIGHT),
                    SensorManager.SENSOR_DELAY_NORMAL);
            log( "Sensor Light registered "+ result);
        }


    }


    public static boolean doWrite = true;
    private static String f1, f2, f3, f4, f5, f6, f7;
    
    public static String Acc, Mag, Ori, Gyro, Light, Hum, Tem;

    @Override
    public void onSensorChanged(SensorEvent event) {
        String message = "";
        
        DecimalFormat df = new DecimalFormat("#,##0.000");

        if (doWrite) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:

                    if (!prefs.accOn()) break;
                    message = getMessage(event, 3);
                    Acc = message;
                    String udpMessage = getMessage(event, 3, 1.0/9.8);

                    f1 = FileIO.writeSensorSample(Sensors.ACC, f1, message, udpMessage);
                    accFeature.push(message);

                    break;

                case Sensor.TYPE_MAGNETIC_FIELD:
                    if (!prefs.magOn()) break;
                    message = getMessage(event, 3);
                    Mag = message;
                    f2 = FileIO.writeSensorSample(Sensors.MAG, f2, message);
                    break;

                case Sensor.TYPE_ORIENTATION:
                    if(!prefs.oriOn()) break;
                    message = getMessage(event, 3);
                    Ori = message;
                    f3 = FileIO.writeSensorSample(Sensors.ORI, f3, message);
                    break;

                case Sensor.TYPE_GYROSCOPE:
                    if(!prefs.gyroOn()) break;
                    message = getMessage(event, 3);
                    Gyro = message;
                    f4 = FileIO.writeSensorSample(Sensors.GYRO, f4, message);
                    break;

                case Sensor.TYPE_LIGHT:
                    if(!prefs.lightOn()) break;
                    message = getMessage(event, 1);
                    Light = message;
                    f5 = FileIO.writeSensorSample(Sensors.LIGHT, f5, message);
                    break;

                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    if(!prefs.tempOn()) break;
                    message = getMessage(event, 3);
                    Tem = message;
                    f6 = FileIO.writeSensorSample(Sensors.TEMP, f6, message);
                    break;

                case Sensor.TYPE_RELATIVE_HUMIDITY:
                    if(!prefs.humOn()) break;
                    message = getMessage(event, 3);
                    Hum = message;
                    f7 = FileIO.writeSensorSample(Sensors.HUM, f7, message);
                    break;
            }

        }
    }

    private String getMessage(SensorEvent event, int numValues){
        return getMessage(event, numValues, 1.0);
    }

    private String getMessage(SensorEvent event, int numValues, double udpRatio) {

        DecimalFormat df = new DecimalFormat("#,##0.000");// format for decimals
        String values = df.format(event.values[0]*udpRatio);
        for (int i=1;i<numValues;i++)
            values += "," + df.format(event.values[i]*udpRatio);

        return values;
   }

}