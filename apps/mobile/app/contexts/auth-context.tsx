/*
 * authorizes + refreshes user's access token
 */

import { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import * as SecureStore from 'expo-secure-store';

// TODO: add more info here regarding user pfps, general data, etc.
interface User {
  userId: string;
  email: string;
  username: string;
  firstName?: string;
  lastName?: string;
}

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
  user: User | null; // TODO: cant be null bro
  login: (accessToken: string, refreshToken: string) => Promise<void>;
  logout: () => Promise<void>;
  makeAuthenticatedRequest: (url: string, options?: RequestInit) => Promise<Response>;
  refreshAccessToken: () => Promise<string | null>;
  clearTokens: () => Promise<void>;
}

interface AuthProviderProps {
  children: ReactNode;
}

// ============ CONTEXT SETUP ============

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

// ============ AUTH PROVIDER ============

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [user, setUser] = useState<User | null>(null);

  // ============ TOKEN STORAGE (Frontend Only) ============
  
  // TODO: remove refresh tokens thatre expired/no longer active
  // stores both access & refresh tokens into secure storage
  const storeTokens = async (accessToken: string, refreshToken: string): Promise<void> => {
    try {
      // TODO: remove the logs later... used for testing for now
      console.log('🎫 Access token preview:', accessToken.substring(0, 20) + '...');
      console.log('🎫 Refresh token preview:', refreshToken.substring(0, 20) + '...');

      await SecureStore.setItemAsync('accessToken', accessToken);
      await SecureStore.setItemAsync('refreshToken', refreshToken);
      setAccessToken(accessToken);

      const storedAccessToken = await SecureStore.getItemAsync('accessToken');
      const storedRefreshToken = await SecureStore.getItemAsync('refreshToken');
    
      console.log('✅ Access token stored:', storedAccessToken ? 'YES' : 'NO');
      console.log('✅ Refresh token stored:', storedRefreshToken ? 'YES' : 'NO');
      
    } catch (error) {
      console.error('Error storing tokens:', error);
      throw error;
    }
  };

  // getter for access & refresh tokens
  const getStoredTokens = async (): Promise<StoredTokens> => {
    try {
      const accessToken = await SecureStore.getItemAsync('accessToken');
      const refreshToken = await SecureStore.getItemAsync('refreshToken');
      return { accessToken, refreshToken };
    } catch (error) {
      console.error('Error getting stored tokens:', error);
      return { accessToken: null, refreshToken: null };
    }
  };

  // deletes access & refresh tokens from secure storage
  const clearTokens = async (): Promise<void> => {
    try {
      await SecureStore.deleteItemAsync('accessToken');
      await SecureStore.deleteItemAsync('refreshToken');
      setAccessToken(null);
      setUser(null);
      setIsAuthenticated(false);
    } catch (error) {
      console.error('Error clearing tokens:', error);
    }
  };
  

  // ============ BACKEND API CALLS ============

  // TODO BACKEND: Create GET /auth/validate endpoint
  // TODO: check over func, whether access token works
  // Should validate JWT and return user info if valid

  // verifies whether access token is valid, allowing user to access the home page
  const verifyTokenWithBackend = async (token: string): Promise<boolean> => {
    try {
      const response = await fetch('http://Yuxins-Mac.local:8080/users/me', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        }
      });

      console.log('📊 Response details:');
      console.log('  - Status:', response.status);
      console.log('  - Status Text:', response.statusText);
      console.log('  - Headers:', JSON.stringify(Object.fromEntries(response.headers.entries())));

      // TODO: /users/me returns username as the user's email rather than actual username
      if (response.ok) {
        const userData: User = await response.json(); // TODO: uncomment out
        setUser(userData); // Backend returns user info
        console.log("userData.userId: " + userData.userId); // TODO: is null

        return true;
      }
      return false;
    } catch (error) {
      console.error('Error verifying token:', error);
      // TODO: if response is NOT okay... double check whether refresh token is expired to refresh 
      // a new access token
      return false;
    }
  };

  // TODO BACKEND: Create POST /auth/refresh endpoint

  // Should accept refresh token and return new access token
  const refreshAccessToken = async (): Promise<string | null> => {
    try {
      const { refreshToken } = await getStoredTokens();
      if (!refreshToken) {
        throw new Error('No refresh token available');
      }

      const response = await fetch('http://Yuxins-Mac.local:8080/auth/refresh', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ refreshToken })
      });

      if (response.ok) {
        const data: AuthResponse = await response.json();
        if (data.accessToken) {
          console.log('✅ New access token obtained')
          await SecureStore.setItemAsync('accessToken', data.accessToken);
          setAccessToken(data.accessToken);
          
          // Optionally update refresh token if backend rotates it
          if (data.refreshToken) {
            await SecureStore.setItemAsync('refreshToken', data.refreshToken);
          }
          
          return data.accessToken;
        }
      }
      
      throw new Error('Failed to refresh token');
    } catch (error) {
      console.error('Error refreshing token:', error);
      await clearTokens();
      return null;
    }
  };

  // ============ AUTHENTICATED REQUESTS ============

  // 
  const makeAuthenticatedRequest = async (
    url: string, 
    options: RequestInit = {}
  ): Promise<Response> => {
    console.log('🔑 makeAuthenticatedRequest called for:', url);
    let token = accessToken; // get from memory
    
    // if no token in memory, try to get from storage
    if (!token) {
      const { accessToken: storedToken } = await getStoredTokens();
      token = storedToken;
      console.log('🔑 Got token from storage:', !!token);
    }

    if (!token) throw new Error('No access token available');

    checkAuthStatus();

    // allows secure/authenticated calls to APIs without having to re-authenticate
    const authenticatedOptions: RequestInit = {
      ...options,
      headers: {
        ...options.headers,
        'Authorization': `Bearer ${token}`,
        ...options.headers, 
      },
    };

    const response = await fetch(url, authenticatedOptions);
    console.log("Response status: " + response.status);
    
    // If we get 401, try to refresh token once
    if (response.status === 401 || response.status === 403) {
      const newToken = await refreshAccessToken();

      if (newToken && authenticatedOptions.headers) {
        (authenticatedOptions.headers as Record<string, string>)['Authorization'] = `Bearer ${newToken}`;
        return fetch(url, authenticatedOptions);
      } else {
        await clearTokens();
        throw new Error('Authentication failed');
      }
    }
    
    return response;
  };

  // ============ APP STARTUP AUTH CHECK ============

  const checkAuthStatus = async (): Promise<void> => {
    try {
      const { accessToken: storedAccessToken } = await getStoredTokens();
  
      console.log('✅ Access token fetched:', storedAccessToken ? 'YES' : 'NO');

      if (!storedAccessToken) {
        setIsAuthenticated(false);
        return;
      }

      // Let backend verify the token and get user info
      const isValid = await verifyTokenWithBackend(storedAccessToken);
      
      if (isValid) {
        console.log('Access token IS VALID');
        setAccessToken(storedAccessToken);
        setIsAuthenticated(true);
      } else {
        console.log('Access token IS NOT VALID');
        
        // Try to refresh if verification failed
        const newAccessToken = await refreshAccessToken();
        console.log("newAccessToken: " + newAccessToken);
        if (newAccessToken) {
          console.log('✅ new accessToken obtained: ' + accessToken ? 'YES' : 'NO')
          const isValidAfterRefresh = await verifyTokenWithBackend(newAccessToken);
          setIsAuthenticated(isValidAfterRefresh);
          console.log('❌ new accessToken NOT obtained: ' + accessToken ? 'YES' : 'NO')
        } else {
          console.log(' new accessToken obtained: ' + accessToken ? 'YES' : 'NO')
          setIsAuthenticated(false);
        }
      }
    } catch (error) {
      console.error('Error checking auth status:', error);
      await clearTokens();
      setIsAuthenticated(false);
    } finally {
      setIsLoading(false);
    }
  };

  // ============ LOGIN/LOGOUT FUNCTIONS ============

  const login = async (accessToken: string, refreshToken: string): Promise<void> => {
    try {
      await storeTokens(accessToken, refreshToken);
      
      // Verify token and get user info from backend
      const isValid = await verifyTokenWithBackend(accessToken);
      if (isValid) {
        setIsAuthenticated(true);
      } else {
        throw new Error('Invalid tokens received');
      }
    } catch (error) {
      console.error('Error during login:', error);
      await clearTokens();
      throw error;
    }
  };

  // TODO BACKEND: Create POST /auth/logout endpoint
  // Should invalidate/blacklist the refresh token
  const logout = async (): Promise<void> => {
    try {
      const { refreshToken } = await getStoredTokens();
      
      if (refreshToken) {
        // Call backend to invalidate refresh token
        await fetch('http://Yuxins-Mac.local:8080/auth/logout', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ refreshToken })
        });
      }
    } catch (error) {
      console.error('Error calling logout endpoint:', error);
      // Continue with local logout even if server call fails
    } finally {
      await clearTokens();
    }
  };

  // Check auth status on app load
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
    refreshAccessToken,
    clearTokens
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
  
};
