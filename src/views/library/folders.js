/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, { useEffect } from 'react';
import type { PropsWithChildren } from 'react';
import {
	SafeAreaView,
	ScrollView,
	StatusBar,
	StyleSheet,
	Text,
	useColorScheme,
	View,
	NativeModules,
	Dimensions,
	FlatList,
} from 'react-native';

/* Icons */
import ADIcon from 'react-native-vector-icons/AntDesign';
import MCIcon from 'react-native-vector-icons/MaterialCommunityIcons';
import FAIcon from 'react-native-vector-icons/FontAwesome';
import SLIcon from 'react-native-vector-icons/SimpleLineIcons';

import { useHistory } from "react-router-native";

import theme from "../../theme/theme";

/* Components */
import BLButton from "../components/bl_button";

import { read_external_storage_permission } from "../../utils/permissions";

const Folders = () => {

	const history = useHistory();

	const isDarkMode = useColorScheme() === 'dark';
	const windowWidth = Dimensions.get('window').width;
	const windowHeight = Dimensions.get('window').height;

	const [folders, set_folders] = React.useState([]);

	const get_folders = (media_list = []) => {
		const _folders = media_list.reduce((acc, item, index) => {
			const folder_path = item._data.split("/" + item._display_name)[0];
			const folder = folder_path.split("/")[folder_path.split("/").length - 1];
			if(!acc.some(_item => _item._data.split("/" + _item._display_name)[0] === folder_path)) {
				if(typeof item.bucket_display_name === "undefined") {
					acc.push({ ...item, bucket_display_name: folder, folder_path });
				} else {
					acc.push({ ...item, folder_path });
				}
			}
			return acc;
		}, []);
		_folders.sort((a, b) => a.bucket_display_name > b.bucket_display_name ? 1 : -1);
		return _folders;
	};

	useEffect(() => {
		const run_async = async () => {
			/* Request read files permissions first */
			read_external_storage_permission().then(async () => {
				/* Scanning media on storage */
				const media_files = await NativeModules.MediaScanner.find_media();
				const _folders = get_folders(media_files.media_files);
				//console.log("_folders: ", _folders);
				set_folders(_folders);
			});
		};
		run_async();
	}, []);

	const render_item = ({ item, index, separators }) => {

		return (
			<>
				<BLButton
					onPress={() => {
						history.push("/library/folder", {
							data: {
								bucket_display_name: item.bucket_display_name,
								folder_path: item.folder_path,
							}
						});
					}}
					style={{
						borderRadius: 10,
						marginLeft: 5,
						marginRight: 0,
					}}
				>
					<View
						style={{
							width: "100%",
							height: windowHeight / 8,
							display: "flex",
							flexDirection: "row",
							alignItems: "center",
							justifyContent: "flex-start",
						}}
					>
						<MCIcon
							name="folder"
							size={windowHeight / 12}
							color={theme.primary}
							style={{
								marginRight: 10,
								opacity: 0.6,
							}}
						/>
						<View
							style={{
								flexShrink: 1,
							}}
						>
							<Text
								style={{
									color: theme.font.main,
									fontWeight: "bold",
									fontSize: 16,
								}}
								nativeID={`bucket_display_name_${item._id}`}
							>
								{item.bucket_display_name}
							</Text>
							<Text
								style={{
									color: theme.font.main,
									fontWeight: "normal",
									fontSize: 13,
								}}
							>
								{item.folder_path.length > 50 ? item.folder_path.substring(0, 50) + "..." : item.folder_path}
							</Text>
						</View>
					</View>
				</BLButton>
			</>
		);
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
					backgroundColor: theme.background.content.main,
					flex: 1,
				}}
			>
				<Text
					style={{
						color: theme.font.main,
						fontWeight: "bold",
						fontSize: 16,
						flexWrap: "wrap",
						flexShrink: 1,
						backgroundColor: theme.button.main,
						textAlign: "center",
						borderRadius: 10,
						elevation: 10,
						alignSelf: "flex-start",
						marginLeft: 5,
						padding: 5,
						paddingRight: 10,
						paddingLeft: 10,
						position: "absolute",
						zIndex: 2,
						top: windowHeight / 6 - 30,
					}}
				>
					All folders
				</Text>
				<FlatList
					showsHorizontalScrollIndicator={false}
					showsVerticalScrollIndicator={false}
					horizontal={false}
					keyExtractor={(item, index) => item._id}
					data={folders}
					renderItem={render_item}
					overScrollMode="always"
					style={{
						marginTop: windowHeight / 6,
					}}
					contentContainerStyle={{
						paddingBottom: windowHeight / 5,
						paddingTop: 10,
						paddingRight: 5,
						justifyContent: "flex-start",
						alignItems: "center",
						borderTopLeftRadius: 20,
						borderTopRightRadius: 20,
					}}
				/>
			</View>
		</View>
	);
}
export default Folders;
