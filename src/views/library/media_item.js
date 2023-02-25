/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, { useEffect, useState, Fragment } from 'react';
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
	Image,
} from 'react-native';

/* Icons */
import ADIcon from 'react-native-vector-icons/AntDesign';
import MCIcon from 'react-native-vector-icons/MaterialCommunityIcons';
import FAIcon from 'react-native-vector-icons/FontAwesome';
import SLIcon from 'react-native-vector-icons/SimpleLineIcons';

import { useHistory } from "react-router-native";

import theme from "../../theme/theme";

import { to_hh_mm_ss } from "../../utils/TimeCaster";

/* Components */
import BLButton from "../components/bl_button";

const MediaItem = (props) => {

	const { item, index, bucket_display_name, list } = props;

	const windowWidth = Dimensions.get('window').width;
	const windowHeight = Dimensions.get('window').height;

	const [image, set_image] = useState(null);


	useEffect(() => {
		const run_async = async () => {
			const img = await NativeModules.MediaScanner.get_media_img(item._data, item._id);
			if(img != null) {
				set_image(`file://${img}`);
			}
		};
		run_async();
	}, []);

	const on_press_play_tracks = async (item) => {
		try {
			let id_list = list.map(_item => String(_item._id));
			let uri_list = list.map(_item => String(_item._data));

			let title_list = list.map(_item => String(_item.title));
			let artist_list = list.map(_item => String(_item.artist));
			let duration_list = list.map(_item => String(_item.duration));

			const index = id_list.findIndex(_item => _item == item._id);
			id_list = [...id_list.splice(index, id_list.length - 1), ...id_list.splice(0, index)];
			uri_list = [...uri_list.splice(index, uri_list.length - 1), ...uri_list.splice(0, index)];
			title_list = [...title_list.splice(index, title_list.length - 1), ...title_list.splice(0, index)];
			artist_list = [...artist_list.splice(index, artist_list.length - 1), ...artist_list.splice(0, index)];
			duration_list = [...duration_list.splice(index, duration_list.length - 1), ...duration_list.splice(0, index)];
		} catch (ex) {
			console.log(ex);
		}
	};

	return (
		<Fragment>
			<BLButton
				onPress={e => on_press_play_tracks(item)}
				style={{
					borderRadius: 10,
					marginLeft: 5,
					marginRight: 5,
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
					<View>
						{image == null ? (
							<MCIcon
								name="music-circle"
								size={windowHeight / 12}
								color={theme.dark ? theme.font_color_dark : theme.font_color}
								style={{
									marginRight: 10,
									opacity: 0.6,
								}}
							/>
						) : (
							<Image
								source={{ uri: image }}
								style={{
									width: windowHeight / 12,
									height: windowHeight / 12,
									marginRight: 10,
									borderRadius: 5,
								}}
							/>
						)}
					</View>
					<View
						style={{
							flexDirection: "column",
							alignItems: "flex-start",
							justifyContent: "center",
							flexWrap: "nowrap",
							flexShrink: 1,

						}}
					>
						<Text
							style={{
								color: theme.font.main,
								fontWeight: "bold",
								fontSize: 15,
							}}
						>
							{item.title.length > 50 ? item.title.substring(0, 50) + "..." : item.title}
						</Text>
						<Text
							style={{
								color: theme.font.main,
								fontWeight: "bold",
								fontSize: 13,
								flexWrap: "wrap",
								flexShrink: 1,
							}}
						>
							{item.artist.length > 25 ? item.artist.substring(0, 25) + "..." : item.artist}
						</Text>
					</View>
					<Text
						style={{
							color: theme.font.main,
							fontWeight: "normal",
							fontSize: 13,
							flexWrap: "wrap",
							flexShrink: 1,
							backgroundColor: theme.background.main,
							position: "absolute",
							right: -5,
							padding: 4,
							bottom: -5,
							minWidth: 40,
							flexDirection: "row",
							alignItems: "center",
							justifyContent: "center",
							textAlign: "center",
							borderRadius: 20,
							borderWidth: 0.5,
							elevation: 5,
						}}
					>
						{to_hh_mm_ss(item.duration)}
					</Text>
				</View>
			</BLButton>
		</Fragment>
	);
};

export default MediaItem;