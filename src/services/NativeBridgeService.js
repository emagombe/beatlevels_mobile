import {
	NativeModules,
	NativeEventEmitter,
	DeviceEventEmitter,
	BackHandler,
	Linking,
} from 'react-native';

import mmkv from "../storage/mmkv";

module.exports = async (data) => {

	if(data.event === "ON_PLAYING_STATE_CHANGE") {
		const { is_playing = false } = data;

		global.PLAYER_IS_PLAYING = is_playing;
	}
	if(data.event === "CURRENT_POSITION") {
		const { id = null, position = 0, duration = 0 } = data;

		global.PLAYER_DURATION = duration;
		global.PLAYER_POSITION = position;
		global.PLAYER_ID = id;
		
		/* Saving progress state */
		mmkv.set("PLAYER_DURATION", String(duration));
		mmkv.set("PLAYER_POSITION", String(position));
		mmkv.set("PLAYER_ID", id);
	}
	if(data.event === "ON_MEDIA_CHANGE") {
		const { id = null, position = 0, duration = 0, reason = 1 } = data;

		global.PLAYER_DURATION = duration;
		global.PLAYER_POSITION = position;
		global.PLAYER_ID = id;

		/* Saving progress state */
		mmkv.set("PLAYER_DURATION", String(duration));
		mmkv.set("PLAYER_POSITION", String(position));
		mmkv.set("PLAYER_ID", id);
	}
};