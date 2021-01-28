package jp.sozolab.fahlog.service;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jp.sozolab.gtolog.UI.box.ActTypeBox;

public class TensorFlowClassifier {

    private static String TAG = "TensorFlowClassifier";

    static {
        System.loadLibrary("tensorflow_inference");
    }

    private TensorFlowInferenceInterface inferenceInterface;
    private static final String MODEL_FILE = "file:///android_asset/frozen_model.pb";
    private static final String INPUT_NODE = "inputs";
    private static final String[] OUTPUT_NODES = {"y_"};
    private static final String OUTPUT_NODE = "y_";
    public final int N_SAMPLES = 200;
    private final  int N_DIMENSION = 1;
    private final  int N_FEATURES = 3;
    private  final long[] INPUT_SIZE = {N_DIMENSION, N_SAMPLES, N_FEATURES};
    private static final int OUTPUT_SIZE = 6;

    private String[] labels = {"Downstairs", "Jogging", "Sitting", "Standing", "Upstairs", "Walking"};
    private String className = "";
    private float confidence = 0;

    private NotificationService notification;

    Context context;

    public TensorFlowClassifier(final Context context) {
        this.context = context;
        inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), MODEL_FILE);
        notification = new NotificationService(context);
    }

    public String getClassName() {
        return className;
    }

    public float getConfidence() {
        return confidence;
    }

    public float[] predictProbabilities(float[] data) {
        float[] result = new float[OUTPUT_SIZE];
        inferenceInterface.feed(INPUT_NODE, data, INPUT_SIZE);
        inferenceInterface.run(OUTPUT_NODES);
        inferenceInterface.fetch(OUTPUT_NODE, result);

        return result;
    }


    public void classify(List<Float> features) {

        float[] results = predictProbabilities(toFloatArray(features));

        if (results == null || results.length == 0) {
            return;
        }

        float max = -1;
        int idx = -1;

        for (int i = 0; i < results.length; i++) {
            if (results[i] > max) {
                idx = i;
                max = results[i];
            }
        }

        Log.d(TAG, " tensorFlowClassifier predicted label: " + labels[idx]);
        className = labels[idx];

        try {
            alert();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void alert() throws JSONException {
        ActTypeBox actTypeBox = new ActTypeBox();
        actTypeBox.name(className);
        notification.alert(actTypeBox);
    }

    private float[] toFloatArray(List<Float> list) {
        int i = 0;
        float[] array = new float[list.size()];

        for (Float f : list) {
            array[i++] = (f != null ? f : Float.NaN);
        }

        return array;
    }

    private static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}
