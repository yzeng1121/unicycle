import { 
  View, 
  Text, 
  TextInput, 
  TouchableOpacity, 
  StyleSheet, 
  ScrollView, 
  Alert, 
  Modal, 
  KeyboardAvoidingView,
  Platform
} from 'react-native';
import { useState } from 'react';
import { Picker } from '@react-native-picker/picker';
import { router } from 'expo-router';
import * as SecureStore from 'expo-secure-store';

import { DORMS } from "../../constants/Dorms";

const register = () => {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [selectedDorm, setSelectedDorm] = useState('');
  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  // Modal state
  const [modalVisible, setModalVisible] = useState(false);
  const [tempSelectedDorm, setTempSelectedDorm] = useState('');

  // Function to store tokens securely
  const storeTokens = async (accessToken: string, refreshToken: string) => {
    try {
      await SecureStore.setItemAsync('accessToken', accessToken);
      await SecureStore.setItemAsync('refreshToken', refreshToken);
    } catch (error) {
      console.error('Error storing tokens:', error);
    }
  };

  // Function to verify token format (basic JWT check)
  const isValidJWT = (token: string) => {
    if (!token) return false;
    const parts = token.split('.');
    return parts.length === 3;
  };

  // Validate username (only alphanumeric and underscores)
  const isValidUsername = (username: string) => {
    const usernameRegex = /^[a-zA-Z0-9_.]+$/;
    return usernameRegex.test(username) && username.length >= 3;
  };

  // Password strength validation
  const isValidPassword = (password: string) => {
    return password.length >= 8;
  };

  const handleRegister = async () => {
    // Basic validation
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
      Alert.alert('Error', 'Password must be at least 8 characters long.');
      return;
    }

    if (!isValidUsername(username)) {
      Alert.alert('Error', 'Username must be at least 3 characters and contain only letters, numbers, and underscores.');
      return;
    }

    if (!email.includes('@') || !email.includes('tufts.edu')) {
      Alert.alert('Error', 'Please enter a valid Tufts University email.');
      return;
    }

    setIsLoading(true);

    try {
      const response = await fetch('http://10.243.71.82:8080/auth/signup', {
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
        // Check if tokens are provided (some systems send tokens immediately)
        if (responseData.accessToken && responseData.refreshToken) {
          // Validate token format
          if (isValidJWT(responseData.accessToken) && isValidJWT(responseData.refreshToken)) {
            // Store tokens securely
            await storeTokens(responseData.accessToken, responseData.refreshToken);
          }
        }

        Alert.alert('Success', 'Account created successfully! Please check your email for verification.');

        // Navigate to verification screen
        router.replace({
          pathname: '/(auth)/verification',
          params: { email: email }
        });
      } else if (response.status === 403) {
        // User exists but is not verified
        Alert.alert('Account Not Verified', 'Please check your email for the verification code.');
        router.replace({
          pathname: "/verification",
          params: { email: email }
        });
      } else if (response.status === 409) {
        // User already exists
        Alert.alert('Error', 'An account with this email or username already exists.');
      } else if (response.status === 400) {
        // Bad request - validation errors
        const errorMessage = responseData.message || 'Please check your input and try again.';
        Alert.alert('Error', errorMessage);
      } else {
        // Handle other error responses
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

  // Modal handlers
  const openDormPicker = () => {
    if (isLoading) return;
    setTempSelectedDorm(selectedDorm);
    setModalVisible(true);
  };

  const confirmDormSelection = () => {
    setSelectedDorm(tempSelectedDorm);
    setModalVisible(false);
  };

  const cancelDormSelection = () => {
    setTempSelectedDorm(selectedDorm);
    setModalVisible(false);
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      keyboardVerticalOffset={Platform.OS === 'ios' ? 0 : 20}
    >
      <ScrollView style={styles.scrollView}>
        <View style={styles.content}>
          <Text style={styles.title}>Create Account</Text>
          
          {/* First Name */}
          <View style={styles.inputGroup}>
            <Text style={styles.label}>First Name *</Text>
            <TextInput
              style={styles.input}
              placeholder="Enter your first name"
              value={firstName}
              onChangeText={setFirstName}
              autoCapitalize="words"
              editable={!isLoading}
            />
          </View>

          {/* Last Name */}
          <View style={styles.inputGroup}>
            <Text style={styles.label}>Last Name *</Text>
            <TextInput
              style={styles.input}
              placeholder="Enter your last name"
              value={lastName}
              onChangeText={setLastName}
              autoCapitalize="words"
              editable={!isLoading}
            />
          </View>

          {/* Dorm Selection */}
          <View style={styles.inputGroup}>
            <Text style={styles.label}>Dorm *</Text>
            <TouchableOpacity 
              style={[styles.dormButton, isLoading && styles.dormButtonDisabled]} 
              onPress={openDormPicker}
              disabled={isLoading}
            >
              <Text style={styles.dormButtonText}>
                {selectedDorm || 'Select a dorm'}
              </Text>
              <Text style={styles.dormButtonArrow}>▼</Text>
            </TouchableOpacity>
          </View>

          {/* School Email */}
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
            />
          </View>

          {/* Username */}
          <View style={styles.inputGroup}>
            <Text style={styles.label}>Username *</Text>
            <TextInput
              style={styles.input}
              placeholder="Choose a username (3+ characters, letters, numbers, _)"
              value={username}
              onChangeText={setUsername}
              autoCapitalize="none"
              autoCorrect={false}
              editable={!isLoading}
            />
          </View>

          {/* Password */}
          <View style={styles.inputGroup}>
            <Text style={styles.label}>Password *</Text>
            <TextInput
              style={styles.input}
              placeholder="Create a password (8+ characters)"
              value={password}
              onChangeText={setPassword}
              secureTextEntry
              autoCapitalize="none"
              autoCorrect={false}
              editable={!isLoading}
            />
          </View>

          {/* Confirm Password */}
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
            />
          </View>

          {/* Register Button */}
          <TouchableOpacity 
            style={[styles.registerButton, isLoading && styles.registerButtonDisabled]} 
            onPress={handleRegister}
            disabled={isLoading}
          >
            <Text style={styles.registerButtonText}>
              {isLoading ? 'Creating Account...' : 'Create Account'}
            </Text>
          </TouchableOpacity>

          {/* Login Link */}
          <TouchableOpacity 
            style={styles.loginLink} 
            onPress={() => router.push("/login")}
            disabled={isLoading}
          >
            <Text style={styles.loginLinkText}>Already have an account? Sign in</Text>
          </TouchableOpacity>
        </View>

        {/* Modal for Dorm Selection */}
        <Modal
          animationType="slide"
          transparent={true}
          visible={modalVisible}
          onRequestClose={cancelDormSelection}
        >
          <View style={styles.modalOverlay}>
            <View style={styles.modalContent}>
              {/* Modal Header */}
              <View style={styles.modalHeader}>
                <TouchableOpacity onPress={cancelDormSelection}>
                  <Text style={styles.cancelButton}>Cancel</Text>
                </TouchableOpacity>
                <Text style={styles.modalTitle}>Select Dorm</Text>
                <TouchableOpacity onPress={confirmDormSelection}>
                  <Text style={styles.doneButton}>Done</Text>
                </TouchableOpacity>
              </View>
              
              {/* Picker */}
              <Picker
                selectedValue={tempSelectedDorm}
                onValueChange={setTempSelectedDorm}
                style={styles.modalPicker}
              >
                <Picker.Item label="Select a dorm" value="" />
                {DORMS.map((dorm, index) => (
                  <Picker.Item key={index} label={dorm} value={dorm} />
                ))}
              </Picker>
            </View>
          </View>
        </Modal>
      </ScrollView>
    </KeyboardAvoidingView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
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
    color: '#333',
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
    backgroundColor: '#007AFF',
    borderRadius: 8,
    padding: 15,
    alignItems: 'center',
    marginTop: 20,
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
    color: '#007AFF',
    fontSize: 16,
  },
  // Modal styles
  modalOverlay: {
    flex: 1,
    justifyContent: 'flex-end',
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
  },
  modalContent: {
    backgroundColor: 'white',
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    maxHeight: '50%',
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 15,
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#333',
  },
  cancelButton: {
    fontSize: 16,
    color: '#007AFF',
  },
  doneButton: {
    fontSize: 16,
    fontWeight: '600',
    color: '#007AFF',
  },
  modalPicker: {
    height: 200,
  },
});

export default register;

