package com.znbox.beatlevels.modules;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class Player extends ReactContextBaseJavaModule {

	public Player(ReactApplicationContext context) {
		super(context);
	}

	@ReactMethod(isBlockingSynchronousMethod = false)
	public void set_media(String json, Promise promise) {
		try {
			Intent intent = new Intent();
			intent.setAction("SET_MEDIA");
			intent.putExtra("json", json);
			intent.setClass(getReactApplicationContext(), com.znbox.beatlevels.services.Player.class);
			getReactApplicationContext().startService(intent);
			promise.resolve(true);
		} catch (Exception ex) {
			promise.reject(ex);
			if(ex.getMessage() != null) {
				ex.printStackTrace();
				Log.e("Error: ", ex.getMessage());
			}
		}
	}

	@ReactMethod(isBlockingSynchronousMethod = false)
	public void play(Promise promise) {
		try {
			Intent intent = new Intent();
			intent.setAction("PLAY");
			intent.setClass(getReactApplicationContext(), com.znbox.beatlevels.services.Player.class);
			getReactApplicationContext().startService(intent);
			promise.resolve(true);
		} catch (Exception ex) {
			promise.reject(ex);
			if(ex.getMessage() != null) {
				ex.printStackTrace();
				Log.e("Error: ", ex.getMessage());
			}
		}
	}

	@ReactMethod(isBlockingSynchronousMethod = false)
	public void pause(Promise promise) {
		try {
			Intent intent = new Intent();
			intent.setAction("PAUSE");
			intent.setClass(getReactApplicationContext(), com.znbox.beatlevels.services.Player.class);
			getReactApplicationContext().startService(intent);
			promise.resolve(true);
		} catch (Exception ex) {
			promise.reject(ex);
			if(ex.getMessage() != null) {
				ex.printStackTrace();
				Log.e("Error: ", ex.getMessage());
			}
		}
	}

	@NonNull
	@Override
	public String getName() {
		return "Player";
	}
}
