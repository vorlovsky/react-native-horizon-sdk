import { AppRegistry } from 'react-native';
import App from './src/App';
import { HorizonSdk } from 'react-native-horizon-sdk';

const API_KEY =
  'm8W7J+sSoGogrwibc/TCXX/M8h9XOFozFIQ1r03zhOmpOK9Bcfde8jUrEXthYmPfHn434/WNyjfF82i0Nv5cCmNvbS5odnQucGV0cmFrZWFzLnNpbXBsZWFwcHxOT3xhbmRyb2lk';

HorizonSdk.init(API_KEY);

AppRegistry.registerComponent('main', () => App);
