package com.fiskur.bbcmicro;

import android.util.Log;

/**
 * Created by jonathan.fisher on 04/12/2014.
 */
public class L {
    private static final String TAG = "BBCMicro";

    public static void l(String message){
        Log.d(TAG, message);
    }

    public static void e(String message){
        Log.e(TAG, message);
    }
}
