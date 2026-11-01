import React from 'react';
import { TurnPhase, rotationDegrees } from '../ui/constants.js';
import { toRenderTile, featureLabel } from '../ui/tileAdapter.js';
import { TileSvg, MeepleIcon } from './TileSvg.js';

export function TurnPanel({
  game,
  rotation,
  validPlacements = [],
  onRotate,
  onPlaceMeeple,
  onSkipMeeple,
  currentPlayer,
  busy = false
}) {
  const { phase, drawnTile, placedTile, deckRemaining, meepleOptions = [], placements = [] } = game;
  const previewTile = drawnTile ? toRenderTile(drawnTile.features, rotation) : null;
  const placedRender = placedTile ? toRenderTile(placedTile.features, placedTile.rotation) : null;

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
        React.createElement('span', { className: 'deck-number' }, deckRemaining),
        React.createElement('span', { className: 'deck-label' }, 'no monte')
      ])
    ]),

    // Fase 1: Posicionamento e Rotação da Peça
    phase === TurnPhase.PLACE_TILE && previewTile ? React.createElement('div', { className: 'tile-action-box' }, [
      React.createElement('div', { className: 'drawn-tile-preview' }, [
        React.createElement(TileSvg, {
          tile: previewTile,
          size: 130
        }),
        React.createElement('div', { className: 'rotation-badge' }, `${rotationDegrees(rotation)}°`)
      ]),
      React.createElement('div', { className: 'tile-info' }, [
        React.createElement('h3', { className: 'tile-name' }, `Peça ${drawnTile.tileId}`),
        React.createElement('p', { className: 'tile-help-text' }, 
          validPlacements.length > 0 
            ? 'Gire a peça e clique em uma posição válida no tabuleiro.' 
            : placements.length > 0
              ? '⚠️ Nenhuma posição válida com esta rotação. Tente girar a peça!'
              : '⚠️ Nenhuma posição válida para esta peça.'
        )
      ]),
      React.createElement('button', {
        className: 'btn btn-primary btn-rotate',
        onClick: onRotate,
        disabled: busy,
        title: 'Girar peça em 90 graus no sentido horário'
      }, [
        React.createElement('span', { key: 'icon', className: 'btn-icon' }, '⟳'),
        React.createElement('span', { key: 'txt' }, 'Girar 90°')
      ])
    ]) : null,

    // Fase 2: Alocação Opcional de Meeple
    phase === TurnPhase.PLACE_MEEPLE ? React.createElement('div', { className: 'meeple-action-box' }, [
      placedRender ? React.createElement('div', { className: 'drawn-tile-preview', style: { marginBottom: 12 } }, [
        React.createElement(TileSvg, {
          tile: placedRender,
          size: 100
        }),
        React.createElement('div', { className: 'rotation-badge' }, `(${placedTile.x}, ${placedTile.y})`)
      ]) : null,
      React.createElement('div', { className: 'meeple-box-header' }, [
        React.createElement(MeepleIcon, { colorHex: currentPlayer.color.hex, size: 36 }),
        React.createElement('div', null, [
          React.createElement('h3', { className: 'meeple-title' }, 'Colocar Meeple?'),
          React.createElement('p', { className: 'meeple-subtitle' }, 
            `Disponíveis no estoque: ${currentPlayer.availableMeeples}`
          )
        ])
      ]),

      meepleOptions.length > 0 ? (
        React.createElement('div', { className: 'meeple-options-list' }, [
          React.createElement('span', { className: 'options-label' }, 'Escolha a região da peça:'),
          meepleOptions.map(opt => 
            React.createElement('button', {
              key: `meeple-${opt.x}-${opt.y}-${opt.featureId}`,
              className: 'btn btn-outline btn-meeple-segment',
              disabled: busy,
              onClick: () => onPlaceMeeple(opt)
            }, [
              React.createElement('span', { key: 'dot', className: 'segment-dot', style: { background: currentPlayer.color.hex } }),
              React.createElement('span', { key: 'txt' }, placedTile
                ? featureLabel(placedTile.features, opt.featureId, placedTile.rotation)
                : `Região ${opt.featureId}`)
            ])
          )
        ])
      ) : (
        React.createElement('p', { className: 'no-meeple-note' }, 
          currentPlayer.availableMeeples <= 0 
            ? 'Você não possui mais meeples disponíveis.' 
            : 'Não há estruturas elegíveis para meeple nesta peça.'
        )
      ),

      React.createElement('button', {
        className: 'btn btn-secondary btn-skip-meeple',
        disabled: busy,
        onClick: onSkipMeeple
      }, '⏭️ Pular Meeple (Finalizar Turno)')
    ]) : null
  ]);
}
