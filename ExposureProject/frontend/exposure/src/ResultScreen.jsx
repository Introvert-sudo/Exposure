import { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

export default function ResultScreen() {
  const { state } = useLocation();
  const navigate = useNavigate();
  const isLiar = state?.isLiar;
  const hasResult = state != null && typeof isLiar === 'boolean';

  useEffect(() => {
    if (!hasResult) navigate('/', { replace: true });
  }, [hasResult, navigate]);

  if (!hasResult) return null;

  return (
    <div className="result-screen">
      <h1 className="result-screen__title">Результаты игры</h1>

      <div className="app-screen__card result-screen__card">
        {isLiar ? (
          <>
            <h2 className="result-screen__heading result-screen__heading--success">
              🎉 Вы победили!
            </h2>
            <p className="result-screen__text">
              Вы успешно разоблачили лжеца. Ваша интуиция вас не подвела!
            </p>
          </>
        ) : (
          <>
            <h2 className="result-screen__heading result-screen__heading--error">
              💀 Ошибка
            </h2>
            <p className="result-screen__text">
              Этот бот говорил правду. Настоящий лжец остался в тени…
            </p>
          </>
        )}
      </div>

      <button
        type="button"
        className="app-btn app-btn--primary result-screen__home"
        onClick={() => navigate('/')}
      >
        Вернуться на главную
      </button>
    </div>
  );
}
