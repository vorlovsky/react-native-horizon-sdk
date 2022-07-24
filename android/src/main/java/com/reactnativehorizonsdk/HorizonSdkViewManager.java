package com.reactnativehorizonsdk;

import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.common.MapBuilder;

import android.content.Context;
import android.hardware.Camera;
import android.view.WindowManager;
import android.view.Surface;

import android.util.Log;

import com.hvt.horizonSDK.CameraHelper;
import com.hvt.horizonSDK.HVTCamcorderProfile;
import com.hvt.horizonSDK.HVTCamera;
import com.hvt.horizonSDK.HVTCameraListener;
import com.hvt.horizonSDK.HVTVars;
import com.hvt.horizonSDK.HVTVars.HVTLevelerCropMode;
import com.hvt.horizonSDK.HVTView;
import com.hvt.horizonSDK.Size;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.List;

public class HorizonSdkViewManager extends SimpleViewManager<View> {
    public static final String REACT_CLASS = "HorizonSdkView";

    private static final String TAG = "ReactNative";

    private static final String COMMAND_START_RUNNING = "startRunning";
    private static final String COMMAND_STOP_RUNNING = "stopRunning";
    // private static final String COMMAND_SET_CAMERA_MODE = "setCameraMode";
    private static final String COMMAND_SET_CAMERA = "setCamera";
    // private static final String COMMAND_SET_SCREEN_ROTATION = "setScreenRotation";
    private static final String COMMAND_START_RECORDING = "startRecording";
    private static final String COMMAND_STOP_RECORDING = "stopRecording";

    private static final String CALLBACK_ON_FAILED_TO_START = "onFailedToStart";
    private static final String CALLBACK_ON_STARTED_RUNNING = "onStartedRunning";
    private static final String CALLBACK_ON_STOPPED_RUNNING = "onStoppedRunning";
    private static final String CALLBACK_ON_RECORDING_STARTED = "onRecordingStarted";
    private static final String CALLBACK_ON_RECORDING_FINISHED = "onRecordingFinished";
    private static final String CALLBACK_ON_PHOTO_CAPTURED = "onPhotoCaptured";
    private static final String CALLBACK_ON_SNAPSHOT_CAPTURED = "onSnapshotCaptured";

    private HVTCamera mHVTCamera;
    private HVTView mCameraPreview;
    private CameraHelper mCameraHelper;

    private File mVideoFile;

    @Override
    @NonNull
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    @NonNull
    public View createViewInstance(ThemedReactContext reactContext) {
        Log.w(TAG, "createViewInstance");

        if(mHVTCamera == null) {
            mHVTCamera = new HVTCamera(reactContext);        
            mHVTCamera.setScreenRotation(Surface.ROTATION_0);
            mHVTCamera.setCameraMode(HVTVars.CameraMode.AUTO);
            
            try {
                mCameraHelper = new CameraHelper();
            } catch (IOException e) {
                e.printStackTrace();
            }
            int facing = mCameraHelper.hasCamera(Camera.CameraInfo.CAMERA_FACING_BACK) ?
                    Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
            Size[] sizes = mCameraHelper.getDefaultVideoAndPhotoSize(facing);
            mHVTCamera.setCamera(facing, sizes[0], sizes[1]);
        }

        if(mCameraPreview == null) {
            mCameraPreview = new HVTView(reactContext);

            mHVTCamera.attachPreviewView(mCameraPreview);
            mHVTCamera.setListener(new CameraListener(mCameraPreview));
            // mHVTCamera.startRunning();
        }

        return mCameraPreview;
    }

    @Override
    public void onDropViewInstance(@NonNull View view) {
        if(view != mCameraPreview) {
            return;
        }

        // mHVTCamera.stopRunning();
        mHVTCamera.detachPreviewView(mCameraPreview);
        mHVTCamera.setListener(null);

        mCameraPreview = null;

        mHVTCamera.destroy();
        mHVTCamera = null;
    }

    @ReactProp(name = "cameraMode")
    public void setCameraMode(View view, String modeName) {
        mHVTCamera.setCameraMode(HVTVars.CameraMode.valueOf(modeName));
    }

    @ReactProp(name = "screenRotation")
    public void setScreenRotation(View view, int rotation) {
        mHVTCamera.setScreenRotation(rotation);
    }

    @ReactProp(name = "previewDisabled")
    public void setPreviewDisabled(View view, boolean status) {
        mCameraPreview.setEnabled(!status);
    }

    @ReactProp(name = "tapToFocus")
    public void setTapToFocus(View view, boolean status) {
        mCameraPreview.setTapToFocus(status);
    }

    @Override
    public void receiveCommand(@NonNull View root, String commandId, @Nullable ReadableArray args) {
        switch(commandId) {
            case COMMAND_START_RUNNING:
                if(!mHVTCamera.isRunning()) {
                    mHVTCamera.startRunning();
                }
                break;

            case COMMAND_STOP_RUNNING:
                if(mHVTCamera.isRunning()) {
                    mHVTCamera.stopRunning();
                }
                break;

            // case COMMAND_SET_SCREEN_ROTATION:
            //     int rotation = args.getInt(0);
            //     mHVTCamera.setScreenRotation(rotation);
            //     break;

            // case COMMAND_SET_CAMERA_MODE:
            //     int mode = args.getInt(0);
                
            //     break;

            case COMMAND_SET_CAMERA:
                boolean running = mHVTCamera.isRunning();
                if(running) {
                    mHVTCamera.stopRunning();
                }

                int cameraFacing = args.getInt(0);

                Size videoSize, photoSize;
                ReadableArray dimensions;
                if(args.isNull(1) && args.isNull(2)) {
                    Size[] defaultSizes = mCameraHelper.getDefaultVideoAndPhotoSize(cameraFacing);

                    videoSize = defaultSizes[0];
                    photoSize = defaultSizes[1];
                } else {
                    if(args.isNull(1)) {
                        dimensions = args.getArray(2);
                        photoSize = new Size(dimensions.getInt(0), dimensions.getInt(1));
                        List<Size> sizes = mCameraHelper.getVideoSizesForPhotoSize(cameraFacing, photoSize);
                        videoSize = sizes.get(0);
                    } else if(args.isNull(2)) {
                        dimensions = args.getArray(1);
                        videoSize = new Size(dimensions.getInt(0), dimensions.getInt(1));
                        List<Size> sizes = mCameraHelper.getPhotoSizesForVideoSize(cameraFacing, videoSize);
                        photoSize = sizes.get(0);
                    } else {
                        dimensions = args.getArray(1);
                        videoSize = new Size(dimensions.getInt(0), dimensions.getInt(1));
                        dimensions = args.getArray(2);
                        photoSize = new Size(dimensions.getInt(0), dimensions.getInt(1));
                    }
                }

                Log.i(TAG, videoSize.getWidth() + "x" + videoSize.getHeight());

                mHVTCamera.setCamera(cameraFacing, videoSize, photoSize);

                if(running) {
                    mHVTCamera.startRunning();
                }
                break;

            case COMMAND_START_RECORDING:
                if(mVideoFile == null) {
                    String filePath = args.getString(0);

                    mVideoFile = new File(filePath);

                    Size size = mHVTCamera.getOutputMovieSize();
                    HVTCamcorderProfile recordingProfile = new HVTCamcorderProfile(size.getWidth(), size.getHeight()); // helper method to create a profile

                    mHVTCamera.startRecording(mVideoFile, recordingProfile);
                }                
                break;

            case COMMAND_STOP_RECORDING:
                if(mVideoFile != null) {
                    mHVTCamera.stopRecording();

                    mVideoFile = null;
                }
                break;
        }
    }

    @Override
    @Nullable 
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.<String, Object>builder()
        .put(
            CameraListener.EVENT_FAILED_TO_START,
            MapBuilder.of("registrationName", CALLBACK_ON_FAILED_TO_START)
        )
        .put(
            CameraListener.EVENT_STARTED_RUNNING,
            MapBuilder.of("registrationName", CALLBACK_ON_STARTED_RUNNING)
        )
        .put(
            CameraListener.EVENT_STOPPED_RUNNING,
            MapBuilder.of("registrationName", CALLBACK_ON_STOPPED_RUNNING)
        )
        .put(
            CameraListener.EVENT_RECORDING_STARTED,
            MapBuilder.of("registrationName", CALLBACK_ON_RECORDING_STARTED)
        )
        .put(
            CameraListener.EVENT_RECORDING_FINISHED,
            MapBuilder.of("registrationName", CALLBACK_ON_RECORDING_FINISHED)
        )
        .put(
            CameraListener.EVENT_PHOTO_CAPTURED,
            MapBuilder.of("registrationName", CALLBACK_ON_PHOTO_CAPTURED)
        )
        .put(
            CameraListener.EVENT_SNAPSHOT_CAPTURED,
            MapBuilder.of("registrationName", CALLBACK_ON_SNAPSHOT_CAPTURED)
        ).build();
    }

    private class CameraListener implements HVTCameraListener {
        public static final String EVENT_FAILED_TO_START = "failedToStart";
        public static final String EVENT_STARTED_RUNNING = "startedRunning";
        public static final String EVENT_STOPPED_RUNNING = "stoppedRunning";
        public static final String EVENT_RECORDING_STARTED = "recordingStarted";
        public static final String EVENT_RECORDING_FINISHED = "recordingFinished";
        public static final String EVENT_PHOTO_CAPTURED = "photoCaptured";
        public static final String EVENT_SNAPSHOT_CAPTURED = "snapshotCaptured";

        private View view;

        public CameraListener(View cameraView) {
            this.view = cameraView;
        }

        private void dispatchEvent(String eventName) {
            dispatchEvent(eventName, null);
        }

        private void dispatchEvent(String eventName, @Nullable WritableMap event) {
            ((ThemedReactContext) view.getContext())
                .getJSModule(RCTEventEmitter.class)
                .receiveEvent(view.getId(), eventName, event);
        }

        @Override
        public void onFailedToStart() {
            Log.i(TAG, "onFailedToStart");
            dispatchEvent(EVENT_FAILED_TO_START);
        }

        @Override
        public void onStartedRunning(Camera.Parameters parameters, int i) {
            Log.i(TAG, "onStartedRunning");
            dispatchEvent(EVENT_STARTED_RUNNING);
        }

        @Override
        public void onPreviewHasBeenRunning(Camera.Parameters parameters, int i) {
            Log.i(TAG, "onPreviewHasBeenRunning");  
        }

        @Override
        public void onWillStopRunning() {
            Log.i(TAG, "onWillStopRunning");
        }

        @Override
        public void onStoppedRunning() {
            Log.i(TAG, "onStoppedRunning");
            dispatchEvent(EVENT_STOPPED_RUNNING);
        }

        @Override
        public void onRecordingHasStarted() {
            Log.i(TAG, "onRecordingHasStarted");
            dispatchEvent(EVENT_RECORDING_STARTED);
        }

        @Override
        public void onRecordingWillStop() {
            Log.i(TAG, "onRecordingWillStop");
        }

        @Override
        public void onRecordingFinished(File file, boolean success) {   
            Log.i(TAG, "onRecordingFinished "+file.getAbsolutePath()+" "+success);         
            WritableMap event = Arguments.createMap();
            event.putString("path", file.getAbsolutePath());
            event.putBoolean("success", success);
            dispatchEvent(EVENT_RECORDING_FINISHED, event);
        }

        @Override
        public void onPhotoCaptured(File file, boolean success) {
            Log.i(TAG, "onPhotoCaptured "+file.getAbsolutePath()+" "+success);         
            WritableMap event = Arguments.createMap();
            event.putString("path", file.getAbsolutePath());
            event.putBoolean("success", success);
            dispatchEvent(EVENT_PHOTO_CAPTURED, event);
        }

        @Override
        public void onSnapshotCaptured(File file) {
            Log.i(TAG, "onSnapshotCaptured "+file.getAbsolutePath());         
            WritableMap event = Arguments.createMap();
            event.putString("path", file.getAbsolutePath());
            dispatchEvent(EVENT_SNAPSHOT_CAPTURED, event);
        }

        @Override
        public void onAngleUpdated(float angle, float scale) {
        }

        @Override
        public void onSensorNotResponding() {           
        }

        @Override
        public void onSensorResponded() {

        }
    }
}
