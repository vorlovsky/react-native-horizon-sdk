import React, { useState, useEffect } from 'react';

import { StyleSheet, View, Button } from 'react-native';
import { DownloadDirectoryPath } from 'react-native-fs';
import { HorizonSdk, HorizonSdkView } from 'react-native-horizon-sdk';

const API_KEY =
  'm8W7J+sSoGogrwibc/TCXX/M8h9XOFozFIQ1r03zhOmpOK9Bcfde8jUrEXthYmPfHn434/WNyjfF82i0Nv5cCmNvbS5odnQucGV0cmFrZWFzLnNpbXBsZWFwcHxOT3xhbmRyb2lk';

export default function App() {
  const [recording, setRecording] = useState(false);
  const [horizonViewRef, setHorizonViewRef] = useState(null);

  useEffect(() => {
    HorizonSdk.init(API_KEY);
  }, []);

  const onRecStop = () => {
    if (recording) {
      horizonViewRef.stopRecording();
    } else {
      horizonViewRef.startRecording(DownloadDirectoryPath + '/rec.mp4');
    }

    setRecording(!recording);
  };

  return (
    <View style={styles.container}>
      <HorizonSdkView ref={setHorizonViewRef} style={styles.horizon} />
      <View style={styles.recStop}>
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
    height: 40,
  },
});
