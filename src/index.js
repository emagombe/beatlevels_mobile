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

import Library from "./views/library/library";

const Index = () => {

	React.useEffect(() => {

	}, []);

	const isDarkMode = useColorScheme() === 'dark';

	return (
		<View
			style={{
				flex: 1,
			}}
		>
			<Library />
		</View>
	);
}
export default Index;
