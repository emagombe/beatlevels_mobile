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

import { NativeRouter, Route, Link, Routes } from "react-router-native";

import Library from "./views/library/library";

import Root from "./views/root";

const Index = () => {

	React.useEffect(() => {

	}, []);

	const isDarkMode = useColorScheme() === 'dark';

	return (
		<NativeRouter>
			<Routes>
				<Route exact path="/*" element={<Root />} />
			</Routes>
		</NativeRouter>
	);
}
export default Index;
