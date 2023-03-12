/**
 * @format
 */

import { AppRegistry } from 'react-native';
import Index from './src/index';
import { name as appName } from './app.json';

global.PLAYER_IS_PLAYING = false;
global.PLAYER_DURATION = 0;
global.PLAYER_POSITION = 0;
global.PLAYER_ID = null;

AppRegistry.registerComponent(appName, () => Index);
AppRegistry.registerHeadlessTask("NativeBridgeService", () => require("./src/services/NativeBridgeService.js"));