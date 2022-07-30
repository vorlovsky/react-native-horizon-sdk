package com.reactnativehorizonsdk.helper;

import android.util.Log;

import com.hvt.horizonSDK.CameraHelper;

import java.io.IOException;

public class CameraHelperSingleton {
  private static class Loader {
    static CameraHelper CameraHelperInstance = null;

    static {
      try {
        CameraHelperInstance = new CameraHelper();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static synchronized CameraHelper getInstance() {
    return Loader.CameraHelperInstance;
  }
}
