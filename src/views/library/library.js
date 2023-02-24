/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React from 'react';
import type { PropsWithChildren } from 'react';
import {
	ScrollView,
	StatusBar,
	StyleSheet,
	Text,
	useColorScheme,
	View,
	Dimensions,
} from 'react-native';

/* Icons */
import ADIcon from 'react-native-vector-icons/AntDesign';
import MCIcon from 'react-native-vector-icons/MaterialCommunityIcons';
import FAIcon from 'react-native-vector-icons/FontAwesome';
import SLIcon from 'react-native-vector-icons/SimpleLineIcons';

/* Components */
import BLButton from "../components/bl_button";
import BLIconButton from "../components/bl_icon_button";

import Folders from "./folders";

import theme from "../../theme/theme";

const Library = () => {

	const windowWidth = Dimensions.get("window").width;
	const windowHeight = Dimensions.get("window").height;

	React.useEffect(() => {

	}, []);

	const isDarkMode = useColorScheme() === 'dark';

	return (
		<View
			style={{
				flex: 1,
				backgroundColor: theme.background.main,
			}}
		>
			<View
				style={{ ...styles.library_title_container, marginTop: windowHeight / 5}}
			>
				<Text
					style={styles.title}
				>Library</Text>
			</View>
			<View style={styles.library_container}>
				<BLButton style={styles.library_container_item}>
					<View
						style={{
							flexDirection: "row",
							alignItems: "center",
							justifyContent: "space-between",
							width: '100%',
						}}
					>
						<Text style={styles.library_container_item_text}>
							All folders
						</Text>
						<MCIcon
							name="folder"
							size={30}
							color={theme.primary}
							style={{
								marginRight: 10,
								opacity: 0.6,
							}}
						/>
					</View>
				</BLButton>
				<BLButton style={styles.library_container_item}>
					<View
						style={{
							flexDirection: "row",
							alignItems: "center",
							justifyContent: "space-between",
							width: '100%',
						}}
					>
						<Text style={styles.library_container_item_text}>
							Playlists
						</Text>
						<MCIcon
							name="playlist-music-outline"
							size={30}
							color={theme.primary}
							style={{
								marginRight: 10,
								opacity: 0.6,
							}}
						/>
					</View>
				</BLButton>
			</View>
		</View>
	);
}
export default Library;

const styles = StyleSheet.create({
	library_title_container: {
		margin: 10,
		marginTop: 50,
	},
	title: {
		fontSize: 30,
		fontWeight: 'bold',
		color: theme.font.main,
	},
	library_container: {
		marginTop: 30,
	},
	library_container_item: {
		margin: 10,
		marginTop: 1,
		marginBottom: 1,

	},
	library_container_item_text: {
		fontSize: 14,
		fontWeight: 'bold',
		color: theme.font.main,
	},
});