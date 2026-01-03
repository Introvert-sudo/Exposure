import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const AuthScreen = () => {
  const [isLogin, setIsLogin] = useState(true); // Переключатель Вход/Регистрация
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // URL зависит от вашего бэкенда
    const endpoint = isLogin ? '/api/login' : '/api/register';
    
    // ЧТО ПЕРЕДАЕМ: JSON с username и password
    const payload = { username, password };

    try {
      const response = await fetch(`http://localhost:8080${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      const data = await response.json();

      if (response.ok) {
        // ЧТО ОЖИДАЕМ: Обычно бэкенд возвращает токен (JWT) или данные пользователя
        console.log('Успех:', data);
        localStorage.setItem('token', data.token); // Сохраняем токен
        navigate('/'); // Перенаправляем на главную
      } else {
        alert(data.message || 'Ошибка авторизации');
      }
    } catch (error) {
      console.error('Ошибка при запросе:', error);
    }
  };

  return (
    <div style={{ maxWidth: '300px', margin: '50px auto', textAlign: 'center' }}>
      <h2>{isLogin ? 'Вход' : 'Регистрация'}</h2>
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="Имя пользователя"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
          style={{ display: 'block', width: '100%', marginBottom: '10px', padding: '8px' }}
        />
        <input
          type="password"
          placeholder="Пароль"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          style={{ display: 'block', width: '100%', marginBottom: '10px', padding: '8px' }}
        />
        <button type="submit" style={{ width: '100%', padding: '10px', cursor: 'pointer' }}>
          {isLogin ? 'Войти' : 'Создать аккаунт'}
        </button>
      </form>
      <button 
        onClick={() => setIsLogin(!isLogin)} 
        style={{ marginTop: '15px', background: 'none', border: 'none', color: 'blue', cursor: 'pointer' }}
      >
        {isLogin ? 'Нет аккаунта? Зарегистрируйтесь' : 'Уже есть аккаунт? Войдите'}
      </button>
    </div>
  );
};

export default AuthScreen;
