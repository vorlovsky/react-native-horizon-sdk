package com.reactnativehorizonsdk;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.common.MapBuilder;

import android.hardware.Camera;
import android.view.Surface;

import android.util.Log;

import com.hvt.horizonSDK.CameraHelper;
import com.hvt.horizonSDK.HVTCamcorderProfile;
import com.hvt.horizonSDK.HVTCamera;
import com.hvt.horizonSDK.HVTCameraListener;
import com.hvt.horizonSDK.HVTVars;
import com.hvt.horizonSDK.HVTView;
import com.hvt.horizonSDK.Size;
import com.reactnativehorizonsdk.helper.CameraHelperSingleton;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

@SuppressWarnings("deprecation")
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

  private File mVideoFile;

  private final List<Command> commandsQueue = new ArrayList<>();

  @Override
  @NonNull
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  @NonNull
  public View createViewInstance(ThemedReactContext reactContext) {
    Log.w(TAG, "createViewInstance");

    CameraHelper cameraHelper = CameraHelperSingleton.getInstance();

    if (mHVTCamera == null) {
      int facing = cameraHelper.hasCamera(Camera.CameraInfo.CAMERA_FACING_BACK) ?
        Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;

      mHVTCamera = new HVTCamera(reactContext);
      mHVTCamera.setScreenRotation(Surface.ROTATION_0);
      mHVTCamera.setCameraMode(HVTVars.CameraMode.AUTO);

      Size[] sizes = cameraHelper.getDefaultVideoAndPhotoSize(facing);
      mHVTCamera.setCamera(facing, sizes[0], sizes[1]);
    }

    if (mCameraPreview == null) {
      mCameraPreview = new HVTView(reactContext);

      mHVTCamera.attachPreviewView(mCameraPreview);
      mHVTCamera.setListener(new CameraListener(new EventEmitterInterface() {
        private void dispatchEvent(String eventName, @Nullable WritableMap event) {
          reactContext
            .getJSModule(RCTEventEmitter.class)
            .receiveEvent(mCameraPreview.getId(), eventName, event);
        }

        @Override
        public void receiveEvent(String eventName, @Nullable WritableMap event) {
          // TODO: filter events
          ContextCompat.getMainExecutor(reactContext).execute(new Runnable() {
            @Override
            public void run() {
              processCommandsQueue();
            }
          });

          dispatchEvent(eventName, event);
        }
      }));
      // mHVTCamera.startRunning();
    }

    return mCameraPreview;
  }

  @Override
  public void onDropViewInstance(@NonNull View view) {
    if (view != mCameraPreview) {
      return;
    }

    commandsQueue.clear();

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
    Log.i(TAG, "receiveCommand " + commandId);

    String[] waitingForCameraStateEnter = new String[0];
    String[] waitingForCameraStateExit = new String[0];

    switch (commandId) {
//      case COMMAND_START_RUNNING:
//        waitingForCameraStateEnter = new String[]{};
//        break;
//
//      case COMMAND_STOP_RUNNING:
//        waitingForCameraStateEnter = new String[]{};
//        break;
//
//       case COMMAND_SET_SCREEN_ROTATION:
//           break;
//
//       case COMMAND_SET_CAMERA_MODE:
//           break;

      case COMMAND_SET_CAMERA:
        waitingForCameraStateEnter = new String[]{"isStopped", "isRunningIdle"};
        break;

      case COMMAND_START_RECORDING:
        waitingForCameraStateEnter = new String[]{"isRunningIdle"};
        break;

      case COMMAND_STOP_RECORDING:
        waitingForCameraStateEnter = new String[]{"isRecording"};
        waitingForCameraStateExit = new String[]{"isStartingRecording"};
        break;
    }

    commandsQueue.add(new Command(root, commandId, args, waitingForCameraStateEnter, waitingForCameraStateExit));

    processCommandsQueue();
  }

  private boolean canProcessCommand(Command command) {
    String[] states;
    Method method = null;

    states = command.getWaitingForCameraStateEnter();

    if (states.length > 0) {
      boolean anyOneMet = false;
      for (String state : states) {
        try {
          method = mHVTCamera.getClass().getMethod(state);
        } catch (NoSuchMethodException e) {
          e.printStackTrace();
        }

        try {
          if ((boolean) method.invoke(mHVTCamera)) {
            anyOneMet = true;
            Log.i(TAG, "state enter: "+state);
            break;
          }
        } catch (IllegalAccessException | InvocationTargetException e) {
          e.printStackTrace();
        }
      }

      if (!anyOneMet) {
        Log.i(TAG, "canProcessCommand: "+command.getCommandId() + " "+ false);
        return false;
      }
    }

    states = command.getWaitingForCameraStateExit();

    if (states.length > 0) {
      boolean allMet = true;
      for (String state : states) {
        try {
          method = mHVTCamera.getClass().getMethod(state);
        } catch (NoSuchMethodException e) {
          e.printStackTrace();
        }

        try {
          if ((boolean) method.invoke(mHVTCamera)) {
            allMet = false;
            Log.i(TAG, "state not exit: "+state);
            break;
          }
        } catch (IllegalAccessException | InvocationTargetException e) {
          e.printStackTrace();
        }
      }

      if (!allMet) {
        Log.i(TAG, "canProcessCommand: "+command.getCommandId() + " "+ false);
        return false;
      }
    }

    Log.i(TAG, "canProcessCommand: "+command.getCommandId() + " "+ true);
    return true;
  }

  private void processCommandsQueue() {
    while (!commandsQueue.isEmpty()) {
      Command command = commandsQueue.get(0);
      if (!canProcessCommand(command)) {
        break;
      }
      commandsQueue.remove(0);
      processCommand(command.getView(), command.getCommandId(), command.getArgs());
    }
  }

  private void processCommand(@NonNull View root, String commandId, @Nullable ReadableArray args) {
    Log.i(TAG, "processCommand " + commandId);
    switch (commandId) {
      case COMMAND_START_RUNNING:
        if (mHVTCamera.isStopped()) {
          mHVTCamera.startRunning();
        }
        break;

      case COMMAND_STOP_RUNNING:
        if (mHVTCamera.isRunning()) {
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
        //                boolean running = mHVTCamera.isRunning();
        //                if(running) {
        //                    mHVTCamera.stopRunning();
        //                }

        CameraHelper cameraHelper = CameraHelperSingleton.getInstance();

        int cameraFacing = args.getInt(0);

        Size videoSize, photoSize;
        ReadableArray dimensions;
        if (args.isNull(1) && args.isNull(2)) {
          Size[] defaultSizes = cameraHelper.getDefaultVideoAndPhotoSize(cameraFacing);

          videoSize = defaultSizes[0];
          photoSize = defaultSizes[1];
        } else {
          if (args.isNull(1)) {
            dimensions = args.getArray(2);
            photoSize = new Size(dimensions.getInt(0), dimensions.getInt(1));
            List<Size> sizes = cameraHelper.getVideoSizesForPhotoSize(cameraFacing, photoSize);
            videoSize = sizes.get(0);
            Log.i(TAG, "args1 isNull " + videoSize.toString());
          } else if (args.isNull(2)) {
            dimensions = args.getArray(1);
            videoSize = new Size(dimensions.getInt(0), dimensions.getInt(1));
            Log.i(TAG, "args2 isNull " + videoSize);
            List<Size> sizes = cameraHelper.getPhotoSizesForVideoSize(cameraFacing, videoSize);
            photoSize = sizes.get(0);
          } else {
            dimensions = args.getArray(1);
            videoSize = new Size(dimensions.getInt(0), dimensions.getInt(1));
            Log.i(TAG, "args " + videoSize);
            dimensions = args.getArray(2);
            photoSize = new Size(dimensions.getInt(0), dimensions.getInt(1));
          }
        }

        Log.i(TAG, "video size " + videoSize.toString());

        mHVTCamera.setCamera(cameraFacing, videoSize, photoSize);

//        if(running) {
//            mHVTCamera.startRunning();
//        }
        break;

      case COMMAND_START_RECORDING:
        if (mVideoFile == null) {
          String filePath = args.getString(0);

          mVideoFile = new File(filePath);

          Size size = mHVTCamera.getOutputMovieSize();
          HVTCamcorderProfile recordingProfile = new HVTCamcorderProfile(size.getWidth(), size.getHeight()); // helper method to create a profile

          Log.d(TAG, "output size " + size);

          mHVTCamera.startRecording(mVideoFile, recordingProfile);
        }
        break;

      case COMMAND_STOP_RECORDING:
        if (mVideoFile != null) {
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

  private class Command {
    private View view;
    private final String commandId;
    private final ReadableArray args;

    private String[] waitingForCameraStateEnter;
    private String[] waitingForCameraStateExit;

    public Command(@NonNull View view, String commandId, @Nullable ReadableArray args, String[] waitingForCameraStateEnter, String[] waitingForCameraStateExit) {
      this.view = view;
      this.commandId = commandId;
      this.args = args;

      this.waitingForCameraStateEnter = waitingForCameraStateEnter;
      this.waitingForCameraStateExit = waitingForCameraStateExit;
    }

    public String[] getWaitingForCameraStateEnter() {
      return waitingForCameraStateEnter;
    }

    public String[] getWaitingForCameraStateExit() {
      return waitingForCameraStateExit;
    }

    public View getView() {
      return view;
    }

    public String getCommandId() {
      return commandId;
    }

    public ReadableArray getArgs() {
      return args;
    }
  }

  interface EventEmitterInterface {
    void receiveEvent(String eventName, @Nullable WritableMap event);
  }

  private class CameraListener implements HVTCameraListener {
    public static final String EVENT_FAILED_TO_START = "failedToStart";
    public static final String EVENT_STARTED_RUNNING = "startedRunning";
    public static final String EVENT_STOPPED_RUNNING = "stoppedRunning";
    public static final String EVENT_RECORDING_STARTED = "recordingStarted";
    public static final String EVENT_RECORDING_FINISHED = "recordingFinished";
    public static final String EVENT_PHOTO_CAPTURED = "photoCaptured";
    public static final String EVENT_SNAPSHOT_CAPTURED = "snapshotCaptured";

    private EventEmitterInterface eventEmitter;

    public CameraListener(EventEmitterInterface eventReceiver) {
      this.eventEmitter = eventReceiver;
    }

    private void dispatchEvent(String eventName) {
      dispatchEvent(eventName, null);
    }

    private void dispatchEvent(String eventName, @Nullable WritableMap event) {
      eventEmitter.receiveEvent(eventName, event);
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
      Log.i(TAG, "onRecordingFinished " + file.getAbsolutePath() + " " + success);
      WritableMap event = Arguments.createMap();
      event.putString("path", file.getAbsolutePath());
      event.putBoolean("success", success);
      dispatchEvent(EVENT_RECORDING_FINISHED, event);
    }

    @Override
    public void onPhotoCaptured(File file, boolean success) {
      Log.i(TAG, "onPhotoCaptured " + file.getAbsolutePath() + " " + success);
      WritableMap event = Arguments.createMap();
      event.putString("path", file.getAbsolutePath());
      event.putBoolean("success", success);
      dispatchEvent(EVENT_PHOTO_CAPTURED, event);
    }

    @Override
    public void onSnapshotCaptured(File file) {
      Log.i(TAG, "onSnapshotCaptured " + file.getAbsolutePath());
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
