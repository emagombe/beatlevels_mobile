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


const BLIconButton = forwardRef((props, ref) => {

	const theme = useSelector(state => state.theme);

	const {
		size = 100,
		onPress = () => {},
		style = {},
		color = theme.dark ? theme.button_dark : theme.button,
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
				background={TouchableNativeFeedback.Ripple(theme.dark ? theme.ripple_color_dark : theme.ripple_color, true)}
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