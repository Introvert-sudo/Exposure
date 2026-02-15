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
      <h1 className="result-screen__title">Game results</h1>

      <div className="app-screen__card result-screen__card">
        {isLiar ? (
          <>
            <h2 className="result-screen__heading result-screen__heading--success">
              You have won!
            </h2>
            <p className="result-screen__text">
              You've successfully exposed the liar. Your intuition was spot on!
            </p>
          </>
        ) : (
          <>
            <h2 className="result-screen__heading result-screen__heading--error">
              Wrong.
            </h2>
            <p className="result-screen__text">
              This bot was telling the truth. The real liar remained in the shadows...
            </p>
          </>
        )}
      </div>

      <button
        type="button"
        className="app-btn app-btn--primary result-screen__home"
        onClick={() => navigate('/')}
      >
        Return to home
      </button>
    </div>
  );
}
