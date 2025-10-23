import { View, Text, TextInput, TouchableOpacity, StyleSheet, ScrollView, Alert } from 'react-native';
import { useState } from 'react';
import { router } from 'expo-router';
import { useAuth } from '../contexts/auth-context';

import * as SecureStore from 'expo-secure-store';

const login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  
  const { login: authLogin } = useAuth();

  // TODO: backend already has an isValid function
  // Function to verify token format (basic JWT check)
  const isValidJWT = (token: string) => {
    if (!token) return false;
    const parts = token.split('.');
    return parts.length === 3;
  };

  const handleLogin = async () => {
    // Basic validation
    if (!email || !password) {
      Alert.alert('Error', 'Please fill in all fields.');
      return;
    }

    if (!email.includes('@') || !email.includes('.edu')) {
      Alert.alert('Error', 'Please enter a valid school email.');
      return;
    }

    setIsLoading(true);

    try {
      const response = await fetch('http://10.243.71.82:8080/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        body: JSON.stringify({
          email: email,
          password: password
        })
      });

      const responseData = await response.json();

      if (response.status === 200) {
        // Extract tokens from response
        const { accessToken, refreshToken } = responseData;
        
        if (!accessToken || !refreshToken) {
          Alert.alert('Error', 'Invalid response from server. Please try again.');
          return;
        }

        // Validate token format
        if (!isValidJWT(accessToken) || !isValidJWT(refreshToken)) {
          Alert.alert('Error', 'Invalid token format received.');
          return;
        }

        // Use auth context to handle login i.e. store access & refresh tokens
        await authLogin(accessToken, refreshToken);
        
        Alert.alert('Success', 'Logged in successfully!');
        
        // Navigation will be handled automatically by the auth context
        // The app will re-render and show the authenticated screens
        // Navigate to main app
        router.replace('/(tabs)');
        
      } else if (response.status === 403) {
        // User exists but is not verified
        Alert.alert('Account Not Verified', 'Please check your email for the verification code.');
        
        try {
          const resendResponse = await fetch(`http://10.243.71.82:8080/auth/resend?email=${encodeURIComponent(email)}`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              'Accept': 'application/json',
            }
          });
          
          if (resendResponse.ok) {
            Alert.alert('Success', 'Verification email sent! Please check your inbox.');
          } else {
            Alert.alert('Error', 'Failed to resend verification email. Please try again.');
          }
        } catch (resendError) {
          console.error('Resend error:', resendError);
          Alert.alert('Error', 'Network error. Please check your connection.');
        }
        
        router.replace({
          pathname: "/verification",
          params: { email: email }
        });
      } else if (response.status === 401) {
        Alert.alert('Error', 'Invalid email or password.');
      } else if (response.status === 404) {
        Alert.alert('Error', 'Account not found. Please check your email or create an account.');
      } else {
        const errorMessage = responseData.message || 'Login failed. Please try again.';
        Alert.alert('Error', errorMessage);
      }
    } catch (error) {
      console.error('Network error:', error);
      Alert.alert('Error', 'Network error. Please check your connection.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleForgotPassword = () => {
    if (!email) {
      Alert.alert('Reset Password', 'Please enter your email address first');
      return;
    }
    // TODO: Implement password reset functionality
    Alert.alert('Password Reset', 'Password reset link sent to your email');
  };

  return (
    <ScrollView style={styles.container}>
      <View style={styles.content}>
        <Text style={styles.title}>Welcome Back</Text>
        <Text style={styles.subtitle}>Sign in to your account</Text>
        
        {/* Email */}
        <View style={styles.inputGroup}>
          <Text style={styles.label}>School Email</Text>
          <TextInput
            style={styles.input}
            placeholder="first.last@tufts.edu"
            value={email}
            onChangeText={setEmail}
            keyboardType="email-address"
            autoCapitalize="none"
            autoCorrect={false}
            editable={!isLoading}
          />
        </View>

        {/* Password */}
        <View style={styles.inputGroup}>
          <Text style={styles.label}>Password</Text>
          <TextInput
            style={styles.input}
            placeholder="Enter your password"
            value={password}
            onChangeText={setPassword}
            secureTextEntry
            autoCapitalize="none"
            autoCorrect={false}
            editable={!isLoading}
          />
        </View>

        {/* Forgot Password Link */}
        <TouchableOpacity 
          style={styles.forgotPasswordLink} 
          onPress={handleForgotPassword}
          disabled={isLoading}
        >
          <Text style={styles.forgotPasswordText}>Forgot Password?</Text>
        </TouchableOpacity>

        {/* Login Button */}
        <TouchableOpacity 
          style={[styles.loginButton, isLoading && styles.loginButtonDisabled]} 
          onPress={handleLogin}
          disabled={isLoading}
        >
          <Text style={styles.loginButtonText}>
            {isLoading ? 'Signing In...' : 'Sign In'}
          </Text>
        </TouchableOpacity>

        {/* Register Link */}
        <TouchableOpacity 
          style={styles.registerLink} 
          onPress={() => router.push("/register")}
          disabled={isLoading}
        >
          <Text style={styles.registerLinkText}>Don't have an account? Create one</Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  content: {
    padding: 20,
    paddingTop: 80,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 8,
    color: '#333',
  },
  subtitle: {
    fontSize: 16,
    textAlign: 'center',
    marginBottom: 40,
    color: '#666',
  },
  inputGroup: {
    marginBottom: 20,
  },
  label: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 8,
    color: '#333',
  },
  input: {
    backgroundColor: 'white',
    borderRadius: 8,
    padding: 15,
    fontSize: 16,
    borderWidth: 1,
    borderColor: '#ddd',
  },
  forgotPasswordLink: {
    alignItems: 'flex-end',
    marginBottom: 30,
  },
  forgotPasswordText: {
    color: '#007AFF',
    fontSize: 14,
  },
  loginButton: {
    backgroundColor: '#007AFF',
    borderRadius: 8,
    padding: 15,
    alignItems: 'center',
    marginBottom: 20,
  },
  loginButtonDisabled: {
    backgroundColor: '#cccccc',
  },
  loginButtonText: {
    color: 'white',
    fontSize: 18,
    fontWeight: '600',
  },
  registerLink: {
    alignItems: 'center',
    marginTop: 20,
  },
  registerLinkText: {
    color: '#007AFF',
    fontSize: 16,
  },
});

export default login;
