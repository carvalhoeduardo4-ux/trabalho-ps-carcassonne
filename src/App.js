import React, { useState, useEffect, useMemo } from 'react';
import * as api from './api/gameApi.js';
import { TurnPhase, colorById } from './ui/constants.js';
import { GameBoard } from './components/GameBoard.js';
import { TurnPanel } from './components/TurnPanel.js';
import { ScoreBoard } from './components/ScoreBoard.js';
import { SetupModal } from './components/SetupModal.js';
import { GameOverModal } from './components/GameOverModal.js';

const ROTATION_ORDER = ['DEG_0', 'DEG_90', 'DEG_180', 'DEG_270'];

export function App() {
  const [isSettingUp, setIsSettingUp] = useState(false);
  const [showRules, setShowRules] = useState(false);
  const [game, setGame] = useState(null);      // GameStateDto do backend
  const [rotation, setRotation] = useState('DEG_0');
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  // Compra automaticamente a próxima peça quando o turno volta à fase DRAW_TILE
  const settle = async (state) => {
    let s = state;
    if (s.status === 'ACTIVE' && s.phase === TurnPhase.DRAW_TILE) s = await api.drawTile();
    setRotation('DEG_0');
    setGame(s);
  };

  const run = async (action) => {
    setBusy(true);
    setError(null);
    try {
      await settle(await action());
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  };

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

    // Mensagem de erro retornada pelo backend
    error ? React.createElement('div', {
      key: 'error-banner',
      className: 'badge badge-action',
      role: 'alert',
      style: { margin: '8px 16px', cursor: 'pointer' },
      onClick: () => setError(null)
    }, `⚠️ ${error}`) : null,

    // Conteúdo Principal da Aplicação
    game ? React.createElement('main', { key: 'main-layout', className: 'game-layout' }, [
      // Área Central: Tabuleiro
      React.createElement('section', { key: 'board-area', className: 'board-area' }, [
        React.createElement(GameBoard, {
          board: game.board,
          meeples: game.meeples,
          validPlacements: validPlacements,
          onPlaceTile: handlePlaceTile,
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
          currentPlayerIndex: game.currentPlayerIndex
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
