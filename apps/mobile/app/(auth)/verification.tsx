import { View, Text, TextInput, TouchableOpacity, StyleSheet, ScrollView, Alert } from 'react-native';
import React, { useState } from 'react';
import { useLocalSearchParams, router } from 'expo-router';

// TODO: have user input their email then send it from this page
// in what case would user be able to access verification page without either
// logging in first or registering first?
// TODO: case where user enters the wrong password
// TODO: case where user logs in with an account thats not registered
// TODO: confirm user password & throw error if they dont match
// TODO: option to allow user to see their password
const verification = () => {
  const { email } = useLocalSearchParams();
  const [verificationCode, setVerificationCode] = useState('');
  const [isResending, setIsResending] = useState(false);

  const handleVerifyCode = async () => {
    // Basic validation
    if (!verificationCode) {
      Alert.alert('Error', 'Please enter the verification code');
      return;
    }

    // TODO: where does my code generate? will it always be 6 digits??
    if (verificationCode.length < 6) {
      Alert.alert('Error', 'Please enter a valid verification code');
      return;
    }

    try {
      const response = await fetch('http://Yuxins-Mac.local:8080/auth/verify', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: email,
          verificationCode: verificationCode
        })
      });

      if (response.ok) {
        Alert.alert('Success', 'Email verified successfully!', [
          {
            text: 'Continue',
            onPress: () => router.replace('/(tabs)') // Navigate to main app
          }
        ]);
      } else {
        Alert.alert('Error', 'Invalid verification code. Please try again.');
      }
    } catch (error) {
      Alert.alert('Error', 'Verification failed. Please try again.');
    }

    console.log({ email, verificationCode });
  };

  const handleResendCode = async () => {
    if (isResending) return; // Prevent multiple requests

    setIsResending(true);
    
    try {
      const response = await fetch('http://Yuxins-Mac.local:8080/auth/resend', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email })
      });

      if (response.ok) {
        Alert.alert('Code Sent', 'A new verification code has been sent to your email');
      } else {
        Alert.alert('Error', 'Failed to resend verification code.');
      }
    } catch (error) {
      Alert.alert('Error', 'Failed to resend verification code.');
    }

    setIsResending(false);
  };

  const handleBackToLogin = () => {
    router.push('/(auth)/login');
  };

  return (
    <ScrollView style={styles.container}>
      <View style={styles.content}>
        <Text style={styles.title}>Verify Your Email</Text>
        <Text style={styles.subtitle}>
          A verification code has been sent to your email (double check your junk). Please enter it below.
        </Text>
        
        {/* Verification Code Input */}
        <View style={styles.inputGroup}>
          <Text style={styles.label}>Verification Code</Text>
          <TextInput
            style={styles.input}
            placeholder="Enter 6-digit code"
            value={verificationCode}
            onChangeText={setVerificationCode}
            keyboardType="number-pad"
            maxLength={6}
            autoCapitalize="none"
            autoCorrect={false}
            textAlign="center"
          />
        </View>

        {/* submit */}
        <TouchableOpacity style={styles.verifyButton} onPress={handleVerifyCode}>
          <Text style={styles.verifyButtonText}>Verify Email</Text>
        </TouchableOpacity>

        {/* resend */}
        <TouchableOpacity 
          style={styles.resendLink} 
          onPress={handleResendCode}
          disabled={isResending}
        >
          <Text style={[styles.resendText, isResending && styles.disabledText]}>
            {isResending ? 'Sending...' : "Didn't get a verification code? Press here to send another one"}
          </Text>
        </TouchableOpacity>

        {/* back to login */}
        <TouchableOpacity style={styles.backLink} onPress={handleBackToLogin}>
          <Text style={styles.backLinkText}>Back to Sign In</Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f9f1',
  },
  content: {
    padding: 20,
    paddingTop: 80,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 30,
    color: '#1b0c0cff',
  },
  subtitle: {
    fontSize: 16,
    textAlign: 'center',
    marginBottom: 30,
    color: '#666',
    lineHeight: 22,
  },
  inputGroup: {
    marginBottom: 30,
  },
  label: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 8,
    color: '#333',
    textAlign: 'center',
  },
  input: {
    backgroundColor: 'white',
    borderRadius: 8,
    padding: 15,
    fontSize: 16,
    borderWidth: 1,
    borderColor: '#ddd',
  },
  verifyButton: {
    backgroundColor: '#9daa72ff',
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
  verifyButtonText: {
    color: 'white',
    fontSize: 18,
    fontWeight: '600',
  },
  resendLink: {
    alignItems: 'center',
    marginBottom: 20,
    paddingVertical: 10,
  },
  resendText: {
    color: '#383d24ff',
    fontSize: 14,
    textAlign: 'center',
    lineHeight: 20,
  },
  disabledText: {
    color: '#999',
  },
  backLink: {
    alignItems: 'center',
    marginTop: 10,
  },
  backLinkText: {
    color: '#383d24ff',
    fontSize: 16,
  },
});

export default verification;