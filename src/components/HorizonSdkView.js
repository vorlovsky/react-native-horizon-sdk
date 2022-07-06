import React from 'react';
import {
  requireNativeComponent,
  UIManager,
  Platform,
  ViewStyle,
  NativeModules,
  findNodeHandle,
  View,
} from 'react-native';
import {
  checkMultiple,
  requestMultiple,
  PERMISSIONS,
  RESULTS,
  Permission,
} from 'react-native-permissions';
import { LINKING_ERROR } from '../constants';

const ComponentName = 'HorizonSdkView';

const HorizonSdkViewNative =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };

export class HorizonSdkView extends React.PureComponent {
  viewRef = React.createRef();

  state = {
    permissionsGranted: false,
  };

  async componentDidMount() {
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

      if (!Object.values(statuses).every((item) => item === RESULTS.GRANTED)) {
        console.log('All permissions need to be granted');

        return;
      }
    }

    this.setState({ permissionsGranted: true });
  }

  startRecording(filePath) {
    callViewMethod(this.viewRef, 'startRecording', filePath);
  }

  stopRecording() {
    callViewMethod(this.viewRef, 'stopRecording');
  }

  _onRecordingStarted = () => {
    console.log('onRecordingStarted');

    const { onRecordingStarted } = this.props;

    !!onRecordingStarted && onRecordingStarted();
  };

  _onRecordingFinished = (event) => {
    console.log('onRecordingFinished');
    console.log(event.nativeEvent);

    const { onRecordingFinished } = this.props;

    !!onRecordingFinished && onRecordingFinished(event);
  };

  render() {
    const { permissionsGranted } = this.state;

    return permissionsGranted ? (
      <HorizonSdkViewNative
        {...this.props}
        ref={this.viewRef}
        onRecordingStarted={this._onRecordingStarted}
        onRecordingFinished={this._onRecordingFinished}
      />
    ) : (
      <View {...this.props} />
    );
  }
}

function callViewMethod(viewRef, methodName, ...params) {
  if (viewRef.current == null) {
    return;
  }

  if (Platform.OS === 'ios') {
    NativeModules.HorizonSdkViewNative[methodName].apply(
      NativeModules.HorizonSdkViewNative,
      [findNodeHandle(viewRef.current), ...params]
    );
  } else if (Platform.OS === 'android') {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(viewRef.current),
      methodName,
      params
    );
  }
}
