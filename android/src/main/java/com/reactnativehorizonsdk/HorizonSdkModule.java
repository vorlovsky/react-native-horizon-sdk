package com.reactnativehorizonsdk;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;

import com.hvt.horizonSDK.HorizonSDK;

@ReactModule(name = HorizonSdkModule.NAME)
public class HorizonSdkModule extends ReactContextBaseJavaModule {
    public static final String NAME = "HorizonSdk";

    private static boolean initialized = false;

    public HorizonSdkModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void init(String apiKey, Promise promise) {
        if(!initialized) {
            HorizonSDK.init(getReactApplicationContextIfActiveOrWarn(), apiKey);

            initialized = true;
        }

        promise.resolve("HorizonSDK initialized");
    }

}
