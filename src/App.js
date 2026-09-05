import React, { useState } from 'react';
import { 
  createInitialGameState, 
  rotateCurrentTile, 
  placeCurrentTile, 
  placeMeeple, 
  skipMeeple 
} from './core/gameEngine.js';
import { GameBoard } from './components/GameBoard.js';
import { TurnPanel } from './components/TurnPanel.js';
import { ScoreBoard } from './components/ScoreBoard.js';
import { SetupModal } from './components/SetupModal.js';
import { GameOverModal } from './components/GameOverModal.js';

export function App() {
  const [isSettingUp, setIsSettingUp] = useState(false);
  const [showRules, setShowRules] = useState(false);
  const [gameState, setGameState] = useState(() => createInitialGameState());

  const currentPlayer = gameState.players[gameState.currentPlayerIndex] || gameState.players[0];

  const handleStartNewGame = (playerConfigs) => {
    const newState = createInitialGameState(playerConfigs);
    setGameState(newState);
    setIsSettingUp(false);
  };

  const handleRotate = () => {
    setGameState(prev => rotateCurrentTile(prev));
  };

  const handlePlaceTile = (x, y) => {
    setGameState(prev => placeCurrentTile(prev, x, y));
  };

  const handlePlaceMeeple = (segment) => {
    setGameState(prev => placeMeeple(prev, segment));
  };

  const handleSkipMeeple = () => {
    setGameState(prev => skipMeeple(prev));
  };

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

    // Conteúdo Principal da Aplicação
    React.createElement('main', { key: 'main-layout', className: 'game-layout' }, [
      // Área Central: Tabuleiro
      React.createElement('section', { key: 'board-area', className: 'board-area' }, [
        React.createElement(GameBoard, {
          board: gameState.board,
          validPlacements: gameState.validPlacements,
          onPlaceTile: handlePlaceTile,
          currentTile: gameState.currentTile,
          phase: gameState.phase,
          players: gameState.players
        })
      ]),

      // Barra Lateral: Ações do Turno e Placar
      React.createElement('aside', { key: 'sidebar-area', className: 'sidebar-area' }, [
        React.createElement(TurnPanel, {
          gameState: gameState,
          onRotate: handleRotate,
          onPlaceMeeple: handlePlaceMeeple,
          onSkipMeeple: handleSkipMeeple,
          currentPlayer: currentPlayer
        }),
        React.createElement(ScoreBoard, {
          players: gameState.players,
          currentPlayerIndex: gameState.currentPlayerIndex,
          history: gameState.history
        })
      ])
    ]),

    // Modal de Configuração de Jogadores
    isSettingUp ? React.createElement(SetupModal, {
      key: 'setup-modal',
      onStartGame: handleStartNewGame
    }) : null,

    // Modal de Fim de Jogo
    gameState.gameOver ? React.createElement(GameOverModal, {
      key: 'gameover-modal',
      players: gameState.players,
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
