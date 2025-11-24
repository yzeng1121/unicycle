import { render, fireEvent } from '@testing-library/react-native';
import LandingPage from '../app/index';

jest.mock('expo-router');
const { __mockNavigate, __mockPush, __mockReplace } = require('expo-router');

describe('landing page navigation buttons', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    })

    it('navigates to register page when register button pressed', () => {
        const { getByTestId } = render( <LandingPage />);
        const consoleOutput = jest.spyOn(console, 'log');

        fireEvent.press(getByTestId('signUpButton'));

        expect(consoleOutput).toHaveBeenCalledWith("sign up pressed");
        expect(__mockNavigate).toHaveBeenCalledWith('/(auth)/register')
    });

    it('navigates to register pagewhen pressed', () => {
        const { getByTestId } = render(<LandingPage />);
        const consoleOutput = jest.spyOn(console, 'log');

        fireEvent.press(getByTestId('loginButton'));

        expect(consoleOutput).toHaveBeenCalledWith("log in pressed");
        expect(__mockNavigate).toHaveBeenCalledWith('/(auth)/login')
    })
});
