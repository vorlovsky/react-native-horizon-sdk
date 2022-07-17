package com.reactnativehorizonsdk;

import android.util.Log;
import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.module.annotations.ReactModule;

import com.hvt.horizonSDK.HorizonSDK;
import com.hvt.horizonSDK.CameraHelper;
import com.hvt.horizonSDK.Size;

import java.util.List;
import java.io.IOException;

@ReactModule(name = CameraHelperModule.NAME)
public class CameraHelperModule extends ReactContextBaseJavaModule {
    public static final String NAME = "CameraHelper";

    private CameraHelper mCameraHelper;

    public CameraHelperModule(ReactApplicationContext reactContext) {
        super(reactContext);

        try {
            mCameraHelper = new CameraHelper();
        } catch (IOException e) {
            Log.e("HorizonSDK error", "Unable to create CameraHelper instance");

            return;
        }
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }
    
    @ReactMethod
    public void hasCamera(int cameraFacing, Promise promise) {
        boolean status = mCameraHelper.hasCamera(cameraFacing);

        promise.resolve(status);
    }

    @ReactMethod
    public void getSupportedVideoSize(int cameraFacing, Promise promise) {
        List<Size> sizeList = mCameraHelper.getSupportedVideoSize(cameraFacing);

        WritableArray array = new WritableNativeArray();
        for (Size size : sizeList) {
            WritableMap map = new WritableNativeMap();
            map.putInt("width", size.getWidth());
            map.putInt("height", size.getHeight());
            map.putDouble("aspectRatio", size.getAspectRatio());
            array.pushMap(map);
        }

        promise.resolve(array);
    }

    @ReactMethod
    public void getSupportedFlashModes(int cameraFacing, Promise promise) {
        List<String> modesList = mCameraHelper.getSupportedFlashModes(cameraFacing);

        promise.resolve(Arguments.fromList(modesList));
    }
}
