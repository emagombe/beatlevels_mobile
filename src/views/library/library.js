/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React from 'react';
import { useEffect, useState, } from 'react';
import {
	ScrollView,
	StatusBar,
	StyleSheet,
	Text,
	useColorScheme,
	View,
	Dimensions,
	ActivityIndicator,
	NativeModules,
} from 'react-native';

import { useHistory, } from "react-router-native";

/* Icons */
import ADIcon from 'react-native-vector-icons/AntDesign';
import MCIcon from 'react-native-vector-icons/MaterialCommunityIcons';
import MIcon from 'react-native-vector-icons/MaterialIcons';
import FAIcon from 'react-native-vector-icons/FontAwesome';
import SLIcon from 'react-native-vector-icons/SimpleLineIcons';

/* Components */
import BLButton from "../components/bl_button";
import BLIconButton from "../components/bl_icon_button";

/* Classes */
import { get_folders } from "../../classes/Media";

import Folders from "./folders";

import theme from "../../theme/theme";

const Library = (props) => {

	const [media_loading, set_media_loading] = useState(false);
	const [folders_count, set_folders_count] = useState(0);
	const [playlist_count, set_playlist_count] = useState(0);
	const [albums_count, set_albums_count] = useState(0);

	const windowWidth = Dimensions.get("window").width;
	const windowHeight = Dimensions.get("window").height;

	const isDarkMode = useColorScheme() === 'dark';
	const history = useHistory();

	const top_icon_button_size = 25;

	useEffect(() => {

	}, []);

	const refresh_media_files = async () => {
		try {
			set_media_loading(true);
			if(NativeModules.MediaScanner != null) {
				const refresh_media_files = await NativeModules.MediaScanner.refresh_media_files("/");
				const media_files = await NativeModules.MediaScanner.find_media();
				const folders = get_folders(media_files.media_files);
				set_folders_count(folders.length);
			}
			setTimeout(() => {
				set_media_loading(false);
			}, 1000);
		} catch (ex) {
			console.log(ex);
			setTimeout(() => {
				set_media_loading(false);
			}, 1000);
		}
	};

	const on_press_refresh = () => {
		try {
			refresh_media_files();
		} catch (ex) {
			console.log(ex);
		}
	};

	const on_press_all_folders = () => {
		history.push("/library/all_folders");
	};

	return (
		<View
			style={{
				flex: 1,
				backgroundColor: theme.background.main,
			}}
		>

			<View
				style={{
					position: "absolute",
					top: 0,
					left: 0,
					right: 0,
					padding: 5,
					flexDirection: "row",
					alignItems: "center",
					justifyContent: "space-between",
					zIndex: 2,
				}}
			>
				<View></View>
				<View
					style={{
						flexDirection: "row",
						alignItems: "center",
						justifyContent: "flex-end",
					}}
				>
					<BLIconButton
						size={top_icon_button_size}
						onPress={on_press_refresh}
						disabled={media_loading}
					>
						{media_loading ? (
							<ActivityIndicator size="small" color={theme.font.main} />
						) : (
							<MCIcon name="refresh" size={top_icon_button_size} color={theme.font.main} />
						)}
					</BLIconButton>
					<BLIconButton
						size={top_icon_button_size}
					>
						<MIcon name="settings" size={top_icon_button_size} color={theme.font.main} />
					</BLIconButton>
				</View>
			</View>
			<View
				style={{
					backgroundColor: "black",
					height: "100%",
				}}
			>
				<View
					style={{ ...styles.library_title_container }}
				>
					<Text
						style={styles.title}
					>Library</Text>
				</View>
				<View style={styles.library_container}>
					<BLButton
						style={{ ...styles.library_container_item }}
						color={"transparent"}
						onPress={on_press_all_folders}
					>
						<View
							style={{
								flexDirection: "row",
								alignItems: "center",
								justifyContent: "space-between",
								width: '100%',
							}}
						>
							<View
								style={{
									flexDirection: "row",
									alignItems: "center",
									justifyContent: "flex-start",
								}}
							>
								<MCIcon
									name="folder"
									size={45}
									color={theme.primary}
									style={{
										marginRight: 10,
										opacity: 0.6,
									}}
								/>
								<Text style={styles.library_container_item_text}>
									All folders
								</Text>
							</View>
							<Text
								style={{
									color: theme.font.main,
									fontSize: 18,
									fontWeight: "bold",
								}}
							>
								{folders_count}
							</Text>
						</View>
					</BLButton>
					<BLButton
						style={{ ...styles.library_container_item }}
						color={"transparent"}
					>
						<View
							style={{
								flexDirection: "row",
								alignItems: "center",
								justifyContent: "space-between",
								width: '100%',
							}}
						>
							<View
								style={{
									flexDirection: "row",
									alignItems: "center",
									justifyContent: "flex-start",
								}}
							>
								<MCIcon
									name="playlist-music-outline"
									size={45}
									color={theme.primary}
									style={{
										marginRight: 10,
										opacity: 0.6,
									}}
								/>
								<Text style={styles.library_container_item_text}>
									Playlists
								</Text>
							</View>
							<Text
								style={{
									color: theme.font.main,
									fontSize: 18,
									fontWeight: "bold",
								}}
							>
								{playlist_count}
							</Text>
						</View>
					</BLButton>
					<BLButton
						style={{ ...styles.library_container_item }}
						color={"transparent"}
					>
						<View
							style={{
								flexDirection: "row",
								alignItems: "center",
								justifyContent: "space-between",
								width: '100%',
							}}
						>
							<View
								style={{
									flexDirection: "row",
									alignItems: "center",
									justifyContent: "flex-start",
								}}
							>
								<MCIcon
									name="album"
									size={45}
									color={theme.primary}
									style={{
										marginRight: 10,
										opacity: 0.6,
									}}
								/>
								<Text style={styles.library_container_item_text}>
									Albums
								</Text>
							</View>
							<Text
								style={{
									color: theme.font.main,
									fontSize: 18,
									fontWeight: "bold",
								}}
							>
								{albums_count}
							</Text>
						</View>
					</BLButton>
				</View>
			</View>
		</View>
	);
}
export default Library;

const styles = StyleSheet.create({
	library_title_container: {
		margin: 10,
		marginTop: 70,
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
		fontSize: 18,
		fontWeight: 'bold',
		color: theme.font.main,
	},
});