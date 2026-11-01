import React, { useState, useEffect, useMemo, useRef } from 'react';
import * as api from './api/gameApi.js';
import { TurnPhase, ROTATIONS, colorById } from './ui/constants.js';
import { toRenderTile } from './ui/tileAdapter.js';
import { GameBoard } from './components/GameBoard.js';
import { TurnPanel } from './components/TurnPanel.js';
import { ScoreBoard } from './components/ScoreBoard.js';
import { SetupModal } from './components/SetupModal.js';
import { GameOverModal } from './components/GameOverModal.js';
import { RulesModal } from './components/RulesModal.js';

const ERROR_TIMEOUT_MS = 4500;
const ERROR_MS_PER_CHAR = 60; // tempo de leitura proporcional ao tamanho da mensagem

// Motivos de clique inválido no tabuleiro (calculados em GameBoard)
const INVALID_PLACEMENT_MESSAGES = {
  isolated: 'A peça precisa encostar em pelo menos uma peça já colocada.',
  incompatible: 'As bordas não encaixam com as peças vizinhas (cidade com cidade, estrada com estrada, campo com campo). Gire a peça ou escolha uma posição destacada.'
};

const SCORE_NOTICE_MS = 4500;

const TURN_NOTICE_MS = 2500;

const STRUCTURE_LABELS = { CITY: 'Cidade', ROAD: 'Estrada', MONASTERY: 'Mosteiro', FIELD: 'Campo' };

const meepleKey = (m) => `${m.x},${m.y},${m.featureId},${m.playerIndex}`;

/**
 * Compara o estado anterior com o novo e descreve a pontuação do turno:
 * quem ganhou pontos, quais estruturas foram concluídas (meeples que saíram do
 * tabuleiro) e em quais peças. Devolve null se ninguém pontuou.
 */
function detectScoring(prev, next) {
  if (!prev || prev.players.length !== next.players.length || next.board.length < prev.board.length) return null;
  const gains = {};
  next.players.forEach((p, i) => {
    const delta = p.score - prev.players[i].score;
    if (delta > 0) gains[i] = delta;
  });
  const winners = Object.keys(gains).map(Number);
  if (winners.length === 0) return null;

  const stillThere = new Set(next.meeples.map(meepleKey));
  const removed = prev.meeples.filter(m => !stillThere.has(meepleKey(m)));
  const types = new Set();
  const keys = new Set();
  removed.forEach(m => {
    keys.add(`${m.x},${m.y}`);
    const tile = next.board.find(t => t.x === m.x && t.y === m.y);
    const feature = tile && tile.features.find(f => f.id === m.featureId);
    if (feature) types.add(STRUCTURE_LABELS[feature.type] || 'Estrutura');
  });

  const what = types.size > 0 ? [...types].join(' e ') : 'Estrutura';
  const plural = types.size > 1 ? 'concluídas' : 'concluída';
  const who = winners.map(i => `${next.players[i].name} +${gains[i]} pts`).join(' · ');
  const tie = winners.length > 1 ? ' (empate: todos pontuam)' : '';
  return {
    id: Date.now(),
    gains,
    keys: [...keys],
    message: `${what} ${plural}! ${who}${tie}`
  };
}

/** Troca mensagens técnicas do backend por textos que o jogador entende. */
function friendlyError(message) {
  if (/inválida na fase/i.test(message)) {
    return 'Essa ação não está disponível nesta etapa do turno.';
  }
  return message;
}

/** Rotação inicial da peça: 0° se couber, senão a primeira rotação que tenha posição válida. */
function initialRotation(state) {
  const placements = state.placements || [];
  if (placements.length === 0 || placements.some(p => p.rotation === 'DEG_0')) return 'DEG_0';
  return ROTATIONS.find(r => placements.some(p => p.rotation === r)) || 'DEG_0';
}

export function App() {
  const [isSettingUp, setIsSettingUp] = useState(false);
  const [showRules, setShowRules] = useState(false);
  const [game, setGame] = useState(null);      // GameStateDto do backend
  const [connection, setConnection] = useState('loading'); // 'loading' | 'ok' | 'offline'
  const [rotation, setRotation] = useState('DEG_0');
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);
  const [scoreInfo, setScoreInfo] = useState(null); // pontuação do último turno (feedback visual)
  const [turnNotice, setTurnNotice] = useState(null); // aviso de troca de turno (RNF02)
  const [lastPlacedKey, setLastPlacedKey] = useState(null);
  const [meeplePreview, setMeeplePreview] = useState(null);
  const [showCoords, setShowCoords] = useState(false);
  const [gameOverDismissed, setGameOverDismissed] = useState(false);
  const gameRef = useRef(null);

  // Compra automaticamente a próxima peça quando o turno volta à fase DRAW_TILE
  const settle = async (state) => {
    let s = state;
    if (s.status === 'ACTIVE' && s.phase === TurnPhase.DRAW_TILE) s = await api.drawTile();
    const prev = gameRef.current;
    if (s.status === 'ACTIVE' && (!prev || prev.currentPlayerIndex !== s.currentPlayerIndex)) {
      const p = s.players[s.currentPlayerIndex];
      setTurnNotice({ id: Date.now(), name: p.name, color: colorById(p.color).hex });
    }
    // A peça recém-colocada continua destacada até a próxima jogada
    if (s.placedTile) setLastPlacedKey(`${s.placedTile.x},${s.placedTile.y}`);
    setRotation(initialRotation(s));
    setScoreInfo(detectScoring(prev, s));
    setMeeplePreview(null);
    gameRef.current = s;
    setGame(s);
  };

  const inFlight = useRef(false);

  const run = async (action) => {
    if (inFlight.current) return; // ignora cliques repetidos enquanto uma ação está em andamento
    inFlight.current = true;
    setBusy(true);
    setError(null);
    try {
      await settle(await action());
    } catch (e) {
      setError(friendlyError(e.message));
    } finally {
      inFlight.current = false;
      setBusy(false);
    }
  };

  // Avisos somem sozinhos
  useEffect(() => {
    if (!error) return undefined;
    const timer = setTimeout(() => setError(null), Math.max(ERROR_TIMEOUT_MS, error.length * ERROR_MS_PER_CHAR));
    return () => clearTimeout(timer);
  }, [error]);

  useEffect(() => {
    if (!scoreInfo) return undefined;
    const timer = setTimeout(() => setScoreInfo(null), SCORE_NOTICE_MS);
    return () => clearTimeout(timer);
  }, [scoreInfo]);

  useEffect(() => {
    if (!turnNotice) return undefined;
    const timer = setTimeout(() => setTurnNotice(null), TURN_NOTICE_MS);
    return () => clearTimeout(timer);
  }, [turnNotice]);

  const loadGame = () => {
    setConnection('loading');
    api.getState()
      .then((s) => { setConnection('ok'); return settle(s); })
      .catch((e) => {
        if (e.offline) {
          setConnection('offline');
        } else {
          setConnection('ok'); // servidor respondeu, só não há partida
          setIsSettingUp(true);
        }
      });
  };

  useEffect(() => {
    loadGame();
  }, []);

  const players = useMemo(
    () => (game ? game.players.map(p => ({ ...p, color: colorById(p.color) })) : []),
    [game]
  );
  const currentPlayer = players[game ? game.currentPlayerIndex : 0];

  // Posições válidas apenas para a rotação escolhida localmente
  const validPlacements = useMemo(
    () => (game ? game.placements.filter(p => p.rotation === rotation) : []),
    [game, rotation]
  );

  // Peça sorteada já girada, para a pré-visualização no tabuleiro
  const previewTile = useMemo(
    () => (game && game.drawnTile ? toRenderTile(game.drawnTile.features, rotation) : null),
    [game, rotation]
  );

  const handleStartNewGame = (playerConfigs) => {
    setIsSettingUp(false);
    setLastPlacedKey(null);
    setGameOverDismissed(false);
    gameRef.current = null;
    run(() => api.startGame(playerConfigs));
  };

  const handleRotate = (step = 1) => {
    setRotation(prev => ROTATIONS[(ROTATIONS.indexOf(prev) + step + 4) % 4]);
  };

  const handlePlaceTile = (x, y) => run(() => api.placeTile({ x, y, rotation }));

  const handleInvalidPlacement = (reason) => {
    if (reason === 'occupied') return; // a célula treme; um aviso aqui só atrapalharia
    setError(INVALID_PLACEMENT_MESSAGES[reason] || 'Posição inválida para esta peça.');
  };

  const handlePlaceMeeple = (option) => run(() => api.placeMeeple(option));

  const handleSkipMeeple = () => run(() => api.skipMeeple());

  /** Tela exibida quando não há partida carregada. */
  const renderSplash = () => {
    const content = {
      loading: { icon: '🏰', title: 'Carcassonne Digital', text: 'Carregando partida…' },
      offline: {
        icon: '⚠️',
        title: 'Servidor indisponível',
        text: 'Não foi possível falar com o servidor do jogo. Verifique se o backend está rodando e tente de novo.',
        action: { label: 'Tentar novamente', onClick: loadGame }
      },
      ok: {
        icon: '🏰',
        title: 'Carcassonne Digital',
        text: 'Monte seu grupo de 2 a 5 jogadores e comece uma partida neste dispositivo.',
        action: { label: 'Criar partida', onClick: () => setIsSettingUp(true) }
      }
    }[connection];

    return React.createElement('div', { key: 'no-game-splash', className: 'splash' },
      React.createElement('div', { className: 'modal-card splash-card' },
        React.createElement('span', { className: 'splash-icon', 'aria-hidden': 'true' }, content.icon),
        React.createElement('h2', { className: 'modal-title' }, content.title),
        React.createElement('p', { className: 'modal-subtitle', role: 'status' }, content.text),
        content.action ? React.createElement('div', { className: 'modal-actions' },
          React.createElement('button', {
            type: 'button',
            className: 'btn btn-primary btn-lg',
            onClick: content.action.onClick
          }, content.action.label)
        ) : null
      )
    );
  };

  const isFinished = game && game.status === 'FINISHED';

  return React.createElement('div', { className: 'app-root' },
    // Barra de Navegação Superior
    React.createElement('header', { className: 'app-navbar' },
      React.createElement('div', { className: 'nav-brand' },
        React.createElement('span', { className: 'brand-icon', 'aria-hidden': 'true' }, '🏰'),
        React.createElement('h1', { className: 'brand-title' }, 'Carcassonne Digital'),
        React.createElement('span', { className: 'brand-subtitle' }, 'No mesmo dispositivo')
      ),
      React.createElement('div', { className: 'nav-actions' },
        React.createElement('button', {
          type: 'button',
          className: 'btn btn-sm btn-outline',
          onClick: () => setShowRules(true)
        },
          React.createElement('span', { 'aria-hidden': 'true' }, '📖'),
          'Regras'
        ),
        React.createElement('button', {
          type: 'button',
          className: 'btn btn-sm btn-outline',
          onClick: () => setIsSettingUp(true)
        },
          React.createElement('span', { 'aria-hidden': 'true' }, '🔄'),
          'Nova partida'
        )
      )
    ),

    // Anúncios para leitores de tela (troca de turno)
    React.createElement('div', { className: 'sr-only', 'aria-live': 'polite' },
      turnNotice ? `Vez de ${turnNotice.name}.` : ''
    ),

    // Avisos flutuantes: erros/jogadas inválidas e pontuação de estruturas concluídas
    (error || scoreInfo) ? React.createElement('div', { className: 'toast-stack' },
      scoreInfo ? React.createElement('div', {
        key: 'score-toast',
        className: 'toast toast-score',
        role: 'status'
      },
        React.createElement('span', { className: 'toast-text' },
          React.createElement('span', { 'aria-hidden': 'true' }, '🏆 '),
          scoreInfo.message
        ),
        React.createElement('button', {
          type: 'button',
          className: 'toast-close',
          'aria-label': 'Fechar aviso',
          onClick: () => setScoreInfo(null)
        }, '×')
      ) : null,
      error ? React.createElement('div', {
        key: 'error-banner',
        className: 'toast toast-error',
        role: 'alert'
      },
        React.createElement('span', { className: 'toast-text' },
          React.createElement('span', { 'aria-hidden': 'true' }, '⚠️ '),
          error
        ),
        React.createElement('button', {
          type: 'button',
          className: 'toast-close',
          'aria-label': 'Fechar aviso',
          onClick: () => setError(null)
        }, '×')
      ) : null
    ) : null,

    // Conteúdo Principal da Aplicação
    game ? React.createElement('main', {
      key: 'main-layout',
      className: `game-layout${busy ? ' is-busy' : ''}`,
      'aria-busy': busy
    },
      // Área Central: Tabuleiro
      React.createElement('section', { className: 'board-area' },
        React.createElement(GameBoard, {
          board: game.board,
          meeples: game.meeples,
          validPlacements: validPlacements,
          onPlaceTile: handlePlaceTile,
          onInvalidPlacement: handleInvalidPlacement,
          scoredKeys: scoreInfo ? scoreInfo.keys : [],
          phase: game.phase,
          players: players,
          previewTile: previewTile,
          lastPlacedKey: lastPlacedKey,
          meeplePreview: meeplePreview,
          currentPlayerIndex: game.currentPlayerIndex,
          showCoords: showCoords,
          onToggleCoords: () => setShowCoords(v => !v),
          turnNotice: turnNotice
        })
      ),

      // Barra Lateral: Ações do Turno e Placar
      React.createElement('aside', { className: 'sidebar-area' },
        React.createElement(TurnPanel, {
          game: game,
          rotation: rotation,
          validPlacements: validPlacements,
          onRotate: handleRotate,
          onSetRotation: setRotation,
          onPlaceMeeple: handlePlaceMeeple,
          onSkipMeeple: handleSkipMeeple,
          onPreviewMeeple: setMeeplePreview,
          onShowResult: () => setGameOverDismissed(false),
          currentPlayer: currentPlayer,
          busy: busy
        }),
        React.createElement(ScoreBoard, {
          players: players,
          currentPlayerIndex: game.currentPlayerIndex,
          gains: scoreInfo ? scoreInfo.gains : {},
          gainId: scoreInfo ? scoreInfo.id : null
        })
      )
    ) : (!isSettingUp ? renderSplash() : null),

    // Modal de Configuração de Jogadores
    isSettingUp ? React.createElement(SetupModal, {
      key: 'setup-modal',
      onStartGame: handleStartNewGame,
      onCancel: game ? () => setIsSettingUp(false) : null,
      hasGameInProgress: !!game && !isFinished
    }) : null,

    // Modal de Fim de Jogo
    isFinished && !gameOverDismissed && !isSettingUp ? React.createElement(GameOverModal, {
      key: 'gameover-modal',
      players: players,
      onRestart: () => setIsSettingUp(true),
      onClose: () => setGameOverDismissed(true)
    }) : null,

    // Modal de Regras Simplificadas
    showRules ? React.createElement(RulesModal, { key: 'rules-modal', onClose: () => setShowRules(false) }) : null
  );
}
