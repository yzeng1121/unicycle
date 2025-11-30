import React from 'react';
import { StyleSheet, View, Platform } from 'react-native';
import { BlurView } from 'expo-blur';

export default function TabBarBackground() {
  if (Platform.OS === 'ios') {
    // iOS: Use BlurView for native blur effect
    return (
      <BlurView
        intensity={80}
        tint="light"
        style={StyleSheet.absoluteFill}
      />
    );
  }

  // Android/other platforms: Use semi-transparent background
  return (
    <View style={[StyleSheet.absoluteFill, styles.androidBackground]} />
  );
}

const styles = StyleSheet.create({
  androidBackground: {
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
  },
});