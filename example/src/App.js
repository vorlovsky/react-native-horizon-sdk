import React, { useState, useEffect } from 'react';

import { StyleSheet, View, Button } from 'react-native';
import RNFS, { DownloadDirectoryPath } from 'react-native-fs';
import {
  HorizonSdk,
  HorizonSdkView,
  CameraHelper,
  CameraMode,
  CameraFacing,
} from 'react-native-horizon-sdk';

const API_KEY =
  'm8W7J+sSoGogrwibc/TCXX/M8h9XOFozFIQ1r03zhOmpOK9Bcfde8jUrEXthYmPfHn434/WNyjfF82i0Nv5cCmNvbS5odnQucGV0cmFrZWFzLnNpbXBsZWFwcHxOT3xhbmRyb2lk';

export default function App() {
  const [recording, setRecording] = useState(false);
  const [horizonViewRef, setHorizonViewRef] = useState(null);
  const [videoSize, setVideoSize] = useState(null);
  const [previewDisabled, setPreviewDisabled] = useState(false);

  useEffect(() => {
    const test = async () => {
      try {
        const status = await CameraHelper.hasCamera(CameraFacing.BACK);

        console.log(status);

        const list = await CameraHelper.getSupportedVideoSize(
          CameraFacing.BACK
        );

        console.log(list);

        const size = list
          .filter((item) => item.height > 720)
          .sort((item1, item2) => item1.height - item2.height)[0];

        console.log(size);

        setVideoSize(size);

        const modes = await CameraHelper.getSupportedFlashModes(
          CameraFacing.BACK
        );

        console.log(modes);
      } catch (error) {
        console.log(error);

        return;
      }
    };

    test();

    HorizonSdk.init(API_KEY);
  }, []);

  const onRecStop = () => {
    if (recording) {
      // @ts-ignore: Object is possibly 'null'.
      horizonViewRef.stopRecording();
    } else {
      // @ts-ignore: Object is possibly 'null'.
      horizonViewRef.startRecording(DownloadDirectoryPath + '/rec.mp4');
    }

    setRecording(!recording);
  };

  const onTogglePreview = () => {
    setPreviewDisabled(!previewDisabled);
  };

  const onFailedToStart = () => {
    console.log('onFailedToStart');
  };

  const onStartedRunning = () => {
    console.log('onStartedRunning');
  };

  const onStoppedRunning = () => {
    console.log('onStoppedRunning');
  };

  const onRecordingStarted = () => {
    console.log('onRecordingStarted');
  };

  const onRecordingFinished = async (event) => {
    console.log('onRecordingFinished');
    console.log(event.nativeEvent);

    const list = await RNFS.readDir(DownloadDirectoryPath);
    list.forEach((item) => console.log(item.path, item.size));
  };

  return (
    <View style={styles.container}>
      <HorizonSdkView
        ref={setHorizonViewRef}
        style={styles.horizon}
        cameraMode={CameraMode.VIDEO}
        videoSize={videoSize}
        previewDisabled={previewDisabled}
        screenRotation={0}
        onFailedToStart={onFailedToStart}
        onStartedRunning={onStartedRunning}
        onStoppedRunning={onStoppedRunning}
        onRecordingStarted={onRecordingStarted}
        onRecordingFinished={onRecordingFinished}
      />
      <View style={styles.recStop}>
        <Button
          disabled={horizonViewRef == null}
          title={previewDisabled ? 'Show Preview' : 'Hide Preview'}
          onPress={onTogglePreview}
        />
        <Button
          disabled={horizonViewRef == null}
          title={recording ? 'Stop' : 'Rec'}
          onPress={onRecStop}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  horizon: {
    width: '100%',
    height: '100%',
  },
  recStop: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
  },
});
