import React, { forwardRef } from 'react';
import type {Node} from 'react';
import {
	SafeAreaView,
	ScrollView,
	StatusBar,
	StyleSheet,
	Text,
	useColorScheme,
	View,
	TouchableNativeFeedback,
} from 'react-native';

import theme from "../../theme/theme";

const BLIconButton = forwardRef((props, ref) => {

	const {
		size = 100,
		onPress = () => {},
		style = {},
		color = theme.button.main,
	} = props;

	return (
		<View
			ref={ref}
			style={{
				backgroundColor: color,
				flexDirection: "row",
				alignItems: "center",
				justifyContent: "center",
				elevation: 5,
				borderRadius: 50,
				margin: 3,
				...style,
			}}
		>
			<TouchableNativeFeedback
				onPress={onPress}
				background={TouchableNativeFeedback.Ripple(theme.ripple.main, true, 300)}
			>
				<View
					style={{
						minWidth: size,
						minHeight: size,
						padding: 5,
					}}
				>
					{props.children}
				</View>
			</TouchableNativeFeedback>
		</View>
	);
});

export default BLIconButton;