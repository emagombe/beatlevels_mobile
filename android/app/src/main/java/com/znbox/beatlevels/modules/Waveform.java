package com.znbox.beatlevels.modules;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class Waveform extends ReactContextBaseJavaModule {

    static {
        System.loadLibrary("beatlevels");
    }

    private native int decode_to_pcm(String input, String output);

    public Waveform(ReactApplicationContext context) {
        super(context);
    }

    @ReactMethod(isBlockingSynchronousMethod = false)
    public String set_waveform(String path) {

        return path;
    }

    @Override
    public String getName() {
        return "Waveform";
    }
}
