import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL;

export default function MainScreen() {
  const navigate = useNavigate();
  const [bots, setBots] = useState([]);
  const [missions, setMissions] = useState([]);
  const [selectedBotIds, setSelectedBotIds] = useState([]);
  const [selectedMissionId, setSelectedMissionId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isGameLoading, setIsGameLoading] = useState(false);
  const [startError, setStartError] = useState(null);
  const userToken = localStorage.getItem('token');

  useEffect(() => {
    const init = async () => {
      try {
        await axios.get(API_URL + '/api/main', {
          headers: { Authorization: userToken },
        });
        const [botsRes, missionsRes] = await Promise.all([
          axios.get(API_URL + '/api/main/bots', {
            headers: { Authorization: userToken },
          }),
          axios.get(API_URL + '/api/main/missions', {
            headers: { Authorization: userToken },
          }),
        ]);
        setBots(botsRes.data ?? []);
        setMissions(missionsRes.data ?? []);
      } catch (err) {
        console.error('Ошибка при инициализации или загрузке данных', err);
      } finally {
        setLoading(false);
      }
    };

    if (userToken) init();
  }, [userToken]);

  const toggleBot = (id) => {
    setSelectedBotIds((prev) => {
      if (prev.includes(id)) return prev.filter((x) => x !== id);
      if (prev.length >= 2) return prev;
      return [...prev, id];
    });
  };

  const startGame = async () => {
    if (selectedBotIds.length !== 2 || !selectedMissionId) return;
    try {
      setStartError(null);
      setIsGameLoading(true);
      const res = await axios.post(API_URL + '/api/game/start', {
        userId: userToken,
        selectedBotIds,
        missionId: selectedMissionId,
      });
      navigate('/game', {
        state: {
          sessionId: res.data.sessionId,
          bots: res.data.bots,
          userId: userToken,
          questionsLeft: res.data.questionsLeft,
        },
      });
    } catch (e) {
      console.error('Ошибка старта игры', e);
      setStartError('Не удалось начать игру');
      setIsGameLoading(false);
    }
  };

  const botsReady = selectedBotIds.length === 2;
  const canStart = botsReady && !!selectedMissionId && !isGameLoading;

  if (loading) {
    return (
      <div className="main-screen">
        <div className="main-screen__loading app-pulse">Загрузка…</div>
      </div>
    );
  }

  return (
    <div className="main-screen">
      <h1 className="main-screen__title">Exposure</h1>
      <p className="main-screen__subtitle">
        Choose two opponents ({selectedBotIds.length}/2)
      </p>

      <ul className="main-screen__bot-list">
        {bots.map((bot) => {
          const sel = selectedBotIds.includes(bot.id);
          return (
            <li key={bot.id}>
              <button
                type="button"
                onClick={() => !isGameLoading && toggleBot(bot.id)}
                className={`main-screen__bot-item ${sel ? 'main-screen__bot-item--selected' : ''} ${isGameLoading ? 'main-screen__bot-item--disabled' : ''}`}
              >
                <span className="main-screen__bot-icon">🤖</span>
                {bot.name}
              </button>
            </li>
          );
        })}
      </ul>

      {botsReady && (
        <div className="main-screen__missions-wrap">
          <p className="main-screen__missions-label">Select mission</p>
          {missions.length === 0 ? (
            <p className="main-screen__missions-empty">No missions available</p>
          ) : (
            <ul className="main-screen__mission-list">
              {missions.map((m) => (
                <li key={m.id}>
                  <button
                    type="button"
                    onClick={() => !isGameLoading && setSelectedMissionId(m.id)}
                    className={`main-screen__mission-item ${selectedMissionId === m.id ? 'main-screen__mission-item--selected' : ''} ${isGameLoading ? 'main-screen__mission-item--disabled' : ''}`}
                  >
                    {m.title}
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      {startError && <p className="main-screen__error">{startError}</p>}
      <button
        type="button"
        className="app-btn app-btn--primary main-screen__play"
        onClick={startGame}
        disabled={!canStart}
      >
        {isGameLoading ? 'Loading…' : 'Play'}
      </button>
    </div>
  );
}
