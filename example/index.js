import { AppRegistry } from 'react-native';
import App from './src/App';
import { API_KEY } from './src/config/key';
import { HorizonSdk } from 'react-native-horizon-sdk';

HorizonSdk.init(API_KEY);

AppRegistry.registerComponent('main', () => App);
