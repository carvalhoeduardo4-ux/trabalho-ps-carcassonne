import React from 'react';
import { TurnPhase } from '../core/types.js';
import { TileSvg, MeepleIcon } from './TileSvg.js';

export function TurnPanel({
  gameState,
  onRotate,
  onPlaceMeeple,
  onSkipMeeple,
  currentPlayer
}) {
  const { 
    phase, 
    currentTile, 
    currentTileRotation, 
    deck, 
    availableMeepleOptions = [],
    validPlacements = []
  } = gameState;

  return React.createElement('div', { className: 'turn-panel card' }, [
    React.createElement('div', { className: 'turn-header' }, [
      React.createElement('div', { 
        className: 'player-indicator',
        style: { borderLeftColor: currentPlayer.color.hex }
      }, [
        React.createElement('span', { className: 'turn-label' }, 'Vez de Jogar'),
        React.createElement('h2', { 
          className: 'player-name-current',
          style: { color: currentPlayer.color.hex }
        }, currentPlayer.name)
      ]),
      React.createElement('div', { className: 'deck-counter' }, [
        React.createElement('span', { className: 'deck-number' }, deck.length),
        React.createElement('span', { className: 'deck-label' }, 'no monte')
      ])
    ]),

    // Fase 1: Posicionamento e Rotação da Peça
    phase === TurnPhase.PLACE_TILE && currentTile ? React.createElement('div', { className: 'tile-action-box' }, [
      React.createElement('div', { className: 'drawn-tile-preview' }, [
        React.createElement(TileSvg, {
          tile: currentTile,
          size: 130
        }),
        React.createElement('div', { className: 'rotation-badge' }, `${currentTileRotation}°`)
      ]),
      React.createElement('div', { className: 'tile-info' }, [
        React.createElement('h3', { className: 'tile-name' }, currentTile.name),
        React.createElement('p', { className: 'tile-help-text' }, 
          validPlacements.length > 0 
            ? 'Gire a peça e clique em uma posição válida no tabuleiro.' 
            : '⚠️ Nenhuma posição válida com esta rotação. Tente girar a peça!'
        )
      ]),
      React.createElement('button', {
        className: 'btn btn-primary btn-rotate',
        onClick: onRotate,
        title: 'Girar peça em 90 graus no sentido horário'
      }, [
        React.createElement('span', { key: 'icon', className: 'btn-icon' }, '⟳'),
        React.createElement('span', { key: 'txt' }, 'Girar 90°')
      ])
    ]) : null,

    // Fase 2: Alocação Opcional de Meeple
    phase === TurnPhase.PLACE_MEEPLE ? React.createElement('div', { className: 'meeple-action-box' }, [
      React.createElement('div', { className: 'meeple-box-header' }, [
        React.createElement(MeepleIcon, { colorHex: currentPlayer.color.hex, size: 36 }),
        React.createElement('div', null, [
          React.createElement('h3', { className: 'meeple-title' }, 'Colocar Meeple?'),
          React.createElement('p', { className: 'meeple-subtitle' }, 
            `Disponíveis no estoque: ${currentPlayer.meeplesCount}`
          )
        ])
      ]),

      availableMeepleOptions.length > 0 && currentPlayer.meeplesCount > 0 ? (
        React.createElement('div', { className: 'meeple-options-list' }, [
          React.createElement('span', { className: 'options-label' }, 'Escolha o segmento da peça:'),
          availableMeepleOptions.map(opt => 
            React.createElement('button', {
              key: `meeple-${opt.segment}`,
              className: 'btn btn-outline btn-meeple-segment',
              onClick: () => onPlaceMeeple(opt.segment)
            }, [
              React.createElement('span', { key: 'dot', className: 'segment-dot', style: { background: currentPlayer.color.hex } }),
              React.createElement('span', { key: 'txt' }, opt.label)
            ])
          )
        ])
      ) : (
        React.createElement('p', { className: 'no-meeple-note' }, 
          currentPlayer.meeplesCount <= 0 
            ? 'Você não possui mais meeples disponíveis.' 
            : 'Não há estruturas elegíveis para meeple nesta peça.'
        )
      ),

      React.createElement('button', {
        className: 'btn btn-secondary btn-skip-meeple',
        onClick: onSkipMeeple
      }, '⏭️ Pular Meeple (Finalizar Turno)')
    ]) : null
  ]);
}
