import { NativeModules } from 'react-native';
import { LINKING_ERROR } from './constants/error';

export const HorizonSdk = NativeModules.HorizonSdkModule
  ? NativeModules.HorizonSdkModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );
export const CameraHelper = NativeModules.CameraHelperModule
  ? NativeModules.CameraHelperModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export { CameraFacing, CameraMode, ScreenRotation } from './constants';
export { HorizonSdkView } from './components/HorizonSdkView';
