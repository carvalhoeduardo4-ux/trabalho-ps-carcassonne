import React from 'react';
import { TileSvg, MeepleIcon } from './TileSvg.js';
import { meepleSegment } from '../ui/tileAdapter.js';

// Mesmas coordenadas do viewBox 100x100 de TileSvg.js.
const MEEPLE_COORDS = Object.freeze({
  NORTH: [50, 18], EAST: [82, 50], SOUTH: [50, 82], WEST: [18, 50],
  CENTER: [50, 50],
  NORTH_LEFT: [34, 22], NORTH_RIGHT: [66, 22],
  EAST_LEFT: [78, 34], EAST_RIGHT: [78, 66],
  SOUTH_LEFT: [66, 78], SOUTH_RIGHT: [34, 78],
  WEST_LEFT: [22, 66], WEST_RIGHT: [22, 34]
});

function samePlayerPresentation(a = [], b = []) {
  if (a === b) return true;
  if (a.length !== b.length) return false;
  for (let i = 0; i < a.length; i++) {
    if (a[i]?.color?.hex !== b[i]?.color?.hex || a[i]?.name !== b[i]?.name) return false;
  }
  return true;
}

const StableTileArt = React.memo(function StableTileArt({ tile, size, players, showLabel }) {
  return React.createElement(TileSvg, { tile, size, players, showLabel });
}, (previous, next) => previous.tile === next.tile &&
  previous.size === next.size &&
  previous.showLabel === next.showLabel &&
  samePlayerPresentation(previous.players, next.players));

function MeepleGhost({ tile, previewMeeple, currentPlayerIndex, players }) {
  const segment = meepleSegment(
    tile.__sourceFeatures,
    previewMeeple.featureId,
    tile.__sourceRotation
  );
  const [cx, cy] = MEEPLE_COORDS[segment] || MEEPLE_COORDS.CENTER;
  const player = players[currentPlayerIndex];

  return React.createElement('div', {
    className: 'meeple-ghost-layer',
    'aria-hidden': 'true'
  }, React.createElement('div', {
    className: 'meeple-ghost-icon',
    style: { top: `${cy - 15}%`, left: `${cx - 15}%` }
  }, React.createElement(MeepleIcon, {
    colorHex: player?.color?.hex || '#ef4444',
    size: 30,
    label: String(currentPlayerIndex + 1),
    title: player ? `Meeple de ${player.name}` : null
  })));
}

/**
 * A arte estática e o ghost têm ciclos de render separados. Alterar o hover
 * recompõe apenas um MeepleIcon, sem chamar TileSvg/illustratedTileUri de novo.
 */
export const StablePlacedTile = React.memo(function StablePlacedTile({
  tile,
  size,
  players,
  showLabel,
  previewMeeple = null,
  currentPlayerIndex = 0
}) {
  return React.createElement(React.Fragment, null,
    React.createElement(StableTileArt, { tile, size, players, showLabel }),
    previewMeeple ? React.createElement(MeepleGhost, {
      tile,
      previewMeeple,
      currentPlayerIndex,
      players
    }) : null
  );
}, (previous, next) => previous.tile === next.tile &&
  previous.size === next.size &&
  previous.showLabel === next.showLabel &&
  previous.previewMeeple?.featureId === next.previewMeeple?.featureId &&
  (!(previous.previewMeeple || next.previewMeeple) ||
    previous.currentPlayerIndex === next.currentPlayerIndex) &&
  samePlayerPresentation(previous.players, next.players));
