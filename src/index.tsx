import { NativeModules } from 'react-native';
import { LINKING_ERROR } from './constants/error';

export const HorizonSdk = NativeModules.HorizonSdk
  ? NativeModules.HorizonSdk
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );
export const CameraHelper = NativeModules.CameraHelper
  ? NativeModules.CameraHelper
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
