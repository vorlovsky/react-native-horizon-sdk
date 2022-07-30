import React, { useState, useEffect } from 'react';
import { StyleSheet, View, Button } from 'react-native';
import {
  checkMultiple,
  requestMultiple,
  RESULTS,
  PERMISSIONS,
} from 'react-native-permissions';
import RNFS, {
  DownloadDirectoryPath,
  DocumentDirectoryPath,
} from 'react-native-fs';
import {
  HorizonSdk,
  HorizonSdkView,
  CameraHelper,
  CameraMode,
  CameraFacing,
} from 'react-native-horizon-sdk';

const targetDir = DownloadDirectoryPath;
const recordPath = targetDir + `/${getFileName()}.mp4`;

async function checkPermissions() {
  let statuses = await checkMultiple([
    PERMISSIONS.ANDROID.CAMERA,
    PERMISSIONS.ANDROID.RECORD_AUDIO,
    PERMISSIONS.ANDROID.WRITE_EXTERNAL_STORAGE,
  ]);

  const permissionsToRequest = Object.entries(statuses).reduce(
    (accumulator, item) => {
      if (item[1] !== RESULTS.GRANTED) {
        accumulator.push(item[0]);
      }

      return accumulator;
    },
    []
  );

  if (permissionsToRequest.length > 0) {
    statuses = await requestMultiple(permissionsToRequest);

    return Object.values(statuses).every((item) => item === RESULTS.GRANTED);
  }

  return true;
}

function getFileName() {
  const date = new Date();
  return (
    ('0' + date.getDate()).slice(-2) +
    ('0' + (date.getMonth() + 1)).slice(-2) +
    date.getFullYear() +
    ('0' + date.getHours()).slice(-2) +
    ('0' + date.getMinutes()).slice(-2) +
    ('0' + date.getSeconds()).slice(-2)
  );
}

export default function App() {
  const [recording, setRecording] = useState(false);
  const [horizonViewRef, setHorizonViewRef] = useState(null);
  const [videoSize, setVideoSize] = useState(null);
  const [previewDisabled, setPreviewDisabled] = useState(false);
  //const [setupComplete, setSetupComplete] = useState(true);

  useEffect(() => {
    const setup = async () => {
      try {
        let status = await checkPermissions();

        if (!status) {
          console.log('All permissions need to be granted');
          return;
        }

        status = await CameraHelper.hasCamera(CameraFacing.BACK);

        console.log(`hasCamera ${status}`);

        const list = await CameraHelper.getSupportedVideoSize(
          CameraFacing.BACK
        );

        console.log('getSupportedVideoSize:');
        console.log(list);

        const size = list
          .filter((item) => item.height > 720)
          .sort((item1, item2) => item2.height - item1.height)[0];

        console.log('VideoSize', JSON.stringify(size));

        setVideoSize(size);

        const modes = await CameraHelper.getSupportedFlashModes(
          CameraFacing.BACK
        );

        console.log('getSupportedFlashModes:');
        console.log(modes);

        //setSetupComplete(true);
      } catch (error) {
        console.log(error);

        return;
      }
    };

    setup();
  }, []);

  const onRecStop = () => {
    if (recording) {
      // @ts-ignore: Object is possibly 'null'.
      horizonViewRef.stopRecording();
    } else {
      // @ts-ignore: Object is possibly 'null'.
      horizonViewRef.startRecording(recordPath);
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

  const onRecordingFinished = async ({ nativeEvent }) => {
    console.log('onRecordingFinished');
    console.log(nativeEvent);

    const list = await RNFS.readDir(targetDir);
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
