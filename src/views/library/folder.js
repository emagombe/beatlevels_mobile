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
import MediaItem from "./media_item";

/* Components */
import BLButton from "../components/bl_button";

import { read_external_storage_permission } from "../../utils/permissions";

const Folder = (props) => {

	const history = useHistory();

	const isDarkMode = useColorScheme() === 'dark';
	const windowWidth = Dimensions.get('window').width;
	const windowHeight = Dimensions.get('window').height;

	const { bucket_display_name = "", folder_path = "" } = history.location.state.data;

	const [folders, set_folders] = React.useState([]);
	const [media_files, set_media_files] = React.useState([]);

	const get_folder_content = (media_list = []) => {
		const _media = media_list.filter(item => {
			const _folder_path = item._data.split("/" + item._display_name)[0];
			//const folder = _folder_path.split("/")[_folder_path.split("/").length - 1];
			return _folder_path === folder_path;
		}).map(item => {
			let _bucket_display_name;
			if(typeof item.bucket_display_name === "undefined") {
				const _folder_path = item._data.split("/" + item._display_name)[0];
				const folder = _folder_path.split("/")[_folder_path.split("/").length - 1];
				_bucket_display_name = folder;
			} else {
				_bucket_display_name = item.bucket_display_name;
			}
			return {
				...item,
				bucket_display_name: _bucket_display_name,
			};
		});
		_media.sort((a, b) => a.bucket_display_name > b.bucket_display_name ? 1 : -1);
		return _media;
	};

	useEffect(() => {
		const run_async = async () => {
			const media_files = await NativeModules.MediaScanner.find_media();
			const folder_content = get_folder_content(media_files.media_files);
			set_media_files(folder_content);
		};
		run_async();
	}, []);

	const render_item = ({ item, index, separators }) => {

		return (
			<>
				<MediaItem
					item={item}
					index={index}
					bucket_display_name={bucket_display_name}
					list={media_files}
				/>
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
				{bucket_display_name}
			</Text>
			<FlatList
				showsHorizontalScrollIndicator={false}
				showsVerticalScrollIndicator={false}
				horizontal={false}
				keyExtractor={(item, index) => item._id}
				data={media_files}
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
	);
}
export default Folder;
