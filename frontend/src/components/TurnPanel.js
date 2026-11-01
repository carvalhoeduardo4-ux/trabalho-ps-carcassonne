import React from 'react';
import { TurnPhase, ROTATIONS, rotationDegrees } from '../ui/constants.js';
import { toRenderTile, featureLabel } from '../ui/tileAdapter.js';
import { TileSvg, MeepleIcon } from './TileSvg.js';

export function TurnPanel({
  game,
  rotation,
  validPlacements = [],
  onRotate,
  onSetRotation,
  onPlaceMeeple,
  onSkipMeeple,
  onPreviewMeeple,
  onShowResult,
  currentPlayer,
  busy = false
}) {
  const { phase, drawnTile, placedTile, deckRemaining, meepleOptions = [], placements = [] } = game;
  const previewTile = drawnTile ? toRenderTile(drawnTile.features, rotation) : null;
  const placedRender = placedTile ? toRenderTile(placedTile.features, placedTile.rotation) : null;
  const countByRotation = ROTATIONS.map(r => placements.filter(p => p.rotation === r).length);

  // Partida encerrada: o painel só oferece reabrir o resultado
  if (game.status === 'FINISHED') {
    return React.createElement('div', { className: 'turn-panel card' },
      React.createElement('h2', { className: 'panel-title' }, 'Partida encerrada'),
      React.createElement('p', { className: 'tile-help-text' }, 'Confira o tabuleiro final ou veja o resultado.'),
      React.createElement('button', { type: 'button', className: 'btn btn-primary', onClick: onShowResult }, 'Ver resultado')
    );
  }

  return React.createElement('div', { className: 'turn-panel card' },
    React.createElement('div', { className: 'turn-header' },
      React.createElement('div', {
        className: 'player-indicator',
        style: { borderLeftColor: currentPlayer.color.hex }
      },
        React.createElement('span', { className: 'turn-label' }, 'Vez de jogar'),
        React.createElement('h2', {
          className: 'player-name-current',
          style: { color: currentPlayer.color.hex }
        }, currentPlayer.name)
      ),
      React.createElement('div', { className: 'deck-counter' },
        React.createElement('span', { className: 'deck-number' }, deckRemaining),
        React.createElement('span', { className: 'deck-label' }, 'no monte')
      )
    ),

    // Fase 1: Posicionamento e Rotação da Peça
    phase === TurnPhase.PLACE_TILE && previewTile ? React.createElement('div', { className: 'tile-action-box' },
      React.createElement('div', { className: 'drawn-tile-preview' },
        React.createElement(TileSvg, {
          tile: previewTile,
          size: 130
        }),
        React.createElement('div', { className: 'rotation-badge' }, `${rotationDegrees(rotation)}°`)
      ),
      React.createElement('div', { className: 'tile-info' },
        React.createElement('h3', { className: 'tile-name' }, 'Peça sorteada'),
        React.createElement('p', { className: 'tile-help-text' },
          validPlacements.length > 0
            ? 'Passe o mouse no tabuleiro para ver o encaixe e clique em uma posição destacada.'
            : placements.length > 0
              ? 'Nenhuma posição válida com esta rotação. Escolha outra rotação abaixo.'
              : 'Nenhuma posição válida para esta peça.'
        )
      ),
      React.createElement('div', { className: 'rotate-buttons' },
        React.createElement('button', {
          type: 'button',
          className: 'btn btn-outline btn-rotate-ccw',
          onClick: () => onRotate(-1),
          disabled: busy,
          'aria-label': 'Girar 90 graus no sentido anti-horário',
          title: 'Girar 90° no sentido anti-horário'
        }, React.createElement('span', { className: 'btn-icon', 'aria-hidden': 'true' }, '⟲')),
        React.createElement('button', {
          type: 'button',
          className: 'btn btn-primary btn-rotate',
          onClick: () => onRotate(1),
          disabled: busy,
          title: 'Girar 90° no sentido horário'
        },
          React.createElement('span', { className: 'btn-icon', 'aria-hidden': 'true' }, '⟳'),
          React.createElement('span', null, 'Girar 90°')
        )
      ),
      // Rotações com a quantidade de posições válidas de cada uma
      React.createElement('div', { className: 'rotation-options-box' },
        React.createElement('span', { id: 'rotation-label', className: 'options-label' }, 'Posições válidas por rotação:'),
        React.createElement('div', { className: 'rotation-options', role: 'group', 'aria-labelledby': 'rotation-label' },
          ROTATIONS.map((r, i) => React.createElement('button', {
            key: r,
            type: 'button',
            className: `rotation-option ${rotation === r ? 'active' : ''}`,
            'aria-pressed': rotation === r,
            disabled: busy || countByRotation[i] === 0,
            onClick: () => onSetRotation(r),
            title: `${countByRotation[i]} ${countByRotation[i] === 1 ? 'posição válida' : 'posições válidas'}`
          },
            React.createElement('span', { className: 'rotation-degrees' }, `${rotationDegrees(r)}°`),
            React.createElement('span', { className: 'rotation-count' }, countByRotation[i])
          ))
        )
      )
    ) : null,

    // Fase 2: Alocação Opcional de Meeple
    phase === TurnPhase.PLACE_MEEPLE ? React.createElement('div', { className: 'meeple-action-box' },
      placedRender ? React.createElement('div', { className: 'drawn-tile-preview', style: { marginBottom: 12 } },
        React.createElement(TileSvg, {
          tile: placedRender,
          size: 100
        })
      ) : null,
      React.createElement('div', { className: 'meeple-box-header' },
        React.createElement(MeepleIcon, { colorHex: currentPlayer.color.hex, size: 36 }),
        React.createElement('div', null,
          React.createElement('h3', { className: 'meeple-title' }, 'Colocar meeple?'),
          React.createElement('p', { className: 'meeple-subtitle' },
            `Disponíveis no estoque: ${currentPlayer.availableMeeples}`
          )
        )
      ),

      meepleOptions.length > 0 ? (
        React.createElement('div', { className: 'meeple-options-list' },
          React.createElement('span', { className: 'options-label' }, 'Passe o mouse para ver no tabuleiro e clique para escolher:'),
          meepleOptions.map(opt =>
            React.createElement('button', {
              key: `meeple-${opt.x}-${opt.y}-${opt.featureId}`,
              type: 'button',
              className: 'btn btn-outline btn-meeple-segment',
              disabled: busy,
              onClick: () => onPlaceMeeple(opt),
              onMouseEnter: () => onPreviewMeeple(opt),
              onMouseLeave: () => onPreviewMeeple(null)
            },
              React.createElement('span', { className: 'segment-dot', style: { background: currentPlayer.color.hex } }),
              React.createElement('span', null, placedTile
                ? featureLabel(placedTile.features, opt.featureId, placedTile.rotation)
                : `Região ${opt.featureId}`)
            )
          )
        )
      ) : (
        React.createElement('p', { className: 'no-meeple-note' },
          currentPlayer.availableMeeples <= 0
            ? 'Você não possui mais meeples disponíveis.'
            : 'Não há estruturas elegíveis para meeple nesta peça.'
        )
      ),

      React.createElement('button', {
        type: 'button',
        className: `btn ${meepleOptions.length === 0 ? 'btn-primary' : 'btn-secondary'} btn-skip-meeple`,
        disabled: busy,
        onClick: onSkipMeeple
      }, meepleOptions.length === 0 ? 'Finalizar turno' : 'Finalizar turno sem meeple')
    ) : null
  );
}
