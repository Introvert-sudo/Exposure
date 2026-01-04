import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import MainScreen from './MainScreen';
import GameScreen from './GameScreen';
import AuthScreen from './AuthScreen';
import React, { useState } from 'react';

export default function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(!!localStorage.getItem('token'));

  return (
    <Router>
      <Routes>
        <Route 
          path="/auth" 
          element={<AuthScreen setIsAuthenticated={setIsAuthenticated} />} 
        />
        
        {/* Защищенные роуты */}
        <Route 
          path="/" 
          element={isAuthenticated ? <MainScreen /> : <Navigate to="/auth" />} 
        />
        <Route 
          path="/game" 
          element={isAuthenticated ? <GameScreen /> : <Navigate to="/auth" />} 
        />
      </Routes>
    </Router>
  );
}
