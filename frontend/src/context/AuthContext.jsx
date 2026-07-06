import { createContext, useContext, useState } from 'react';
import { authApi } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const s = localStorage.getItem('billsplit_user');
    return s ? JSON.parse(s) : null;
  });

  const persist = (data) => {
    localStorage.setItem('billsplit_token', data.token);
    const u = { id: data.userId, fullName: data.fullName, email: data.email };
    localStorage.setItem('billsplit_user', JSON.stringify(u));
    setUser(u);
  };

  const login = async (email, password) => {
    const { data } = await authApi.login({ email, password });
    persist(data);
  };

  const register = async (email, password, fullName) => {
    const { data } = await authApi.register({ email, password, fullName });
    persist(data);
  };

  const logout = () => {
    localStorage.removeItem('billsplit_token');
    localStorage.removeItem('billsplit_user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
