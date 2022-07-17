import { NativeModules } from 'react-native';
import { LINKING_ERROR } from './constants';

export const CAMERA_FACING_BACK = 0;
export const CAMERA_FACING_FRONT = 1;

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

export { HorizonSdkView } from './components/HorizonSdkView';
