import React, { useState, useEffect } from 'react';
import { StyleSheet, View, Button } from 'react-native';
import {
  checkMultiple,
  requestMultiple,
  RESULTS,
} from 'react-native-permissions';
import RNFS, { DocumentDirectoryPath } from 'react-native-fs';
import {
  HorizonSdk,
  HorizonSdkView,
  CameraHelper,
  CameraMode,
  CameraFacing,
} from 'react-native-horizon-sdk';
import { VideoRecordingPermissions } from './config/permissions';

const targetDir = DocumentDirectoryPath;
const recordPath = targetDir + `/${getFileName()}.mp4`;

async function checkPermissions() {
  let statuses = await checkMultiple(VideoRecordingPermissions);

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
  const [permissionsGranted, setPermissionsGranted] = useState(false);

  useEffect(() => {
    const setup = async () => {
      try {
        let status = await checkPermissions();

        if (!status) {
          console.log('All permissions need to be granted');
          return;
        }

        setPermissionsGranted(true);

        status = await CameraHelper.hasCamera(CameraFacing.BACK);

        console.log(`hasCamera ${status}`);

        const list = await CameraHelper.getSupportedVideoSize(
          CameraFacing.BACK
        );

        console.log('getSupportedVideoSize:');
        console.log(list);

        const size = list
          .filter((item) => item.height > 720)
          .sort((item1, item2) => item1.height - item2.height)[0];

        console.log('VideoSize', JSON.stringify(size));

        setVideoSize(size);
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
  };

  const onTogglePreview = () => {
    setPreviewDisabled(!previewDisabled);
  };

  const onStartedRunning = () => {
    console.log('onStartedRunning');

    setTimeout(() => horizonViewRef.startRecording(recordPath), 100);
  };

  const onStoppedRunning = ({ nativeEvent }) => {
    console.log('onStoppedRunning');
    console.log(nativeEvent);

    if (nativeEvent.error) {
      console.log('error');
    }
  };

  const onRecordingStarted = () => {
    console.log('onRecordingStarted');

    setRecording(true);
  };

  const onRecordingFinished = async ({ nativeEvent }) => {
    console.log('onRecordingFinished');
    console.log(nativeEvent);

    setRecording(false);

    if (nativeEvent.error) {
      console.log('error');
    }

    const list = await RNFS.readDir(targetDir);
    list.forEach((item) => console.log(item.path, item.size));
  };

  return (
    <View style={styles.container}>
      <HorizonSdkView
        ref={setHorizonViewRef}
        permissionsGranted={permissionsGranted}
        style={styles.horizon}
        cameraMode={CameraMode.VIDEO}
        videoSize={videoSize}
        previewDisabled={previewDisabled}
        screenRotation={0}
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
