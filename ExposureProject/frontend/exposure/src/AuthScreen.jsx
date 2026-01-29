import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const API_URL = import.meta.env.VITE_API_URL;

export default function AuthScreen({ setIsAuthenticated }) {
  const [isLogin, setIsLogin] = useState(true);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    const endpoint = isLogin ? '/api/login' : '/api/register';
    const payload = { username, password };

    try {
      const response = await fetch(API_URL + endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      const data = await response.json();

      if (response.ok) {
        localStorage.setItem('token', data.token);
        setIsAuthenticated(true);
        navigate('/');
      } else {
        setError(data.message || 'Ошибка авторизации');
      }
    } catch (err) {
      console.error('Ошибка при запросе:', err);
      setError('Не удалось подключиться к серверу');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-screen">
      <div className="app-screen__card auth-screen__card">
        <h1 className="auth-screen__title">
          {isLogin ? 'Вход' : 'Регистрация'}
        </h1>
        <form onSubmit={handleSubmit} className="auth-screen__form">
          <input
            type="text"
            className="app-input auth-screen__input"
            placeholder="Имя пользователя"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            disabled={loading}
          />
          <input
            type="password"
            className="app-input auth-screen__input"
            placeholder="Пароль"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            disabled={loading}
          />
          {error && <p className="auth-screen__error">{error}</p>}
          <button
            type="submit"
            className="app-btn app-btn--primary auth-screen__submit"
            disabled={loading}
          >
            {loading ? '…' : isLogin ? 'Войти' : 'Создать аккаунт'}
          </button>
        </form>
        <div className="auth-screen__toggle">
          <button
            type="button"
            className="app-link"
            onClick={() => { setIsLogin(!isLogin); setError(null); }}
          >
            {isLogin ? 'Нет аккаунта? Зарегистрируйтесь' : 'Уже есть аккаунт? Войдите'}
          </button>
        </div>
      </div>
    </div>
  );
}
