import React, { createContext, useContext, useState } from 'react';
import { api } from '../api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('smart_campus_user');
    if (!savedUser || savedUser === 'undefined') return null;
    try {
      return JSON.parse(savedUser);
    } catch (e) {
      localStorage.removeItem('smart_campus_user');
      return null;
    }
  });

  const [token, setToken] = useState(() => {
    const savedToken = localStorage.getItem('smart_campus_token');
    if (!savedToken || savedToken === 'undefined') return null;
    return savedToken;
  });

  const [loading, setLoading] = useState(false);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const response = await api.login({ email, password });
      
      const jwtToken = response.token;
      const userProfile = {
        id: response.userId,
        email: response.email,
        role: response.role,
        name: response.name,
        profileId: response.profileId,
      };

      if (jwtToken) {
        localStorage.setItem('smart_campus_token', jwtToken);
        setToken(jwtToken);
      }
      if (userProfile) {
        localStorage.setItem('smart_campus_user', JSON.stringify(userProfile));
        setUser(userProfile);
      }

      return userProfile;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem('smart_campus_token');
    localStorage.removeItem('smart_campus_user');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, logout, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
