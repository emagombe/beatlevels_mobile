/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, { Fragment, useEffect } from 'react';
import type { PropsWithChildren } from 'react';
import {
	ScrollView,
	StatusBar,
	StyleSheet,
	Text,
	useColorScheme,
	View,
	BackHandler,
} from 'react-native';

import { NativeRouter, Route, Link, useHistory, Switch } from "react-router-native";

import Library from "./library/library";
import Folders from "./library/folders";

const Root = (props) => {

	const isDarkMode = useColorScheme() === 'dark';
	const history = useHistory();

	useEffect(() => {
		const back_handler = BackHandler.addEventListener("hardwareBackPress", () => {
			if(history.index == 0) {
				BackHandler.exitApp();
				return false;
			}
			history.goBack();
			return true;
		});
		return () => back_handler.remove();
	}, []);

	return (
		<Fragment>
			<Switch>
				<Route exact path="/">
					<Library/>
				</Route>
				<Route exact path="/library/all_folders">
					<Folders/>
				</Route>
				<Route>
					<Text>Nothing</Text>
				</Route>
			</Switch>
		</Fragment>
	);
}
export default Root;
