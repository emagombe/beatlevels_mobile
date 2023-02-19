
import { PermissionsAndroid } from "react-native";
import { BackHandler } from "react-native";

export const read_external_storage_permission = async () => {
	const granted = await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.READ_EXTERNAL_STORAGE, {
		title: "Permission to read your storage",
		message: "We need permission to read your storage to be able to locate to music",
		buttonNeutral: "Ask me later",
		buttonNegative: "Deny",
		buttonPositive: "Allow",
	});

	if (granted !== PermissionsAndroid.RESULTS.GRANTED) {
		BackHandler.exitApp();
	}
	return granted;
};