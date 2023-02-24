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

import Library from "./library/library";

const Root = () => {

	React.useEffect(() => {

	}, []);

	const isDarkMode = useColorScheme() === 'dark';

	return (
		<Routes>
			<Route exact path="/" element={<Library />} />
			<Route exact path="/library" element={<Library />} />
		</Routes>
	);
}
export default Root;
