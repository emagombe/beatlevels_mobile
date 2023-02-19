import { MMKV } from 'react-native-mmkv';

const storage = new MMKV({
	id: "beatlevels1",
	encryptionKey: process.env.ENCRYPTION_KEY,
});

export default storage;