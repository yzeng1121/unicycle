import { render, fireEvent, waitFor } from '@testing-library/react-native';
import LoginPage from '../app/(auth)/login';
import { Alert } from 'react-native';
import { useAuth } from '../contexts/AuthContext';

const { __mockPush, __mockReplace } = require('expo-router');
const mockAuthLogin = jest.fn();

jest.mock('../app/contexts/AuthContext', () => ({
    useAuth: jest.fn(() => ({
        login: mockAuthLogin,
        logout: jest.fn(),
        isAuthenticated: false,
        isLoading: false,
        accessToken: null,
        user: null,
    })),
}));

describe('login validation', () => {
    let alertSpy: jest.SpyInstance;

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
        fireEvent.changeText(component.getByPlaceholderText('first.last@tufts.edu'), 'john.doe@tufts.edu');
        fireEvent.changeText(component.getByPlaceholderText('Enter your password'), 'Password123!');
    };

    describe('Empty Field Validation', () => {
        it('shows error when all fields are empty', () => {
            const { getByText } = render(<LoginPage />);
            
            fireEvent.press(getByText('Sign In'));
            
            expect(alertSpy).toHaveBeenCalledWith('Error', 'Please fill in all fields.');
        });

        it('shows error when email is missing', () => {
            const { getByPlaceholderText, getByText } = render(<LoginPage />);
            
            fireEvent.changeText(getByPlaceholderText('Enter your password'), 'Password123!');
            
            fireEvent.press(getByText('Sign In'));
            
            expect(alertSpy).toHaveBeenCalledWith('Error', 'Please fill in all fields.');
        });

        it('shows error when password is missing', () => {
            const { getByPlaceholderText, getByText } = render(<LoginPage />);
            
            fireEvent.changeText(getByPlaceholderText('first.last@tufts.edu'), 'john.doe@tufts.edu');
            
            fireEvent.press(getByText('Sign In'));
            
            expect(alertSpy).toHaveBeenCalledWith('Error', 'Please fill in all fields.');
        });
    });

    describe('Email Validation', () => {
        it('shows error for invalid email (no @)', () => {
        const { getByPlaceholderText, getByText } = render(<LoginPage />);
        
        fireEvent.changeText(getByPlaceholderText('first.last@tufts.edu'), 'invalidemail');
        fireEvent.changeText(getByPlaceholderText('Enter your password'), 'Password123!');
        
        fireEvent.press(getByText('Sign In'));
        
        expect(alertSpy).toHaveBeenCalledWith('Error', 'Please enter a valid school email.');
        });

        it('shows error for non-educational email', () => {
        const { getByPlaceholderText, getByText } = render(<LoginPage />);
        
        fireEvent.changeText(getByPlaceholderText('first.last@tufts.edu'), 'john@gmail.com');
        fireEvent.changeText(getByPlaceholderText('Enter your password'), 'Password123!');
        
        fireEvent.press(getByText('Sign In'));
        
        expect(alertSpy).toHaveBeenCalledWith('Error', 'Please enter a valid school email.');
        });

        it('accepts valid .edu email', async () => {
        const { getByPlaceholderText, getByText } = render(<LoginPage />);
        
        fireEvent.changeText(getByPlaceholderText('first.last@tufts.edu'), 'john.doe@tufts.edu');
        fireEvent.changeText(getByPlaceholderText('Enter your password'), 'Password123!');
        
        fireEvent.press(getByText('Sign In'));
        
        // Should not show email validation error
        expect(alertSpy).not.toHaveBeenCalledWith('Error', 'Please enter a valid school email.');
        });
    });

    describe('Successful Login', () => {
        it('calls API with correct data on valid submission', async () => {
            const component = render(<LoginPage />);
            
            fillValidForm(component);
            
            fireEvent.press(component.getByText('Sign In'));
            
            await waitFor(() => {
                expect(fetch).toHaveBeenCalledWith(
                    'http://Yuxins-Mac.local:8080/auth/login',
                    expect.objectContaining({
                        method: 'POST',
                        headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                        },
                        body: JSON.stringify({
                        email: 'john.doe@tufts.edu',
                        password: 'Password123!'
                        })
                    })
                );
            });
        });

        it('calls authLogin and navigates to tabs on success', async () => {
            const component = render(<LoginPage />);
            
            fillValidForm(component);
            
            fireEvent.press(component.getByText('Sign In'));
            
            await waitFor(() => {
                expect(mockAuthLogin).toHaveBeenCalledWith('mock.access.token', 'mock.refresh.token');
            });
            
            await waitFor(() => {
                expect(alertSpy).toHaveBeenCalledWith('Success', 'Logged in successfully!');
                expect(__mockReplace).toHaveBeenCalledWith('../(tabs)');
            });
        });

        it('validates JWT token format before login', async () => {
            global.fetch = jest.fn(() =>
                Promise.resolve({
                    status: 200,
                    json: () => Promise.resolve({
                        accessToken: 'invalid-token',
                        refreshToken: 'invalid-token'
                    }),
                })
            ) as jest.Mock;

            const component = render(<LoginPage />);
            fillValidForm(component);
            
            fireEvent.press(component.getByText('Sign In'));
            
            await waitFor(() => {
                expect(alertSpy).toHaveBeenCalledWith('Error', 'Invalid token format received.');
            });
            
            expect(mockAuthLogin).not.toHaveBeenCalled();
        });

        it('shows error when tokens are missing from response', async () => {
            global.fetch = jest.fn(() =>
                Promise.resolve({
                status: 200,
                json: () => Promise.resolve({
                    // Missing accessToken and refreshToken
                }),
                })
            ) as jest.Mock;

            const component = render(<LoginPage />);
            fillValidForm(component);
            
            fireEvent.press(component.getByText('Sign In'));
            
            await waitFor(() => {
                expect(alertSpy).toHaveBeenCalledWith('Error', 'Invalid response from server. Please try again.');
            });
        });
    });

    describe('API Error Handling', () => {
        it('shows error for 401 (invalid credentials)', async () => {
            global.fetch = jest.fn(() =>
                Promise.resolve({
                    status: 401,
                    json: () => Promise.resolve({}),
                })
            ) as jest.Mock;

            const component = render(<LoginPage />);
            fillValidForm(component);
            
            fireEvent.press(component.getByText('Sign In'));
            
            await waitFor(() => {
                expect(alertSpy).toHaveBeenCalledWith('Error', 'Invalid email or password.');
            });
        });

        it('shows error for 404 (account not found)', async () => {
            global.fetch = jest.fn(() =>
                Promise.resolve({
                    status: 404,
                    json: () => Promise.resolve({}),
                })
            ) as jest.Mock;

            const component = render(<LoginPage />);
            fillValidForm(component);
            
            fireEvent.press(component.getByText('Sign In'));
            
            await waitFor(() => {
                expect(alertSpy).toHaveBeenCalledWith('Error', 'Account not found. Please check your email or create an account.');
            });
        });

        it('handles 403 (account not verified) and navigates to verification', async () => {
            // Mock the initial login call (403)
            global.fetch = jest.fn()
                .mockResolvedValueOnce({
                    status: 403,
                    json: () => Promise.resolve({}),
                })
                // Mock the resend verification call (200)
                .mockResolvedValueOnce({
                    ok: true,
                    json: () => Promise.resolve({}),
                });

            const component = render(<LoginPage />);
            fillValidForm(component);
            
            fireEvent.press(component.getByText('Sign In'));
            
            await waitFor(() => {
                expect(alertSpy).toHaveBeenCalledWith('Account Not Verified', 'Please check your email for the verification code.');
            });

            await waitFor(() => {
                expect(alertSpy).toHaveBeenCalledWith('Success', 'Verification email sent! Please check your inbox.');
            });

            await waitFor(() => {
                expect(__mockReplace).toHaveBeenCalledWith({
                    pathname: "/verification",
                    params: { email: 'john.doe@tufts.edu' }
                });
            });
        });

        it('handles failed resend verification email', async () => {
            global.fetch = jest.fn()
                .mockResolvedValueOnce({
                    status: 403,
                    json: () => Promise.resolve({}),
                })
                .mockResolvedValueOnce({
                    ok: false,
                    json: () => Promise.resolve({}),
                });

            const component = render(<LoginPage />);
            fillValidForm(component);
            
            fireEvent.press(component.getByText('Sign In'));
            
            await waitFor(() => {
                expect(alertSpy).toHaveBeenCalledWith('Error', 'Failed to resend verification email. Please try again.');
            });
        });

        it('handles network error gracefully', async () => {
            global.fetch = jest.fn(() => Promise.reject(new Error('Network error'))) as jest.Mock;

            const component = render(<LoginPage />);
            fillValidForm(component);
            
            fireEvent.press(component.getByText('Sign In'));
            
            await waitFor(() => {
                expect(alertSpy).toHaveBeenCalledWith('Error', 'Network error. Please check your connection.');
            });
        });
    });

    describe('Forgot Password', () => {
        it('shows error when email is empty', () => {
            const { getByText } = render(<LoginPage />);
            
            fireEvent.press(getByText('Forgot Password?'));
            
            expect(alertSpy).toHaveBeenCalledWith('Reset Password', 'Please enter your email address first');
        });

        it('shows success message when email is provided', () => {
            const { getByPlaceholderText, getByText } = render(<LoginPage />);
            
            fireEvent.changeText(getByPlaceholderText('first.last@tufts.edu'), 'john.doe@tufts.edu');
            fireEvent.press(getByText('Forgot Password?'));
            
            expect(alertSpy).toHaveBeenCalledWith('Password Reset', 'Password reset link sent to your email');
        });
    });

    describe('Navigation', () => {
        it('navigates to register page when register link is pressed', () => {
            const { getByText } = render(<LoginPage />);
            
            fireEvent.press(getByText("Don't have an account? Create one"));
            
            expect(__mockPush).toHaveBeenCalledWith('/register');
        });
    });

    describe('Loading State', () => {
        it('disables inputs and shows loading text during login', async () => {
            global.fetch = jest.fn(() => new Promise(() => {})) as jest.Mock;

            const { getByPlaceholderText, getByText } = render(<LoginPage />);
            
            fillValidForm({ getByPlaceholderText, getByText });
            
            fireEvent.press(getByText('Sign In'));
 
            await waitFor(() => {
                expect(getByText('Signing In...')).toBeTruthy();
            });
        });
    });
});