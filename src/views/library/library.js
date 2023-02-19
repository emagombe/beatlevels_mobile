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
} from 'react-native';

/* Components */
import BLButton from "../components/bl_button";

import Folders from "./folders";

const Library = () => {

	React.useEffect(() => {

	}, []);

	const isDarkMode = useColorScheme() === 'dark';

	return (
		<View
			style={{
				flex: 1,
			}}
		>
			<View
				style={{
					position: "absolute",
					zIndex: 5,
				}}
			>
				<View
					style={{

					}}
				>
					<BLButton
						
					>

					</BLButton>
				</View>
			</View>
			<Folders />
		</View>
	);
}
export default Library;
