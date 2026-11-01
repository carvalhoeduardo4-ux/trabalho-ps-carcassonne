import React, { useState, useEffect, useMemo, useRef } from 'react';
import * as api from './api/gameApi.js';
import { TurnPhase, colorById } from './ui/constants.js';
import { GameBoard } from './components/GameBoard.js';
import { TurnPanel } from './components/TurnPanel.js';
import { ScoreBoard } from './components/ScoreBoard.js';
import { SetupModal } from './components/SetupModal.js';
import { GameOverModal } from './components/GameOverModal.js';

const ROTATION_ORDER = ['DEG_0', 'DEG_90', 'DEG_180', 'DEG_270'];

const ERROR_TIMEOUT_MS = 4500;

// Motivos de clique inválido no tabuleiro (calculados em GameBoard)
const INVALID_PLACEMENT_MESSAGES = {
  occupied: 'Esta posição já está ocupada por outra peça.',
  isolated: 'A peça precisa encostar em pelo menos uma peça já colocada.',
  incompatible: 'As bordas não encaixam com as peças vizinhas (cidade com cidade, estrada com estrada, campo com campo). Gire a peça ou escolha uma posição destacada.'
};

const SCORE_NOTICE_MS = 4500;

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

export function App() {
  const [isSettingUp, setIsSettingUp] = useState(false);
  const [showRules, setShowRules] = useState(false);
  const [game, setGame] = useState(null);      // GameStateDto do backend
  const [rotation, setRotation] = useState('DEG_0');
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);
  const [scoreInfo, setScoreInfo] = useState(null); // pontuação do último turno (feedback visual)
  const gameRef = useRef(null);

  // Compra automaticamente a próxima peça quando o turno volta à fase DRAW_TILE
  const settle = async (state) => {
    let s = state;
    if (s.status === 'ACTIVE' && s.phase === TurnPhase.DRAW_TILE) s = await api.drawTile();
    setRotation('DEG_0');
    setScoreInfo(detectScoring(gameRef.current, s));
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
    const timer = setTimeout(() => setError(null), ERROR_TIMEOUT_MS);
    return () => clearTimeout(timer);
  }, [error]);

  useEffect(() => {
    if (!scoreInfo) return undefined;
    const timer = setTimeout(() => setScoreInfo(null), SCORE_NOTICE_MS);
    return () => clearTimeout(timer);
  }, [scoreInfo]);

  useEffect(() => {
    api.getState()
      .then(settle)
      .catch(() => setIsSettingUp(true));
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

  const handleStartNewGame = (playerConfigs) => {
    setIsSettingUp(false);
    run(() => api.startGame(playerConfigs));
  };

  const handleRotate = () => {
    setRotation(prev => ROTATION_ORDER[(ROTATION_ORDER.indexOf(prev) + 1) % 4]);
  };

  const handlePlaceTile = (x, y) => run(() => api.placeTile({ x, y, rotation }));

  const handleInvalidPlacement = (reason) => {
    setError(INVALID_PLACEMENT_MESSAGES[reason] || 'Posição inválida para esta peça.');
  };

  const handlePlaceMeeple = (option) => run(() => api.placeMeeple(option));

  const handleSkipMeeple = () => run(() => api.skipMeeple());

  return React.createElement('div', { className: 'app-root' }, [
    // Barra de Navegação Superior
    React.createElement('header', { key: 'navbar', className: 'app-navbar' }, [
      React.createElement('div', { className: 'nav-brand' }, [
        React.createElement('span', { className: 'brand-icon' }, '🏰'),
        React.createElement('h1', { className: 'brand-title' }, 'Carcassonne Digital'),
        React.createElement('span', { className: 'brand-subtitle' }, 'Hot-Seat Local')
      ]),
      React.createElement('div', { className: 'nav-actions' }, [
        React.createElement('button', {
          className: 'btn btn-sm btn-outline',
          onClick: () => setShowRules(true)
        }, '📖 Regras'),
        React.createElement('button', {
          className: 'btn btn-sm btn-primary',
          onClick: () => setIsSettingUp(true)
        }, '🔄 Nova Partida')
      ])
    ]),

    // Avisos flutuantes: erros/jogadas inválidas e pontuação de estruturas concluídas
    (error || scoreInfo) ? React.createElement('div', { key: 'toast-stack', className: 'toast-stack' }, [
      scoreInfo ? React.createElement('div', {
        key: 'score-toast',
        className: 'toast toast-score',
        role: 'status',
        title: 'Clique para fechar',
        onClick: () => setScoreInfo(null)
      }, `🏆 ${scoreInfo.message}`) : null,
      error ? React.createElement('div', {
        key: 'error-banner',
        className: 'toast toast-error',
        role: 'alert',
        title: 'Clique para fechar',
        onClick: () => setError(null)
      }, `⚠️ ${error}`) : null
    ]) : null,

    // Conteúdo Principal da Aplicação
    game ? React.createElement('main', { key: 'main-layout', className: 'game-layout' }, [
      // Área Central: Tabuleiro
      React.createElement('section', { key: 'board-area', className: 'board-area' }, [
        React.createElement(GameBoard, {
          board: game.board,
          meeples: game.meeples,
          validPlacements: validPlacements,
          onPlaceTile: handlePlaceTile,
          onInvalidPlacement: handleInvalidPlacement,
          scoredKeys: scoreInfo ? scoreInfo.keys : [],
          phase: game.phase,
          players: players
        })
      ]),

      // Barra Lateral: Ações do Turno e Placar
      React.createElement('aside', { key: 'sidebar-area', className: 'sidebar-area' }, [
        React.createElement(TurnPanel, {
          game: game,
          rotation: rotation,
          validPlacements: validPlacements,
          onRotate: handleRotate,
          onPlaceMeeple: handlePlaceMeeple,
          onSkipMeeple: handleSkipMeeple,
          currentPlayer: currentPlayer,
          busy: busy
        }),
        React.createElement(ScoreBoard, {
          players: players,
          currentPlayerIndex: game.currentPlayerIndex,
          gains: scoreInfo ? scoreInfo.gains : {},
          gainId: scoreInfo ? scoreInfo.id : null
        })
      ])
    ]) : (!isSettingUp ? React.createElement('div', {
      key: 'no-game-splash',
      className: 'modal-backdrop',
      style: { background: 'transparent' }
    }, [
      React.createElement('div', { className: 'modal-card', style: { textAlign: 'center', maxWidth: 420 } }, [
        React.createElement('span', { style: { fontSize: 48 } }, '🏰'),
        React.createElement('h2', { className: 'modal-title', style: { marginTop: 12 } }, 'Carcassonne Digital'),
        React.createElement('p', { className: 'modal-subtitle', style: { marginTop: 8 } },
          'Conectado ao backend. Pronto para iniciar uma nova partida hot-seat local.'
        ),
        React.createElement('div', { className: 'modal-actions', style: { justifyContent: 'center', marginTop: 24 } }, [
          React.createElement('button', {
            className: 'btn btn-primary btn-lg',
            onClick: () => setIsSettingUp(true)
          }, '⚔️ Criar Partida')
        ])
      ])
    ]) : null),

    // Modal de Configuração de Jogadores
    isSettingUp ? React.createElement(SetupModal, {
      key: 'setup-modal',
      onStartGame: handleStartNewGame,
      onCancel: game ? () => setIsSettingUp(false) : null
    }) : null,

    // Modal de Fim de Jogo
    game && game.status === 'FINISHED' ? React.createElement(GameOverModal, {
      key: 'gameover-modal',
      players: players,
      onRestart: () => setIsSettingUp(true)
    }) : null,

    // Modal de Regras Simplificadas
    showRules ? React.createElement('div', { key: 'rules-modal', className: 'modal-backdrop' }, [
      React.createElement('div', { className: 'modal-card rules-modal' }, [
        React.createElement('div', { className: 'modal-header' }, [
          React.createElement('h2', { className: 'modal-title' }, '📜 Como Jogar Carcassonne Simplificado'),
          React.createElement('button', { 
            className: 'btn-close', 
            onClick: () => setShowRules(false) 
          }, '×')
        ]),
        React.createElement('div', { className: 'rules-content' }, [
          React.createElement('h4', null, '1. Ciclo do Turno'),
          React.createElement('p', null, 'A cada turno, uma peça é sorteada do monte. Você pode girá-la em múltiplos de 90° e posicioná-la em qualquer ponto válido (destacado no grid).'),
          
          React.createElement('h4', null, '2. Encaixe Válido'),
          React.createElement('p', null, 'A nova peça deve tocar ao menos uma peça já colocada. Todas as bordas em contato devem ser do mesmo tipo: Cidade com Cidade, Estrada com Estrada, Campo com Campo.'),
          
          React.createElement('h4', null, '3. Colocação de Meeples'),
          React.createElement('p', null, 'Após colocar a peça, você pode (opcionalmente) colocar 1 Meeple em um segmento dela (Estrada, Cidade ou Mosteiro). Não é permitido colocar meeple em uma estrutura que já pertença a outro jogador ou tenha meeple conectado!'),
          
          React.createElement('h4', null, '4. Pontuação e Fechamento'),
          React.createElement('ul', null, [
            React.createElement('li', { key: 'r1' }, '🛣️ Estrada Fechada: 1 ponto por peça.'),
            React.createElement('li', { key: 'r2' }, '🏰 Cidade Fechada: 2 pontos por peça.'),
            React.createElement('li', { key: 'r3' }, '⛪ Mosteiro Concluído (8 vizinhos em volta): 9 pontos.'),
            React.createElement('li', { key: 'r4' }, 'Ao fechar a estrutura, o jogador com a maioria de meeples leva os pontos e os meeples retornam para o estoque.')
          ]),

          React.createElement('h4', null, '5. Fim de Jogo'),
          React.createElement('p', null, 'Quando o monte zerar, as estruturas incompletas são pontuadas e o jogador com mais pontos vence!')
        ]),
        React.createElement('div', { className: 'modal-actions' }, [
          React.createElement('button', {
            className: 'btn btn-primary',
            onClick: () => setShowRules(false)
          }, 'Entendido!')
        ])
      ])
    ]) : null
  ]);
}
