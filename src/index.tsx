import { NativeModules } from 'react-native';
import { LINKING_ERROR } from './constants';

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

export { HorizonSdkView } from './components/HorizonSdkView';
