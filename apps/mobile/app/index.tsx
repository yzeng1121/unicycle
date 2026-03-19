import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Dimensions, Button } from 'react-native';
import { useFonts } from 'expo-font';
import * as SplashScreen from 'expo-splash-screen';

import { router } from 'expo-router';

SplashScreen.preventAutoHideAsync();

export default function LandingPage() {
  const [fontsLoaded] = useFonts({
    'Sunday-Morning': require('../assets/fonts/Sunday-Morning.otf'),
    'SpaceMono-Regular': require('../assets/fonts/SpaceMono-Regular.ttf'),
  });

  React.useEffect(() => {
    if (fontsLoaded) {
      SplashScreen.hideAsync();
    }
  }, [fontsLoaded]);

  if (!fontsLoaded) {
    return null;
  }

  const handleSignUp = () => {
    router.navigate('/(auth)/register');
  };

  const handleLogIn = () => {
    router.navigate('/(auth)/login')
  };

  return (
    <View style={styles.container}>

      {/* title */}
      <View style={styles.topSection}>
        <Text style={styles.title}>unicycle</Text>
      </View>

      {/* logo */}
      <View style={styles.middleSection}>
        <View style={styles.logoContainer}>
          <View style={styles.wheel}>
            <View style={styles.wheelInner} />
          </View>
          <View style={styles.seat} />
          <View style={styles.post} />
          <View style={styles.pedal} />
        </View>
      </View>

      {/* Bottom Section - Buttons */}
      <View style={styles.bottomSection}>
        <TouchableOpacity 
          style={[styles.button, styles.signUpButton]} 
          onPress={handleSignUp}
          activeOpacity={0.8}
          testID="signUpButton"
        >
          <Text style={styles.buttonText}>Sign Up</Text>
        </TouchableOpacity>

        <TouchableOpacity 
          style={[styles.button, styles.logInButton]} 
          onPress={handleLogIn}
          activeOpacity={0.8}
          testID="loginButton"
        >
          <Text style={[styles.buttonText, styles.logInText]}>Log In</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f9f1',
  },
  topSection: {
    flex: 0.25,
    justifyContent: 'center',
    alignItems: 'center',
    paddingTop: 125,
  },
  title: {
    fontFamily: 'Sunday-Morning',
    fontSize: 60,
    color: '#9ca971ff',
    letterSpacing: 2,
  },
  middleSection: {
    flex: 0.5,
    justifyContent: 'center',
    alignItems: 'center',
    paddingBottom: 50,
  },
  logoContainer: {
    width: 150,
    height: 200,
    justifyContent: 'center',
    alignItems: 'center',
    position: 'relative',
  },
  wheel: {
    width: 120,
    height: 120,
    borderRadius: 60,
    borderWidth: 8,
    borderColor: '#415a6a',
    justifyContent: 'center',
    alignItems: 'center',
    position: 'absolute',
    bottom: 0,
  },
  wheelInner: {
    width: 80,
    height: 80,
    borderRadius: 40,
    borderWidth: 4,
    borderColor: '#678395ff',
  },
  post: {
    width: 6,
    height: 100,
    backgroundColor: '#415a6a',
    position: 'absolute',
    top: 30,
    borderRadius: 3,
  },
  seat: {
    width: 50,
    height: 20,
    backgroundColor: '#6d859fff',
    borderRadius: 10,
    position: 'absolute',
    top: 20,
  },
  pedal: {
    width: 40,
    height: 8,
    backgroundColor: '#313437ff',
    position: 'absolute',
    bottom: 50,
    borderRadius: 4,
  },
  bottomSection: {
    flex: 0.25,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 40,
    paddingBottom: 125,
  },
  button: {
    width: '100%',
    height: 55,
    borderRadius: 27.5,
    justifyContent: 'center',
    alignItems: 'center',
    marginVertical: 10,
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.1,
    shadowRadius: 3.84,
    elevation: 5,
  },
  signUpButton: {
    backgroundColor: '#9daa72ff',
  },
  logInButton: {
    backgroundColor: 'transparent',
    borderWidth: 2,
    borderColor: '#7c8a4bff',
  },
  buttonText: {
    fontSize: 18,
    fontWeight: '600',
    color: '#FFFFFF',
    letterSpacing: 0.5,
  },
  logInText: {
    color: '#2a2011ff',
  },
});