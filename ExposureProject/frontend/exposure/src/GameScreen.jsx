import { useState, useEffect, useRef, useCallback } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

const API_URL = import.meta.env.VITE_API_URL;

export default function GameScreen() {
  const { state } = useLocation();
  const navigate = useNavigate();
  const messagesEndRef = useRef(null);
  const leftTabRef = useRef(null);
  const rightTabRef = useRef(null);

  const [status, setStatus] = useState('GENERATING');
  const [mission, setMission] = useState(null);
  const [questionsLeft, setQuestionsLeft] = useState(null);
  const [selectedBot, setSelectedBot] = useState(null);
  const [chats, setChats] = useState({});
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [askError, setAskError] = useState(null);
  const [leftOpen, setLeftOpen] = useState(true);
  const [rightOpen, setRightOpen] = useState(true);
  const [isNarrow, setIsNarrow] = useState(false);

  const sessionId = state?.sessionId;
  const userId = state?.userId;
  const bots = state?.bots ?? [];

  // Redirect if no game state (e.g. refresh on /game); otherwise init suspects and chats
  useEffect(() => {
    if (!state?.sessionId || !state?.userId || !state?.bots?.length) {
      navigate('/', { replace: true });
      return;
    }
    const b = state.bots;
    setSelectedBot(b[0]);
    const initial = {};
    b.forEach((bot) => { initial[bot.id] = []; });
    setChats(initial);
  }, [state?.sessionId, state?.userId, state?.bots, navigate]);

  // Track narrow viewport; default sidebars collapsed on mobile (only on init)
  useEffect(() => {
    const mq = window.matchMedia('(max-width: 768px)');
    const apply = (initial) => {
      const narrow = mq.matches;
      setIsNarrow(narrow);
      if (initial && narrow) {
        setLeftOpen(false);
        setRightOpen(false);
      }
    };
    apply(true);
    const handler = () => apply(false);
    mq.addEventListener('change', handler);
    return () => mq.removeEventListener('change', handler);
  }, []);

  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, []);
  useEffect(() => scrollToBottom, [chats]);

  // Move focus to expand tab after collapsing (avoids aria-hidden + focused descendant)
  useEffect(() => {
    if (!leftOpen) leftTabRef.current?.focus();
  }, [leftOpen]);
  useEffect(() => {
    if (!rightOpen) rightTabRef.current?.focus();
  }, [rightOpen]);

  // Load mission and poll status
  useEffect(() => {
    if (!sessionId) return;

    const loadMission = async () => {
      try {
        const res = await fetch(`${API_URL}/api/game/mission/${sessionId}`);
        const data = await res.json();
        setMission(data);
        setQuestionsLeft(data.initialQuestionsAmount);
      } catch (e) {
        console.error('Ошибка загрузки миссии', e);
      }
    };

    loadMission();

    const interval = setInterval(async () => {
      try {
        const res = await fetch(`${API_URL}/api/game/status/${sessionId}`);
        if (!res.ok) return;
        const data = await res.json();
        if (data.status === 'READY') {
          setStatus('READY');
          clearInterval(interval);
        }
      } catch (e) {
        console.error('Ошибка поллинга', e);
      }
    }, 3000);

    return () => clearInterval(interval);
  }, [sessionId]);

  const askQuestion = async () => {
    const q = input?.trim();
    if (!q || (questionsLeft != null && questionsLeft <= 0) || loading || status !== 'READY' || !selectedBot) return;

    setLoading(true);
    setAskError(null);
    try {
      const res = await fetch(`${API_URL}/api/game/question`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId,
          sessionId,
          botId: selectedBot.id,
          question: q,
        }),
      });
      if (!res.ok) throw new Error('Не удалось отправить вопрос');
      const data = await res.json();

      setChats((prev) => ({
        ...prev,
        [selectedBot.id]: [...(prev[selectedBot.id] || []), { q, a: data.answer }],
      }));
      setQuestionsLeft(data.questionsLeft);
      setInput('');
    } catch (e) {
      setAskError(e.message || 'Ошибка');
    } finally {
      setLoading(false);
    }
  };

  const accuse = async (botId) => {
    try {
      const res = await fetch(`${API_URL}/api/game/choice`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId, sessionId, botId }),
      });
      const data = await res.json();
      navigate('/results', { state: { isLiar: data.isLiar, botId } });
    } catch (e) {
      console.error(e);
    }
  };

  if (!state?.sessionId || !bots.length) {
    return null;
  }

  const totalQuestions = mission?.initialQuestionsAmount ?? state.questionsLeft ?? 0;
  const displayQuestionsLeft = questionsLeft ?? totalQuestions;
  const canAsk = status === 'READY' && displayQuestionsLeft > 0 && !loading;
  const showAccuse = status === 'READY';

  const collapseLeft = () => {
    if (document.activeElement?.closest('.game-screen__suspects')) document.activeElement?.blur();
    setLeftOpen(false);
  };
  const collapseRight = () => {
    if (document.activeElement?.closest('.game-screen__mission')) document.activeElement?.blur();
    setRightOpen(false);
  };

  return (
    <div className="game-screen">
      {isNarrow && (leftOpen || rightOpen) && (
        <div
          className="game-screen__overlay"
          role="button"
          tabIndex={0}
          aria-label="Закрыть панели"
          onClick={() => { setLeftOpen(false); setRightOpen(false); }}
          onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { setLeftOpen(false); setRightOpen(false); } }}
        />
      )}

      {/* Edge tab: open left panel when collapsed */}
      {!leftOpen && (
        <button
          ref={leftTabRef}
          type="button"
          className="game-screen__edge-tab game-screen__edge-tab--left"
          onClick={() => setLeftOpen(true)}
          aria-label="Открыть подозреваемых"
          title="Подозреваемые"
        >
          ▶
        </button>
      )}

      <aside
        className={`game-screen__suspects ${!leftOpen ? 'game-screen__aside--collapsed' : ''}`}
      >
        <div className="game-screen__aside-inner">
          <div className="game-screen__aside-head">
            <h3 className="game-screen__aside-title">Подозреваемые</h3>
            <button
              type="button"
              className="game-screen__aside-toggle"
              onClick={collapseLeft}
              aria-label="Свернуть"
            >
              ◀
            </button>
          </div>
          {bots.map((bot) => (
            <button
              key={bot.id}
              type="button"
              onClick={() => setSelectedBot(bot)}
              className={`game-screen__suspect-btn ${selectedBot?.id === bot.id ? 'game-screen__suspect-btn--active' : ''}`}
            >
              {bot.name}
            </button>
          ))}
          <div className="game-screen__questions-left">
            <span className="game-screen__questions-label">Осталось вопросов</span>
            <span className="game-screen__questions-value">
              {displayQuestionsLeft}
              {totalQuestions > 0 && ` / ${totalQuestions}`}
            </span>
          </div>
        </div>
      </aside>

      <main className="game-screen__main">
        <header className="game-screen__header">
          <span className="game-screen__header-title">
            Допрос: <strong>{selectedBot?.name ?? '—'}</strong>
          </span>
          <div className="game-screen__header-actions">
            {status === 'GENERATING' && (
              <span className="game-screen__status-badge">Формирование сценария…</span>
            )}
            <button
              type="button"
              className="btn btn-outline-secondary btn-sm game-screen__exit-btn"
              onClick={() => navigate('/', { replace: true })}
            >
              Выйти
            </button>
          </div>
        </header>

        <div className="game-screen__chat">
          {(chats[selectedBot?.id] || []).length > 0 ? (
            <div className="game-screen__messages">
              {(chats[selectedBot?.id] || []).map((msg, i) => (
                <div key={i} className="game-screen__msg-pair">
                  <div className="game-screen__bubble game-screen__bubble--user">{msg.q}</div>
                  <div className="game-screen__bubble game-screen__bubble--bot">{msg.a}</div>
                </div>
              ))}
            </div>
          ) : (
            <div className="game-screen__empty">
              {selectedBot?.name} ожидает первого вопроса. Задайте его в форме ниже.
            </div>
          )}
          {loading && <div className="game-screen__loading-msg">Отправка…</div>}
          {askError && <div className="game-screen__error-msg">{askError}</div>}
          <div ref={messagesEndRef} />
        </div>

        <footer className="game-screen__footer">
          {status !== 'READY' && (
            <p className="game-screen__footer-hint">ИИ ещё не готов. Подождите.</p>
          )}
          {status === 'READY' && (
            <div className="game-screen__footer-ready">
              {canAsk && (
                <div className="game-screen__ask-row">
                  <input
                    type="text"
                    className="form-control game-screen__input"
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && askQuestion()}
                    placeholder="Введите вопрос и нажмите Enter или «Спросить»"
                    disabled={loading}
                  />
                  <button
                    type="button"
                    className="btn btn-primary game-screen__ask-btn"
                    onClick={askQuestion}
                    disabled={loading || !input.trim()}
                  >
                    Спросить
                  </button>
                </div>
              )}
              {showAccuse && (
                <div className="game-screen__accuse-block">
                  <p className="game-screen__accuse-hint">
                    {displayQuestionsLeft > 0
                      ? 'Можно обвинить в любой момент или задать ещё вопросов.'
                      : 'Вопросы исчерпаны. Выберите подозреваемого и обвините.'}
                  </p>
                  <button
                    type="button"
                    className="btn btn-danger game-screen__accuse-btn"
                    onClick={() => accuse(selectedBot?.id)}
                    disabled={!selectedBot}
                  >
                    Обвинить {selectedBot?.name ?? 'выбранного'}
                  </button>
                </div>
              )}
            </div>
          )}
        </footer>
      </main>

      {/* Edge tab: open right panel when collapsed */}
      {!rightOpen && (
        <button
          ref={rightTabRef}
          type="button"
          className="game-screen__edge-tab game-screen__edge-tab--right"
          onClick={() => setRightOpen(true)}
          aria-label="Открыть дело"
          title="Дело"
        >
          ◀
        </button>
      )}

      <aside
        className={`game-screen__mission ${!rightOpen ? 'game-screen__aside--collapsed' : ''}`}
      >
        <div className="game-screen__aside-inner">
          <div className="game-screen__aside-head">
            <h3 className="game-screen__aside-title">{mission?.title ?? 'Загрузка дела…'}</h3>
            <button
              type="button"
              className="game-screen__aside-toggle"
              onClick={collapseRight}
              aria-label="Свернуть"
            >
              ▶
            </button>
          </div>
          {mission ? (
            <div className="game-screen__mission-body">
              <p className="game-screen__mission-desc">{mission.description}</p>
              <p className="game-screen__mission-note">Один из них лжёт. Остальные говорят правду — по-своему.</p>
            </div>
          ) : (
            <div className="game-screen__mission-loading">Анализ материалов…</div>
          )}
        </div>
      </aside>
    </div>
  );
}
