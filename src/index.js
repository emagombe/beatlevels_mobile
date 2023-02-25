/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, { useState, useEffect } from 'react';
import type { PropsWithChildren } from 'react';
import {
	ScrollView,
	StatusBar,
	StyleSheet,
	Text,
	useColorScheme,
	View,
	BackHandler
} from 'react-native';

import { NativeRouter, Route, Link, useNavigate } from "react-router-native";

import Library from "./views/library/library";

import Root from "./views/root";

const Index = () => {

	const isDarkMode = useColorScheme() === 'dark';

	useEffect(() => {

	}, []);

	return (
		<NativeRouter>
			<Route exact path="/*" component={Root} />
		</NativeRouter>
	);
}
export default Index;
