import React from 'react';
import { Direction, MeeplePosition, TILE_COLORS } from '../ui/constants.js';

/**
 * Coordenadas para renderizar os meeples em cada segmento (grid 100x100)
 */
const MEEPLE_COORDS = {
  [MeeplePosition.NORTH]: { cx: 50, cy: 18 },
  [MeeplePosition.EAST]: { cx: 82, cy: 50 },
  [MeeplePosition.SOUTH]: { cx: 50, cy: 82 },
  [MeeplePosition.WEST]: { cx: 18, cy: 50 },
  [MeeplePosition.CENTER]: { cx: 50, cy: 50 }
};

const DIRS = [Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST];
const SIDE_ANGLE = { NORTH: 0, EAST: 90, SOUTH: 180, WEST: 270 };

/** Formas de cidade com a cidade voltada para o norte; a rotação é aplicada depois. */
const CITY_SHAPES = {
  one: 'M 15,0 C 25,35 75,35 85,0 Z',                                         // só o lado N
  corner: 'M 35,0 L 100,0 L 100,65 C 65,65 35,35 35,0 Z',                      // N + E
  opposite: 'M 0,0 L 100,0 C 65,35 65,65 100,100 L 0,100 C 35,65 35,35 0,0 Z', // N + S
  three: 'M 0,0 L 100,0 L 100,100 C 70,65 30,65 0,100 Z',                      // N + E + W
  full: 'M 0,0 L 100,0 L 100,100 L 0,100 Z'                                    // 4 lados
};

/** Onde o brasão fica em cada forma (antes da rotação). */
const SHIELD_POS = { one: [50, 12], corner: [76, 24], opposite: [50, 22], three: [50, 30], full: [50, 30] };

/** Escolhe a forma e o ângulo de rotação para uma cidade com estes lados. */
function cityShape(sides) {
  const has = (d) => sides.includes(d);
  if (sides.length === 4) return { shape: 'full', angle: 0 };
  if (sides.length === 3) {
    const missing = DIRS.find(d => !has(d));
    return { shape: 'three', angle: (SIDE_ANGLE[missing] + 180) % 360 };
  }
  if (sides.length === 2) {
    const opposite = Math.abs(DIRS.indexOf(sides[0]) - DIRS.indexOf(sides[1])) === 2;
    if (opposite) return { shape: 'opposite', angle: has(Direction.NORTH) ? 0 : 90 };
    // lado do canto que vem primeiro no sentido horário (N+E -> N, E+S -> E, ...)
    const first = DIRS.find(d => has(d) && has(DIRS[(DIRS.indexOf(d) + 1) % 4]));
    return { shape: 'corner', angle: SIDE_ANGLE[first] };
  }
  return { shape: 'one', angle: SIDE_ANGLE[sides[0]] };
}

/** Gira um ponto em torno do centro da peça (mesma convenção do rotate do SVG). */
function rotatePoint([x, y], angle) {
  const r = (angle * Math.PI) / 180;
  const dx = x - 50;
  const dy = y - 50;
  return [50 + dx * Math.cos(r) - dy * Math.sin(r), 50 + dx * Math.sin(r) + dy * Math.cos(r)];
}

/** Trechos de estrada da peça; o último campo diz se as estradas se encontram no centro. */
function roadPaths(edges) {
  const n = edges[Direction.NORTH] === 'ROAD';
  const e = edges[Direction.EAST] === 'ROAD';
  const s = edges[Direction.SOUTH] === 'ROAD';
  const w = edges[Direction.WEST] === 'ROAD';
  const count = [n, e, s, w].filter(Boolean).length;

  if (count === 2) {
    if (e && w) return { paths: ['M 0,50 L 100,50'], junction: false };
    if (n && s) return { paths: ['M 50,0 L 50,100'], junction: false };
    if (e && s) return { paths: ['M 100,50 Q 50,50 50,100'], junction: false };
    if (s && w) return { paths: ['M 50,100 Q 50,50 0,50'], junction: false };
    if (w && n) return { paths: ['M 0,50 Q 50,50 50,0'], junction: false };
    if (n && e) return { paths: ['M 50,0 Q 50,50 100,50'], junction: false };
  }

  // Bifurcações, cruzamentos ou estrada que termina no centro
  const paths = [];
  if (n) paths.push('M 50,0 L 50,50');
  if (e) paths.push('M 50,50 L 100,50');
  if (s) paths.push('M 50,50 L 50,100');
  if (w) paths.push('M 0,50 L 50,50');
  return { paths, junction: paths.length > 0 };
}

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

  const edges = tile.edges || {};
  const hasMonastery = tile.hasMonastery;
  const meeples = tile.meeples || [];

  // Elementos SVG para desenhar o tile
  const svgChildren = [];

  // 1. Fundo do Campo
  svgChildren.push(
    React.createElement('rect', {
      key: 'bg-field',
      x: 0,
      y: 0,
      width: 100,
      height: 100,
      fill: TILE_COLORS.field,
      rx: 8
    })
  );

  // 2. Cidades (uma forma por cidade, girada para os lados certos)
  (tile.cities || []).forEach((city, i) => {
    const { shape, angle } = cityShape(city.sides);
    svgChildren.push(
      React.createElement('path', {
        key: `city-${i}`,
        d: CITY_SHAPES[shape],
        transform: `rotate(${angle} 50 50)`,
        fill: TILE_COLORS.city,
        stroke: TILE_COLORS.cityWall,
        strokeWidth: 3
      })
    );
    if (city.shields > 0) {
      // O brasão não gira junto: só a posição acompanha a cidade
      const [sx, sy] = rotatePoint(SHIELD_POS[shape], angle);
      svgChildren.push(
        React.createElement('path', {
          key: `shield-${i}`,
          d: 'M -6,-7 L 6,-7 L 6,1 C 6,5 0,8 0,8 C 0,8 -6,5 -6,1 Z',
          transform: `translate(${sx} ${sy})`,
          fill: TILE_COLORS.shield,
          stroke: '#ffffff',
          strokeWidth: 1.5
        })
      );
    }
  });

  // 3. Estradas: borda, leito e faixa tracejada
  const { paths, junction } = roadPaths(edges);
  paths.forEach((d, i) => {
    svgChildren.push(React.createElement('path', {
      key: `road-edge-${i}`, d, stroke: TILE_COLORS.roadEdge, strokeWidth: 18, fill: 'none'
    }));
  });
  if (junction) {
    svgChildren.push(React.createElement('circle', { key: 'r-center-edge', cx: 50, cy: 50, r: 12, fill: TILE_COLORS.roadEdge }));
  }
  paths.forEach((d, i) => {
    svgChildren.push(React.createElement('path', {
      key: `road-${i}`, d, stroke: TILE_COLORS.road, strokeWidth: 14, fill: 'none'
    }));
  });
  if (junction) {
    svgChildren.push(React.createElement('circle', { key: 'r-center', cx: 50, cy: 50, r: 10, fill: TILE_COLORS.road }));
  }
  paths.forEach((d, i) => {
    svgChildren.push(React.createElement('path', {
      key: `road-dash-${i}`, d, stroke: TILE_COLORS.roadLine, strokeWidth: 2, strokeDasharray: '6 6', fill: 'none'
    }));
  });
  if (junction) {
    svgChildren.push(React.createElement('circle', { key: 'r-center-p', cx: 50, cy: 50, r: 4, fill: TILE_COLORS.roadLine }));
  }

  // 4. Mosteiro (se houver)
  if (hasMonastery) {
    svgChildren.push(
      // Parede do mosteiro
      React.createElement('rect', {
        key: 'monastery-base',
        x: 34,
        y: 34,
        width: 32,
        height: 32,
        fill: TILE_COLORS.monasteryWall,
        stroke: TILE_COLORS.monasteryOutline,
        strokeWidth: 2,
        rx: 3
      }),
      // Telhado triangular
      React.createElement('polygon', {
        key: 'monastery-roof',
        points: '50,22 28,38 72,38',
        fill: TILE_COLORS.monasteryRoof,
        stroke: TILE_COLORS.monasteryRoofEdge,
        strokeWidth: 2
      }),
      // Cruz no topo
      React.createElement('line', { key: 'cross-v', x1: 50, y1: 15, x2: 50, y2: 24, stroke: TILE_COLORS.monasteryCross, strokeWidth: 3 }),
      React.createElement('line', { key: 'cross-h', x1: 46, y1: 18, x2: 54, y2: 18, stroke: TILE_COLORS.monasteryCross, strokeWidth: 3 }),
      // Porta
      React.createElement('rect', { key: 'monastery-door', x: 45, y: 52, width: 10, height: 14, fill: TILE_COLORS.monasteryDoor, rx: 2 })
    );
  }

  // 5. Borda da peça (delicada moldura de madeira)
  svgChildren.push(
    React.createElement('rect', {
      key: 'tile-border',
      x: 0,
      y: 0,
      width: 100,
      height: 100,
      fill: 'none',
      stroke: TILE_COLORS.fieldBorder,
      strokeWidth: 2,
      rx: 8
    })
  );

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
