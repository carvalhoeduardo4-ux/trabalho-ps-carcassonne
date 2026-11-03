import React from 'react';
import { MeeplePosition } from '../ui/constants.js';
import { illustratedTileUri } from '../ui/illustratedTileArt.js';

/**
 * Coordenadas para renderizar os meeples em cada segmento (grid 100x100)
 */
const MEEPLE_COORDS = {
  [MeeplePosition.NORTH]: { cx: 50, cy: 18 },
  [MeeplePosition.EAST]: { cx: 82, cy: 50 },
  [MeeplePosition.SOUTH]: { cx: 50, cy: 82 },
  [MeeplePosition.WEST]: { cx: 18, cy: 50 },
  [MeeplePosition.CENTER]: { cx: 50, cy: 50 },
  NORTH_LEFT: { cx: 34, cy: 22 },
  NORTH_RIGHT: { cx: 66, cy: 22 },
  EAST_LEFT: { cx: 78, cy: 34 },
  EAST_RIGHT: { cx: 78, cy: 66 },
  SOUTH_LEFT: { cx: 66, cy: 78 },
  SOUTH_RIGHT: { cx: 34, cy: 78 },
  WEST_LEFT: { cx: 22, cy: 66 },
  WEST_RIGHT: { cx: 22, cy: 34 }
};

const MEEPLE_BODY = 'M 35,36 C 30,36 18,48 16,56 C 14,64 26,66 30,58 L 36,50 L 32,88 C 32,94 44,94 46,88 L 50,68 L 54,88 C 56,94 68,94 68,88 L 64,50 L 70,58 C 74,66 86,64 84,56 C 82,48 70,36 65,36 Z';

/**
 * Desenha a silhueta clássica de um Meeple do Carcassonne.
 * A auréola branca destaca o meeple sobre qualquer fundo e o número (label)
 * identifica o jogador sem depender só da cor.
 */
export function MeepleIcon({ colorHex = '#ef4444', size = 26, stroke = '#1e293b', label = null, title = null }) {
  return React.createElement('svg', {
    width: size,
    height: size,
    viewBox: '0 0 100 100',
    role: title ? 'img' : undefined,
    'aria-label': title || undefined,
    'aria-hidden': title ? undefined : 'true',
    style: { filter: 'drop-shadow(0 2px 4px rgba(0,0,0,0.4))' }
  },
    title ? React.createElement('title', null, title) : null,
    // Auréola clara
    React.createElement('circle', { cx: 50, cy: 22, r: 14, fill: 'none', stroke: '#ffffff', strokeWidth: 12 }),
    React.createElement('path', { d: MEEPLE_BODY, fill: 'none', stroke: '#ffffff', strokeWidth: 12, strokeLinejoin: 'round' }),
    // Cabeça
    React.createElement('circle', { cx: 50, cy: 22, r: 14, fill: colorHex, stroke, strokeWidth: 4 }),
    // Corpo, braços e pernas
    React.createElement('path', { d: MEEPLE_BODY, fill: colorHex, stroke, strokeWidth: 4, strokeLinejoin: 'round' }),
    label ? React.createElement('text', {
      x: 50,
      y: 70,
      textAnchor: 'middle',
      fontSize: 30,
      fontWeight: 800,
      fontFamily: 'Inter, sans-serif',
      fill: '#ffffff',
      stroke: '#000000',
      strokeWidth: 2,
      paintOrder: 'stroke'
    }, label) : null
  );
}

/**
 * Renderizador visual em SVG de um Tile de Carcassonne
 */
export function TileSvg({
  tile,
  size = 110,
  onClick,
  isGhost = false,
  highlightValid = false,
  ghostTile = null,
  showLabel = false,
  players = []
}) {
  if (!tile) {
    if (highlightValid) {
      return React.createElement('div', {
        className: 'tile-slot valid-slot',
        style: { width: size, height: size },
        onClick: onClick,
        title: 'Clique para posicionar a peça aqui'
      },
        ghostTile
          // Pré-visualização da peça sorteada nesta posição (mouse em cima)
          ? React.createElement(TileSvg, { tile: ghostTile, size: size - 4, isGhost: true })
          : [
              React.createElement('div', { key: 'pulse', className: 'valid-pulse' }),
              React.createElement('span', { key: 'plus', className: 'valid-plus-icon', 'aria-hidden': 'true' }, '+')
            ]
      );
    }
    return React.createElement('div', {
      className: 'tile-slot empty-slot',
      style: { width: size, height: size }
    });
  }

  const hasMonastery = tile.hasMonastery;
  const meeples = tile.meeples || [];

  // Elementos SVG para desenhar o tile
  const svgChildren = [];

  // CARCASSONNE_ART_V7: ilustração da topologia renderizada.
  // Meeples e eventos permanecem no componente original.
  svgChildren.push(React.createElement('image', {
    key: 'tile-illustration',
    x: 0, y: 0, width: 100, height: 100,
    href: illustratedTileUri(tile),
    preserveAspectRatio: 'none'
  }));

  // 6. Meeples colocados (ghost = pré-visualização da escolha do jogador)
  meeples.forEach((m, i) => {
    const coords = MEEPLE_COORDS[m.segment] || MEEPLE_COORDS[MeeplePosition.CENTER];
    const player = players[m.playerIndex];
    const colorHex = player?.color?.hex || '#ef4444';

    svgChildren.push(
      React.createElement('g', {
        key: `meeple-${i}-${m.segment}`,
        transform: `translate(${coords.cx - 15}, ${coords.cy - 15})`,
        opacity: m.ghost ? 0.6 : 1
      },
        React.createElement(MeepleIcon, {
          colorHex,
          size: 30,
          label: String(m.playerIndex + 1),
          title: player ? `Meeple de ${player.name}` : null
        })
      )
    );
  });

  return React.createElement('div', {
    className: `carcassonne-tile${isGhost ? ' tile-ghost' : ''}`,
    style: {
      width: size,
      height: size,
      cursor: onClick ? 'pointer' : 'default'
    },
    onClick: onClick
  },
    React.createElement('svg', {
      viewBox: '0 0 100 100',
      width: '100%',
      height: '100%',
      style: { display: 'block', borderRadius: '8px' }
    }, svgChildren),
    showLabel && tile.x !== undefined && tile.y !== undefined ? React.createElement('span', {
      className: 'tile-coords-tag'
    }, `${tile.x},${tile.y}`) : null
  );
}
