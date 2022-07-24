import React, { ComponentClass } from 'react';
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
  Permission,
  PermissionStatus,
  checkMultiple,
  requestMultiple,
  PERMISSIONS,
  RESULTS,
} from 'react-native-permissions';
import { CameraFacing, ScreenRotation } from '../constants';
import { LINKING_ERROR } from '../constants/error';

type HorizonSdkViewProps = typeof HorizonSdkView.defaultProps & {
  cameraFacing?: number;
  videoSize?: { width: number; height: number } | null;
  photoSize?: { width: number; height: number } | null;
  cameraMode?: string;
  previewDisabled?: boolean;
  tapToFocus?: boolean;
  screenRotation?: number;
  style?: ViewStyle;
  //ref?: (ref: ComponentClass<any>) => void | ComponentClass<any> | null;
};

type SpecialProps = {
  ref?: (ref: ComponentClass<any>) => void | ComponentClass<any> | null;
};

type HorizonSdkViewState = {
  permissionsGranted: boolean;
};

const ComponentName = 'HorizonSdkView';

const HorizonSdkViewNative =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<HorizonSdkViewProps & SpecialProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };

export class HorizonSdkView extends React.PureComponent<
  HorizonSdkViewProps,
  HorizonSdkViewState
> {
  static defaultProps = {
    cameraFacing: CameraFacing.BACK,
    cameraMode: 'AUTO',
    screenRotation: ScreenRotation.ROTATION_0,
    previewDisabled: false,
    tapToFocus: false,
  };

  private viewRef: ComponentClass<any> | null = null;

  public state: HorizonSdkViewState = {
    permissionsGranted: false,
  };

  async componentDidMount() {
    let statuses: object = await checkMultiple([
      PERMISSIONS.ANDROID.CAMERA,
      PERMISSIONS.ANDROID.RECORD_AUDIO,
      PERMISSIONS.ANDROID.WRITE_EXTERNAL_STORAGE,
    ]);

    const permissionsToRequest: Permission[] = Object.entries(statuses).reduce(
      (accumulator: Permission[], item: [string, PermissionStatus]) => {
        if (item[1] !== RESULTS.GRANTED) {
          accumulator.push(item[0] as Permission);
        }

        return accumulator;
      },
      [] as Permission[]
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

  componentDidUpdate(prevProps: HorizonSdkViewProps) {
    const { cameraFacing, videoSize, photoSize } = this.props;

    if (
      prevProps.cameraFacing !== cameraFacing ||
      prevProps.videoSize !== videoSize ||
      prevProps.photoSize !== photoSize
    ) {
      this.updateCameraSetup();
    }
  }

  startRunning() {
    callViewMethod(this.viewRef, 'startRunning');
  }

  stopRunning() {
    callViewMethod(this.viewRef, 'stopRunning');
  }

  startRecording(filePath: string) {
    callViewMethod(this.viewRef, 'startRecording', filePath);
  }

  stopRecording() {
    callViewMethod(this.viewRef, 'stopRecording');
  }

  updateCameraSetup() {
    const { cameraFacing, videoSize, photoSize } = this.props;

    callViewMethod(
      this.viewRef,
      'setCamera',
      cameraFacing,
      normalizeSize(videoSize),
      normalizeSize(photoSize)
    );
  }

  onRef = (ref: ComponentClass<any>) => {
    this.viewRef = ref;

    const { cameraFacing, videoSize, photoSize } = this.props;

    if (cameraFacing != null || videoSize != null || photoSize != null) {
      this.updateCameraSetup();
    }

    this.startRunning();
  };

  render() {
    const { permissionsGranted } = this.state;

    return permissionsGranted ? (
      <HorizonSdkViewNative {...this.props} ref={this.onRef} />
    ) : (
      <View {...this.props} />
    );
  }
}

function callViewMethod(
  viewRef: ComponentClass<any> | null,
  methodName: string,
  ...params: any[]
) {
  if (viewRef == null) {
    return;
  }

  if (Platform.OS === 'ios') {
    NativeModules.HorizonSdkViewNative[methodName].apply(
      NativeModules.HorizonSdkViewNative,
      [findNodeHandle(viewRef), ...params]
    );
  } else if (Platform.OS === 'android') {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(viewRef),
      methodName,
      params
    );
  }
}

function normalizeSize(
  size: { width: number; height: number } | null | undefined
) {
  if (size == null) {
    return null;
  }

  if (!!size.width && !!size.height) {
    return [size.width, size.height];
  }

  return null;
}
