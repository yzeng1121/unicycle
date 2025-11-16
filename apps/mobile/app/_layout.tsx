import { DarkTheme, DefaultTheme, ThemeProvider } from '@react-navigation/native';
import { useFonts } from 'expo-font';
import { Stack, router } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import 'react-native-reanimated';
import React from 'react';
import { useEffect } from 'react';
import { ActivityIndicator, View, Text, StyleSheet } from 'react-native';

import { useColorScheme } from '@/hooks/useColorScheme';
import { AuthProvider, useAuth } from './navigation/contexts/auth-context';


// Loading screen component
// TODO: make loading screen cuter... consult Meggy(?)
// TODO: why tf is the first screen the index screen
// TODO: when creating fresh app start ('i' cmd), navigation defaults to auth flow??

const LoadingScreen: React.FC = () => (
  <View style={styles.loadingContainer}>
    <ActivityIndicator size="large" color="#007AFF" />
    <Text style={styles.loadingText}>Loading...</Text>
  </View>
);

// Navigation component with proper TypeScript typing
const AppNavigation: React.FC = () => {
  const { isAuthenticated, isLoading } = useAuth();

  // TODO: remove later used only for offline testing 
  // const { isLoading } = useAuth();
  // let isAuthenticated = true;

  console.log('🧭 === NAVIGATION RENDER ===');
  console.log('🧭 isLoading:', isLoading);
  console.log('🧭 isAuthenticated:', isAuthenticated);

  useEffect(() => {
    if (!isLoading) {
      console.log('🚀 Auth check complete, navigating...');
      if (isAuthenticated) {
        console.log('🎯 Navigating to tabs');
        router.replace('/screens/(tabs)');
      } else {
        console.log('🎯 Navigating to register');
        router.replace('/navigation/(auth)/register');
      }
    }
  }, [isAuthenticated, isLoading]);
  
  // Show loading screen while checking authentication
  if (isLoading) {
    return <LoadingScreen />;
  }

  return (
    <Stack>
      {/* Always define all screens, control access via navigation */}
      <Stack.Screen name="(auth)/register" options={{ headerShown: false }} />
      <Stack.Screen name="(auth)/login" options={{ headerShown: false }} />
      <Stack.Screen name="(auth)/verification" options={{ headerShown: false }} />
      
      <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
      {/* <Stack.Screen name="product/[itemId]" options={{ headerShown: false }} /> */}
      <Stack.Screen name="product" options={{ headerShown: false }} />
      <Stack.Screen name="user/[userId]" options={{ headerShown: false }} />
      <Stack.Screen name="asks" options={{ headerShown: false }} />
      
      <Stack.Screen name="+not-found" />
    </Stack>
  );
};

const RootLayout: React.FC = () => {
  const colorScheme = useColorScheme();
  const [loaded] = useFonts({
    // SpaceMono: require('../../assets/fonts/SpaceMono-Regular.ttf'),
  });

  if (!loaded) {
    // Async font loading only occurs in development.
    return null;
  }

  return (
    <AuthProvider>
      <ThemeProvider value={colorScheme === 'dark' ? DarkTheme : DefaultTheme}>
        <AppNavigation />
        <StatusBar style="auto" />
      </ThemeProvider>
    </AuthProvider>
  );
};

const styles = StyleSheet.create({
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f5f5f5',
  },
  loadingText: {
    marginTop: 16,
    fontSize: 16,
    color: '#666',
    fontFamily: 'SpaceMono',
  },
});

export default RootLayout;

