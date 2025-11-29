import { render, fireEvent, waitFor } from '@testing-library/react-native';
import RegisterPage from '../app/(auth)/register';
import { Alert } from 'react-native';
import * as SecureStore from 'expo-secure-store';

jest.mock('expo-router');
const { __mockPush, __mockReplace } = require('expo-router');
jest.mock('expo-secure-store');

jest.mock('../app/components/ui/Dropdown', () => ({
    Dropdown: ({ onValueChange, testID, value }: any) => {
        const { TouchableOpacity, Text } = require('react-native');
        return (
            <TouchableOpacity 
                testID={testID}
                onPress={() => onValueChange('Hodgedon Hall')}
            >
                <Text>{value || 'Select a dorm'}</Text>
            </TouchableOpacity>
        );
    }
}));

// TODO: registering after you've already made an account
// TODO: check whether a defauly profile is made...

let alertSpy: jest.SpyInstance;

describe('input validation', () => {
    beforeEach(() => {
        jest.clearAllMocks();
        alertSpy = jest.spyOn(Alert, 'alert');
    
        global.fetch = jest.fn(() =>
        Promise.resolve({
            status: 200,
            json: () => Promise.resolve({
            accessToken: 'mock.access.token',
            refreshToken: 'mock.refresh.token'
            }),
        })
    ) as jest.Mock;
});

afterEach(() => {
    jest.restoreAllMocks();
});

const fillValidForm = (component: any) => {
    fireEvent.changeText(component.getByTestId('firstNameInput'), 'John');
    fireEvent.changeText(component.getByTestId('lastNameInput'), 'Doe');
    fireEvent.press(component.getByTestId('dropdownInput'));
    fireEvent.changeText(component.getByTestId('emailInput'), 'john.doe@tufts.edu');
    fireEvent.changeText(component.getByTestId('usernameInput'), 'johndoe123');
    fireEvent.changeText(component.getByTestId('passwordInput'), 'Password123!');
    fireEvent.changeText(component.getByTestId('confirmPasswordInput'), 'Password123!');
};

describe('empty fields', () => {

    it('all fields empty + register button', () => {
        const { getByTestId } = render(<RegisterPage />);
        
        fireEvent.press(getByTestId('registerButton'));
        
        expect(alertSpy).toHaveBeenCalledWith('Error', 'Please fill in all fields.');
    });

    it('all fill in but first name', () => {
        const component = render(<RegisterPage />);
        
        fireEvent.changeText(component.getByTestId('lastNameInput'), 'Doe');
        fireEvent.press(component.getByTestId('dropdownInput'));
        fireEvent.changeText(component.getByTestId('emailInput'), 'john.doe@tufts.edu');
        fireEvent.changeText(component.getByTestId('usernameInput'), 'johndoe123');
        fireEvent.changeText(component.getByTestId('passwordInput'), 'Password123!');
        fireEvent.changeText(component.getByTestId('confirmPasswordInput'), 'Password123!');
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        expect(alertSpy).toHaveBeenCalledWith('Error', 'Please fill in all fields.');
    });

    it('all fill in but last name', () => {
        const component = render(<RegisterPage />);
        
        fireEvent.changeText(component.getByTestId('firstNameInput'), 'John');
        fireEvent.press(component.getByTestId('dropdownInput'));
        fireEvent.changeText(component.getByTestId('emailInput'), 'john.doe@tufts.edu');
        fireEvent.changeText(component.getByTestId('usernameInput'), 'johndoe123');
        fireEvent.changeText(component.getByTestId('passwordInput'), 'Password123!');
        fireEvent.changeText(component.getByTestId('confirmPasswordInput'), 'Password123!');
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        expect(alertSpy).toHaveBeenCalledWith('Error', 'Please fill in all fields.');
    });

    it('all fill in but dorm selection', () => {
        const component = render(<RegisterPage />);
        
        fireEvent.changeText(component.getByTestId('firstNameInput'), 'John');
        fireEvent.changeText(component.getByTestId('lastNameInput'), 'Doe');
        fireEvent.changeText(component.getByTestId('emailInput'), 'john.doe@tufts.edu');
        fireEvent.changeText(component.getByTestId('usernameInput'), 'johndoe123');
        fireEvent.changeText(component.getByTestId('passwordInput'), 'Password123!');
        fireEvent.changeText(component.getByTestId('confirmPasswordInput'), 'Password123!');
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        expect(alertSpy).toHaveBeenCalledWith('Error', 'Please fill in all fields.');
    });

    it('all fill in but email', () => {
        const component = render(<RegisterPage />);
        
        fireEvent.changeText(component.getByTestId('firstNameInput'), 'John');
        fireEvent.changeText(component.getByTestId('lastNameInput'), 'Doe');
        fireEvent.press(component.getByTestId('dropdownInput'));
        fireEvent.changeText(component.getByTestId('usernameInput'), 'johndoe123');
        fireEvent.changeText(component.getByTestId('passwordInput'), 'Password123!');
        fireEvent.changeText(component.getByTestId('confirmPasswordInput'), 'Password123!');
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        expect(alertSpy).toHaveBeenCalledWith('Error', 'Please fill in all fields.');
    });

    it('all fill in but username', () => {
        const component = render(<RegisterPage />);
        
        fireEvent.changeText(component.getByTestId('firstNameInput'), 'John');
        fireEvent.changeText(component.getByTestId('lastNameInput'), 'Doe');
        fireEvent.press(component.getByTestId('dropdownInput'));
        fireEvent.changeText(component.getByTestId('emailInput'), 'john.doe@tufts.edu');
        fireEvent.changeText(component.getByTestId('passwordInput'), 'Password123!');
        fireEvent.changeText(component.getByTestId('confirmPasswordInput'), 'Password123!');
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        expect(alertSpy).toHaveBeenCalledWith('Error', 'Please fill in all fields.');
    });

    it('all fill in but password', () => {
        const component = render(<RegisterPage />);
        
        fireEvent.changeText(component.getByTestId('firstNameInput'), 'John');
        fireEvent.changeText(component.getByTestId('lastNameInput'), 'Doe');
        fireEvent.press(component.getByTestId('dropdownInput'));
        fireEvent.changeText(component.getByTestId('emailInput'), 'john.doe@tufts.edu');
        fireEvent.changeText(component.getByTestId('usernameInput'), 'johndoe123');
        fireEvent.changeText(component.getByTestId('confirmPasswordInput'), 'Password123!');
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        expect(alertSpy).toHaveBeenCalledWith('Error', 'Please fill in all fields.');
    });

    it('all fill in but confirm password', () => {
        const component = render(<RegisterPage />);
        
        fireEvent.changeText(component.getByTestId('firstNameInput'), 'John');
        fireEvent.changeText(component.getByTestId('lastNameInput'), 'Doe');
        fireEvent.press(component.getByTestId('dropdownInput'));
        fireEvent.changeText(component.getByTestId('emailInput'), 'john.doe@tufts.edu');
        fireEvent.changeText(component.getByTestId('usernameInput'), 'johndoe123');
        fireEvent.changeText(component.getByTestId('passwordInput'), 'Password123!');
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        expect(alertSpy).toHaveBeenCalledWith('Error', 'Please fill in all fields.');
    });
});

describe('password validation', () => {
    it('passwords do not match', () => {
      const component = render(<RegisterPage />);
      
      fillValidForm(component);
      fireEvent.changeText(component.getByTestId('confirmPasswordInput'), 'DifferentPassword123!');
      
      fireEvent.press(component.getByTestId('registerButton'));
      
      expect(alertSpy).toHaveBeenCalledWith('Error', 'Passwords do not match.');
    });

    it('weak password (no uppercase)', () => {
        const component = render(<RegisterPage />);
        
        fillValidForm(component);
        fireEvent.changeText(component.getByTestId('passwordInput'), 'password123!');
        fireEvent.changeText(component.getByTestId('confirmPasswordInput'), 'password123!');
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        expect(alertSpy).toHaveBeenCalledWith(
            'Error',
            'Password must be at least 8 characters long, include uppercase, lowercase, numbers, and at least one special character.'
        );
    });

    it('weak password (no lowercase)', () => {
        const component = render(<RegisterPage />);
        
        fillValidForm(component);
        fireEvent.changeText(component.getByTestId('passwordInput'), 'PASSWORD123!');
        fireEvent.changeText(component.getByTestId('confirmPasswordInput'), 'PASSWORD123!');
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        expect(alertSpy).toHaveBeenCalledWith(
            'Error',
            'Password must be at least 8 characters long, include uppercase, lowercase, numbers, and at least one special character.'
        );
    });

    it('weak password (no numbers)', () => {
        const component = render(<RegisterPage />);
        
        fillValidForm(component);
        fireEvent.changeText(component.getByTestId('passwordInput'), 'Password!');
        fireEvent.changeText(component.getByTestId('confirmPasswordInput'), 'Password!');
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        expect(alertSpy).toHaveBeenCalledWith(
            'Error',
            'Password must be at least 8 characters long, include uppercase, lowercase, numbers, and at least one special character.'
        );
    });

    it('weak password (no special characters)', () => {
        const component = render(<RegisterPage />);
        
        fillValidForm(component);
        fireEvent.changeText(component.getByTestId('passwordInput'), 'Password123');
        fireEvent.changeText(component.getByTestId('confirmPasswordInput'), 'Password123');
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        expect(alertSpy).toHaveBeenCalledWith(
            'Error',
            'Password must be at least 8 characters long, include uppercase, lowercase, numbers, and at least one special character.'
        );
    });

    it('password less than 8 characters', () => {
        const component = render(<RegisterPage />);
        
        fillValidForm(component);
        fireEvent.changeText(component.getByTestId('passwordInput'), 'Pass1!');
        fireEvent.changeText(component.getByTestId('confirmPasswordInput'), 'Pass1!');
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        expect(alertSpy).toHaveBeenCalledWith(
            'Error',
            'Password must be at least 8 characters long, include uppercase, lowercase, numbers, and at least one special character.'
        );
    });
});

describe('Username Validation', () => {
    it('valid 5 character username', async () => {
        const component = render(<RegisterPage />);
        
        fillValidForm(component);
        fireEvent.changeText(component.getByTestId('usernameInput'), 'user5');
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        expect(alertSpy).not.toHaveBeenCalledWith(
            'Error',
            'Username must be at least 5 characters and contain only letters, numbers, periods, and underscores.'
        );
    });

    it('4 character username', () => {
        const component = render(<RegisterPage />);
        
        fillValidForm(component);
        fireEvent.changeText(component.getByTestId('usernameInput'), 'user');
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        expect(alertSpy).toHaveBeenCalledWith(
            'Error',
            'Username must be at least 5 characters and contain only letters, numbers, periods, and underscores.'
        );
    });

    it('username with invalid characters', () => {
        const component = render(<RegisterPage />);
        
        fillValidForm(component);
        fireEvent.changeText(component.getByTestId('usernameInput'), 'user@name');
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        expect(alertSpy).toHaveBeenCalledWith(
            'Error',
            'Username must be at least 5 characters and contain only letters, numbers, periods, and underscores.'
        );
    });

    it('accepts username with underscores and periods', async () => {
        const component = render(<RegisterPage />);
        
        fillValidForm(component);
        fireEvent.changeText(component.getByTestId('usernameInput'), 'user_name.123');
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        expect(alertSpy).not.toHaveBeenCalledWith(
            'Error',
            'Username must be at least 5 characters and contain only letters, numbers, periods, and underscores.'
        );
    });
});

describe('email validation', () => {
    it('non-university email', () => {
        const component = render(<RegisterPage />);
        
        fillValidForm(component);
        fireEvent.changeText(component.getByTestId('emailInput'), 'john@gmail.com');
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        expect(alertSpy).toHaveBeenCalledWith('Error', 'Please enter a valid Tufts University email.');
    });

    it('non-tufts university email', () => {
        const component = render(<RegisterPage />);
        
        fillValidForm(component);
        fireEvent.changeText(component.getByTestId('emailInput'), 'john@harvard.edu');
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        expect(alertSpy).toHaveBeenCalledWith('Error', 'Please enter a valid Tufts University email.');
    });

    it('valid tufts.edu email', async () => {
        const component = render(<RegisterPage />);
        
        fillValidForm(component);
        fireEvent.changeText(component.getByTestId('emailInput'), 'john.doe@tufts.edu');
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        expect(alertSpy).not.toHaveBeenCalledWith('Error', 'Please enter a valid Tufts University email.');
    });
  });

describe('successful registration', () => {
    it('calls API with correct data on valid submission', async () => {
        const component = render(<RegisterPage />);
        
        fillValidForm(component);
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        await waitFor(() => {
            expect(fetch).toHaveBeenCalledWith(
                'http://Yuxins-Mac.local:8080/auth/signup',
                expect.objectContaining({
                    method: 'POST',
                    headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                    },
                    body: JSON.stringify({
                    firstName: 'John',
                    lastName: 'Doe',
                    dorm: 'Hodgedon Hall',
                    email: 'john.doe@tufts.edu',
                    username: 'johndoe123',
                    password: 'Password123!'
                    })
                })
            );
        });
    });

    it('stores tokens and navigates to verification on success', async () => {
        const alertSpy = jest.spyOn(Alert, 'alert');
        const component = render(<RegisterPage />);
        
        fillValidForm(component);
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        await waitFor(() => {
            expect(SecureStore.setItemAsync).toHaveBeenCalledWith('accessToken', 'mock.access.token');
            expect(SecureStore.setItemAsync).toHaveBeenCalledWith('refreshToken', 'mock.refresh.token');
        });
        
        await waitFor(() => {
            expect(alertSpy).toHaveBeenCalledWith('Success', 'Account created successfully! Please check your email for verification.');
            expect(__mockReplace).toHaveBeenCalledWith({
            pathname: '/verification',
            params: { email: 'john.doe@tufts.edu' }
            });
        });
    });
});

describe('navigation', () => {
    it('navigates to login page when login link is pressed', () => {
        const { getByTestId } = render(<RegisterPage />);
        
        fireEvent.press(getByTestId('loginLink'));
        
        expect(__mockPush).toHaveBeenCalledWith('/(auth)/login');
    });
});

describe('API Error Handling', () => {
    it('shows error for 409 (user already exists)', async () => {
        global.fetch = jest.fn(() =>
            Promise.resolve({
                status: 409,
                json: () => Promise.resolve({}),
            })
        ) as jest.Mock;

        const component = render(<RegisterPage />);
        fillValidForm(component);
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        await waitFor(() => {
            expect(alertSpy).toHaveBeenCalledWith('Error', 'An account with this email or username already exists.');
        });
    });

    it('handles network error gracefully', async () => {
        global.fetch = jest.fn(() => Promise.reject(new Error('Network error'))) as jest.Mock;

        const component = render(<RegisterPage />);
        fillValidForm(component);
        
        fireEvent.press(component.getByTestId('registerButton'));
        
        await waitFor(() => {
            expect(alertSpy).toHaveBeenCalledWith('Error', 'Network error. Please check your connection.');
        });
    });
});
});