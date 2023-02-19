package com.znbox.beatlevels.services;

import android.content.Intent;
import android.os.Bundle;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.facebook.react.jstasks.HeadlessJsTaskRetryPolicy;
import com.facebook.react.jstasks.LinearCountingRetryPolicy;

import javax.annotation.Nullable;

public class NativeBridgeService extends HeadlessJsTaskService {
    private static HeadlessJsTaskConfig ht;

    @Override
    protected @Nullable HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        try {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    Bundle extras = intent.getExtras();

                    HeadlessJsTaskRetryPolicy retryPolicy = new LinearCountingRetryPolicy(
                            3, // Max number of retry attempts
                            1000 // Delay between each retry attempt
                    );

                    WritableMap data = extras != null ? Arguments.fromBundle(extras) : null;
                    NativeBridgeService.ht = new HeadlessJsTaskConfig(
                            "NativeBridgeService",
                            data,
                            5000, // timeout for the task
                            true, // optional: defines whether or not  the task is allowed in foreground. Default is false
                            retryPolicy
                    );
                }
            };
            thread.start();
            return NativeBridgeService.ht;
        } catch (Exception e) {
            return null;
        }
    }
}