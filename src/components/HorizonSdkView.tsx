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
import { checkMultiple, PERMISSIONS, RESULTS } from 'react-native-permissions';
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

  componentDidMount() {
    this.checkPermissions();
  }

  componentDidUpdate(prevProps: HorizonSdkViewProps) {
    this.checkPermissions();

    const { cameraFacing, videoSize, photoSize } = this.props;

    if (
      prevProps.cameraFacing !== cameraFacing ||
      prevProps.videoSize !== videoSize ||
      prevProps.photoSize !== photoSize
    ) {
      this.updateCameraSetup();
    }
  }

  async checkPermissions() {
    let statuses: object = await checkMultiple([
      PERMISSIONS.ANDROID.CAMERA,
      PERMISSIONS.ANDROID.RECORD_AUDIO,
    ]);

    if (Object.values(statuses).some((item) => item !== RESULTS.GRANTED)) {
      console.log('All permissions need to be granted');

      return;
    }

    this.setState({ permissionsGranted: true });
  }

  public startRunning() {
    callViewMethod(this.viewRef, 'startRunning');
  }

  public stopRunning() {
    callViewMethod(this.viewRef, 'stopRunning');
  }

  public startRecording(filePath: string) {
    callViewMethod(this.viewRef, 'startRecording', filePath);
  }

  public stopRecording() {
    callViewMethod(this.viewRef, 'stopRecording');
  }

  private updateCameraSetup() {
    const { cameraFacing, videoSize, photoSize } = this.props;

    callViewMethod(
      this.viewRef,
      'setCamera',
      cameraFacing,
      normalizeSize(videoSize),
      normalizeSize(photoSize)
    );
  }

  private onRef = (ref: ComponentClass<any>) => {
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
