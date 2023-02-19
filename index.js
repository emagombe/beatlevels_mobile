/**
 * @format
 */

import { AppRegistry } from 'react-native';
import Index from './src/index';
import { name as appName } from './app.json';

AppRegistry.registerComponent(appName, () => Index);
AppRegistry.registerHeadlessTask("NativeBridgeService", () => require("./src/services/NativeBridgeService.js"));