import { 
  View, 
  Text, 
  TextInput, 
  TouchableOpacity, 
  StyleSheet, 
  ScrollView, 
  Alert, 
  KeyboardAvoidingView,
  Platform
} from 'react-native';
import { useState } from 'react';
import { router } from 'expo-router';
import * as SecureStore from 'expo-secure-store';

import { DORMS } from "../constants/Dorms";
import { Dropdown } from '../components/ui/Dropdown';

const RegisterPage = () => {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [selectedDorm, setSelectedDorm] = useState('');
  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  // store tokens securely
  const storeTokens = async (accessToken: string, refreshToken: string) => {
    try {
      await SecureStore.setItemAsync('accessToken', accessToken);
      await SecureStore.setItemAsync('refreshToken', refreshToken);
    } catch (error) {
      console.error('Error storing tokens:', error);
    }
  };

  // verify token format (basic JWT check)
  const isValidJWT = (token: string) => {
    if (!token) return false;
    const parts = token.split('.');
    return parts.length === 3;
  };

  // validate username
  const isValidUsername = (username: string) => {
    const usernameRegex = /^[a-zA-Z0-9_.]+$/;
    return usernameRegex.test(username) && username.length >= 5;
  };

  // password strength validation
  const isValidPassword = (password: string) => {
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-z\d@$!%*?&#]{8,}$/;
    return passwordRegex.test(password) && password.length >= 8;
  };

  const handleRegister = async () => {
    console.log("State:", {
      firstName, lastName, selectedDorm, email, username, password, confirmPassword
    });

    if (
      !firstName || 
      !lastName || 
      !selectedDorm || 
      selectedDorm === 'Select a dorm' || 
      !email || 
      !username || 
      !password ||
      !confirmPassword
    ) {
      Alert.alert('Error', 'Please fill in all fields.');
      return;
    }

    if (password !== confirmPassword) {
      Alert.alert('Error', 'Passwords do not match.');
      return;
    }

    if (!isValidPassword(password)) {
      Alert.alert('Error', 'Password must be at least 8 characters long, include uppercase, lowercase, numbers, and at least one special character.');
      return;
    }

    if (!isValidUsername(username)) {
      Alert.alert('Error', 'Username must be at least 5 characters and contain only letters, numbers, periods, and underscores.');
      return;
    }

    if (!email.includes('@') || !email.includes('tufts.edu')) {
      Alert.alert('Error', 'Please enter a valid Tufts University email.');
      return;
    }

    setIsLoading(true);

    try {
      const response = await fetch('http://10.243.122.160:8080/auth/signup', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        body: JSON.stringify({
          firstName: firstName,
          lastName: lastName,
          dorm: selectedDorm,
          email: email,
          username: username,
          password: password
        })
      });

      const responseData = await response.json();

      if (response.status === 200) {
        if (responseData.accessToken && responseData.refreshToken) {
          if (isValidJWT(responseData.accessToken) && isValidJWT(responseData.refreshToken)) {
            await storeTokens(responseData.accessToken, responseData.refreshToken);
          }
        }

        Alert.alert('Success', 'Account created successfully! Please check your email for verification.');

        router.replace({
          pathname: '/verification',
          params: { email: email }
        });
      } else if (response.status === 403) { // user exists, not verified
        Alert.alert('Account Not Verified', 'Please check your email for the verification code.');
        router.replace({
          pathname: "/verification",
          params: { email: email }
        });
      } else if (response.status === 409) {
        Alert.alert('Error', 'An account with this email or username already exists.');
      } else if (response.status === 400) {
        const errorMessage = responseData.message || 'Please check your input and try again.';
        Alert.alert('Error', errorMessage);
      } else {
        const errorMessage = responseData.message || 'Registration failed. Please try again.';
        Alert.alert('Error', errorMessage);
      }
    } catch (error) {
      console.error('Network error:', error);
      Alert.alert('Error', 'Network error. Please check your connection.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      keyboardVerticalOffset={Platform.OS === 'ios' ? 0 : 20}
    >
      <ScrollView
        keyboardShouldPersistTaps="always"
        contentContainerStyle={{ paddingBottom: 40, flexGrow: 1 }}
      >
        <View style={styles.content}>
          <Text style={styles.title}>Create Account</Text>
          
          {/* first name */}
          <View style={styles.inputGroup}>
            <Text style={styles.label}>First Name *</Text>
            <TextInput
              style={styles.input}
              placeholder="Enter your first name"
              value={firstName}
              onChangeText={setFirstName}
              autoCapitalize="words"
              editable={!isLoading}
              testID="firstNameInput"
            />
          </View>

          {/* last name */}
          <View style={styles.inputGroup}>
            <Text style={styles.label}>Last Name *</Text>
            <TextInput
              style={styles.input}
              placeholder="Enter your last name"
              value={lastName}
              onChangeText={setLastName}
              autoCapitalize="words"
              editable={!isLoading}
              testID="lastNameInput"
            />
          </View>

          {/* dorm */}
          <Dropdown
            label="Dorm"
            placeholder="Select a dorm"
            value={selectedDorm}
            onValueChange={setSelectedDorm}
            options={DORMS}
            required={true}
            maxHeight={250}
            testID="dropdownInput"
          />

          {/* school email */}
          <View style={styles.inputGroup}>
            <Text style={styles.label}>School Email *</Text>
            <TextInput
              style={styles.input}
              placeholder="first.last@tufts.edu"
              value={email}
              onChangeText={setEmail}
              keyboardType="email-address"
              autoCapitalize="none"
              autoCorrect={false}
              editable={!isLoading}
              testID="emailInput"
            />
          </View>

          {/* username */}
          <View style={styles.inputGroup}>
            <Text style={styles.label}>Username *</Text>
            <TextInput
              style={styles.input}
              placeholder="Choose a username (5+ characters, letters, numbers, _)"
              value={username}
              onChangeText={setUsername}
              autoCapitalize="none"
              autoCorrect={false}
              editable={!isLoading}
              testID="usernameInput"
            />
          </View>

          {/* password */}
          <View style={styles.inputGroup}>
            <Text style={styles.label}>Password *</Text>
            <Text>
              Password must be 8+ characters, include uppercase letters, lowercase letters, numbers, and at least one special character.
            </Text>
            <TextInput
              style={styles.input}
              placeholder="Create a password (8+ characters)"
              value={password}
              onChangeText={setPassword}
              secureTextEntry
              autoCapitalize="none"
              autoCorrect={false}
              editable={!isLoading}
              testID="passwordInput"
            />
          </View>

          {/* confirm password */}
          <View style={styles.inputGroup}>
            <Text style={styles.label}>Confirm Password *</Text>
            <TextInput
              style={styles.input}
              placeholder="Confirm your password"
              value={confirmPassword}
              onChangeText={setConfirmPassword}
              secureTextEntry
              autoCapitalize="none"
              autoCorrect={false}
              editable={!isLoading}
              testID="confirmPasswordInput"
            />
          </View>


          {/* register button */}
          <TouchableOpacity
            style={[styles.registerButton, isLoading && styles.registerButtonDisabled]}
            onPress={handleRegister}
            disabled={isLoading}
            activeOpacity={0.7}
            testID="registerButton"
          >
            <Text style={styles.registerButtonText}>
              {isLoading ? 'Creating Account...' : 'Create Account'}
            </Text>
          </TouchableOpacity>
          

          {/* login link */}
          <TouchableOpacity
            style={styles.loginLink}
            onPress={() => {
              console.log('login button pressed');
              router.push("/(auth)/login");
            }}
            disabled={isLoading}
            activeOpacity={0.7}
            testID="loginLink"
          >
            <Text style={styles.loginLinkText}>Already have an account? Sign in</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f9f1',
  },
  scrollView: {
    flex: 1,
  },
  content: {
    padding: 20,
    paddingTop: 60,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 30,
    color: '#1b0c0cff',
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
  dormButton: {
    backgroundColor: 'white',
    borderRadius: 8,
    padding: 15,
    borderWidth: 1,
    borderColor: '#ddd',
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  dormButtonDisabled: {
    backgroundColor: '#f5f5f5',
  },
  dormButtonText: {
    fontSize: 16,
    color: '#333',
  },
  dormButtonArrow: {
    fontSize: 12,
    color: '#666',
  },
  registerButton: {
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
  registerButtonDisabled: {
    backgroundColor: '#cccccc',
  },
  registerButtonText: {
    color: 'white',
    fontSize: 18,
    fontWeight: '600',
  },
  loginLink: {
    alignItems: 'center',
    marginTop: 20,
  },
  loginLinkText: {
    color: '#383d24ff',
    fontSize: 16,
  },
  dropdown: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderWidth: 2,
    borderColor: '#333',
    borderRadius: 8,
    padding: 15,
  },
  dropdownText: {
    fontSize: 16,
    color: '#666',
  },
  dropdownArrow: {
    fontSize: 12,
    color: '#333',
  },
  dropdownOptions: {
    position: 'absolute',
    top: '100%',
    left: 0,
    right: 0,
    backgroundColor: '#FFFFFF',
    borderWidth: 2,
    borderColor: '#333',
    borderRadius: 8,
    zIndex: 1000,
    marginTop: 2,
  },
  dropdownOption: {
    padding: 15,
    borderBottomWidth: 1,
    borderBottomColor: '#E0E0E0',
  },
  dropdownOptionText: {
    fontSize: 16,
    color: '#333',
  },
});

export default RegisterPage;

