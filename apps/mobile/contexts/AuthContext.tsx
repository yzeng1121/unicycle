import { 
  createContext, 
  useContext, 
  useState, 
  useEffect,
  ReactNode, 
} from 'react';
import { Alert } from 'react-native';
import * as SecureStore from 'expo-secure-store';
import { router } from 'expo-router';

interface User {
  userId: string;
  email: string;
  username: string;
  firstName?: string;
  lastName?: string;
}

// TODO: is it rlly necessary
interface AuthResponse {
  accessToken: string;
  refreshToken?: string;
  user?: User;
}

interface StoredTokens {
  accessToken: string | null;
  refreshToken: string | null;
}

interface AuthContextType {
  isAuthenticated: boolean;
  isLoading: boolean;
  accessToken: string | null;
  user: User | null; // TODO: under what circumstances would user be NULL?
  login: (accessToken: string, refreshToken: string) => Promise<void>;
  logout: () => Promise<void>;
  makeAuthenticatedRequest: (url: string, options?: RequestInit) => Promise<Response>;
  refreshAccessToken: () => Promise<string | null>;
}

interface AuthProviderProps {
  children: ReactNode;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [refreshToken, setRefreshToken] = useState<string | null>(null);
  const [user, setUser] = useState<User | null>(null);

  // TODO: remove refresh tokens thatre expired/no longer active
  // stores tokens into secure storage
  const storeTokens = async (accessToken: string, refreshToken: string): Promise<void> => {
    try {
      await SecureStore.setItemAsync('accessToken', accessToken);
      await SecureStore.setItemAsync('refreshToken', refreshToken);
      setAccessToken(accessToken);
    } catch (error) {
      console.error('Error storing tokens:', error);
      throw error;
    }
  }

  const getStoredTokens = async (): Promise<StoredTokens> => {
    try {
      const accessToken = await SecureStore.getItemAsync('accessToken');
      const refreshToken = await SecureStore.getItemAsync('refreshToken');
      return { accessToken, refreshToken };
    } catch (error) {
      console.error('Error getting stored tokens:', error);
      return { accessToken: null, refreshToken: null };
    }
  }

  const clearTokens = async (): Promise<void> => {
    try {
      await SecureStore.deleteItemAsync('accessToken');
      await SecureStore.deleteItemAsync('refreshToken');
    } catch (error) {
      console.error('Error clearing tokens:', error);
    }
  }

  // when failed to refresh access
  const handleAuthFailure = async (): Promise<void> => {
    await clearTokens();
    setIsAuthenticated(false);
    setUser(null);
    router.replace('/(auth)/login');

    Alert.alert(
      'Session Expired',
      'Please log in again to continue.',
      [{ text: 'OK' }]
    );
  }

  // verify access token
  const verifyTokenWithBackend = async (token: string): Promise<boolean> => {
    try {
      if (!token || token.trim().length < 10) return false;

      const response = await fetch('http://13.221.95.208:8080/users/me', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        }
      });

      if (response.ok) {
        const userData: User = await response.json();
        console.log('User data received:', userData);
        setUser(userData);
        return true;
      }

      console.log('Token verification failed with status:', response.status);
      const newAccessToken = await refreshAccessToken();

      if (newAccessToken) {
        await SecureStore.setItemAsync('accessToken', newAccessToken);
        setAccessToken(newAccessToken);
        return true;
      }
      return false;
    } catch (error) {
      console.error('Network error during token verification:', error);
      return false;
    }
  }

  const refreshAccessToken = async (): Promise<string | null> => {
    try {
      const { refreshToken } = await getStoredTokens();
      if (!refreshToken) {
        console.log('No refresh token available');
        throw new Error('No refresh token available.');
      }

      console.log('Attempting to refresh access token...');
      const response = await fetch('http://13.221.95.208:8080/auth/refresh', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ refreshToken })
      });

      console.log('Refresh token response status:', response.status);

      if (response.ok) {
        const data: AuthResponse = await response.json();
        console.log('Refresh token response data:', data);

        if (data.accessToken) {
          await SecureStore.setItemAsync('accessToken', data.accessToken);
          setAccessToken(data.accessToken);
          return data.accessToken;
        }
      }

      const errorData = await response.text();
      console.error('Failed to refresh token. Response:', errorData);
      throw new Error('Failed to refresh token.')
    } catch (error) {
      console.error('Error refreshing access token:', error);
      await handleAuthFailure();
      return null;
    }
  }

  const makeAuthenticatedRequest = async(
    url: string,
    options: RequestInit = {}
  ): Promise<Response> => {
    let token = accessToken;

    if (!token) {
      const { accessToken: storedToken } = await getStoredTokens();
      token = storedToken;
    }

    if (!token) throw new Error('No access token available');

    await checkAuthStatus();

    const authenticatedOptions: RequestInit = {
      ...options,
      headers: {
        ...options.headers,
        'Authorization': `Bearer ${token}`,
        ...options.headers,
      },
    };

    const response = await fetch(url, authenticatedOptions);

    if (response.status === 401 || response.status === 403) {
      const newToken = await refreshAccessToken();

      if (newToken && authenticatedOptions.headers) {
        (authenticatedOptions.headers as Record<string, string>)['Authorization'] = `Bearer ${newToken}`;
        return fetch(url, authenticatedOptions);
      } else {
        await handleAuthFailure();
        throw new Error('Authentication failed.');
      }
    }
    return response;
  }

  const checkAuthStatus = async (): Promise<void> => {
    try {
      const { accessToken: storedAccessToken } = await getStoredTokens();
      if (!storedAccessToken) {
        setIsAuthenticated(false);
        return;
      }

      const isValid = await verifyTokenWithBackend(storedAccessToken);

      if (isValid) {
        setAccessToken(storedAccessToken);
        setIsAuthenticated(true);
      } else {
        const newAccessToken = await refreshAccessToken();

        if (newAccessToken) {
          const isValidAfterRefresh = await verifyTokenWithBackend(newAccessToken);
          setIsAuthenticated(isValidAfterRefresh);
        } else {
          setIsAuthenticated(false);
        }
      }
    } catch (error) {
      console.error('Error checking auth status:', error);
      await handleAuthFailure();
    } finally {
      setIsLoading(false);
    }
  }

  const login = async (accessToken: string, refreshToken: string): Promise<void> => {
    try {
      await storeTokens(accessToken, refreshToken);
      const isValid = await verifyTokenWithBackend(accessToken);

      if (isValid) setIsAuthenticated(true);
      else throw new Error('Invalid tokens recieved.');
    } catch (error) {
      console.error('Error during login:', error);
      await clearTokens();
      throw error;
    }
  }

  const logout = async (): Promise<void> => {
    try {
      const { refreshToken } = await getStoredTokens();

      if (refreshToken) {
        await fetch('http://13.221.95.208:8080/auth/logout', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ refreshToken })
        });
      }
    } catch (error) {
      console.error('Error calling logout endpoint:', error);
    } finally {
      await clearTokens();
      router.replace('/(auth)/login');

      Alert.alert(
        'Successfully logged out',
        'Please log in again to continue.',
        [{ text: 'OK' }]
      );
    }
  }

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const value: AuthContextType = {
    isAuthenticated,
    isLoading, 
    accessToken,
    user,
    login,
    logout,
    makeAuthenticatedRequest,
    refreshAccessToken
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}
