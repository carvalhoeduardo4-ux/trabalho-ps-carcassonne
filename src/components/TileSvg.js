import React from 'react';
import { Direction, MeeplePosition, PLAYER_COLORS } from '../core/types.js';

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

/**
 * Desenha a silhueta clássica de um Meeple do Carcassonne
 */
export function MeepleIcon({ colorHex = '#ef4444', size = 26, stroke = '#1e293b' }) {
  return React.createElement('svg', {
    width: size,
    height: size,
    viewBox: '0 0 100 100',
    style: { filter: 'drop-shadow(0 2px 4px rgba(0,0,0,0.4))' }
  }, [
    // Cabeça
    React.createElement('circle', {
      key: 'head',
      cx: 50,
      cy: 22,
      r: 14,
      fill: colorHex,
      stroke: stroke,
      strokeWidth: 4
    }),
    // Corpo, braços e pernas
    React.createElement('path', {
      key: 'body',
      d: 'M 35,36 C 30,36 18,48 16,56 C 14,64 26,66 30,58 L 36,50 L 32,88 C 32,94 44,94 46,88 L 50,68 L 54,88 C 56,94 68,94 68,88 L 64,50 L 70,58 C 74,66 86,64 84,56 C 82,48 70,36 65,36 Z',
      fill: colorHex,
      stroke: stroke,
      strokeWidth: 4,
      strokeLinejoin: 'round'
    })
  ]);
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
      }, [
        React.createElement('div', { key: 'pulse', className: 'valid-pulse' }),
        React.createElement('span', { key: 'plus', className: 'valid-plus-icon' }, '+')
      ]);
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

  // 1. Fundo do Campo (Grama Verde com leve gradiente)
  svgChildren.push(
    React.createElement('rect', {
      key: 'bg-field',
      x: 0,
      y: 0,
      width: 100,
      height: 100,
      fill: '#4ade80',
      rx: 8
    })
  );

  // 2. Cidades
  // Detectar configurações de cidade
  const isCityNorth = edges[Direction.NORTH] === 'CITY';
  const isCityEast = edges[Direction.EAST] === 'CITY';
  const isCitySouth = edges[Direction.SOUTH] === 'CITY';
  const isCityWest = edges[Direction.WEST] === 'CITY';

  // Se for cidade em canto conectada (ex: North + East)
  const isCornerNE = isCityNorth && isCityEast && (tile.cityGroups?.[0]?.includes(Direction.NORTH) && tile.cityGroups?.[0]?.includes(Direction.EAST));
  const isCornerES = isCityEast && isCitySouth && (tile.cityGroups?.[0]?.includes(Direction.EAST) && tile.cityGroups?.[0]?.includes(Direction.SOUTH));
  const isCornerSW = isCitySouth && isCityWest && (tile.cityGroups?.[0]?.includes(Direction.SOUTH) && tile.cityGroups?.[0]?.includes(Direction.WEST));
  const isCornerWN = isCityWest && isCityNorth && (tile.cityGroups?.[0]?.includes(Direction.WEST) && tile.cityGroups?.[0]?.includes(Direction.NORTH));

  if (isCornerNE) {
    svgChildren.push(
      React.createElement('path', {
        key: 'city-corner-ne',
        d: 'M 35,0 L 100,0 L 100,65 C 65,65 35,35 35,0 Z',
        fill: '#b45309',
        stroke: '#78350f',
        strokeWidth: 3
      })
    );
  } else if (isCornerES) {
    svgChildren.push(
      React.createElement('path', {
        key: 'city-corner-es',
        d: 'M 100,35 L 100,100 L 35,100 C 35,65 65,35 100,35 Z',
        fill: '#b45309',
        stroke: '#78350f',
        strokeWidth: 3
      })
    );
  } else if (isCornerSW) {
    svgChildren.push(
      React.createElement('path', {
        key: 'city-corner-sw',
        d: 'M 0,35 L 65,100 L 0,100 Z',
        fill: '#b45309',
        stroke: '#78350f',
        strokeWidth: 3
      })
    );
  } else if (isCornerWN) {
    svgChildren.push(
      React.createElement('path', {
        key: 'city-corner-wn',
        d: 'M 0,65 L 0,0 L 65,0 C 65,35 35,65 0,65 Z',
        fill: '#b45309',
        stroke: '#78350f',
        strokeWidth: 3
      })
    );
  } else {
    // Bordas de cidade individuais
    if (isCityNorth) {
      svgChildren.push(
        React.createElement('path', {
          key: 'city-n',
          d: 'M 15,0 C 25,35 75,35 85,0 Z',
          fill: '#b45309',
          stroke: '#78350f',
          strokeWidth: 3
        })
      );
    }
    if (isCityEast) {
      svgChildren.push(
        React.createElement('path', {
          key: 'city-e',
          d: 'M 100,15 C 65,25 65,75 100,85 Z',
          fill: '#b45309',
          stroke: '#78350f',
          strokeWidth: 3
        })
      );
    }
    if (isCitySouth) {
      svgChildren.push(
        React.createElement('path', {
          key: 'city-s',
          d: 'M 15,100 C 25,65 75,65 85,100 Z',
          fill: '#b45309',
          stroke: '#78350f',
          strokeWidth: 3
        })
      );
    }
    if (isCityWest) {
      svgChildren.push(
        React.createElement('path', {
          key: 'city-w',
          d: 'M 0,15 C 35,25 35,75 0,85 Z',
          fill: '#b45309',
          stroke: '#78350f',
          strokeWidth: 3
        })
      );
    }
  }

  // 3. Estradas
  const isRoadNorth = edges[Direction.NORTH] === 'ROAD';
  const isRoadEast = edges[Direction.EAST] === 'ROAD';
  const isRoadSouth = edges[Direction.SOUTH] === 'ROAD';
  const isRoadWest = edges[Direction.WEST] === 'ROAD';

  // Estrada reta horizontal
  if (isRoadEast && isRoadWest && !isRoadNorth && !isRoadSouth) {
    svgChildren.push(
      React.createElement('path', {
        key: 'road-ew',
        d: 'M 0,50 L 100,50',
        stroke: '#fef08a',
        strokeWidth: 16,
        strokeLinecap: 'square'
      }),
      React.createElement('path', {
        key: 'road-ew-dash',
        d: 'M 0,50 L 100,50',
        stroke: '#ca8a04',
        strokeWidth: 2,
        strokeDasharray: '6 6'
      })
    );
  }
  // Estrada reta vertical
  else if (isRoadNorth && isRoadSouth && !isRoadEast && !isRoadWest) {
    svgChildren.push(
      React.createElement('path', {
        key: 'road-ns',
        d: 'M 50,0 L 50,100',
        stroke: '#fef08a',
        strokeWidth: 16,
        strokeLinecap: 'square'
      }),
      React.createElement('path', {
        key: 'road-ns-dash',
        d: 'M 50,0 L 50,100',
        stroke: '#ca8a04',
        strokeWidth: 2,
        strokeDasharray: '6 6'
      })
    );
  }
  // Curvas de estrada
  else if (isRoadEast && isRoadSouth && !isRoadNorth && !isRoadWest) {
    svgChildren.push(
      React.createElement('path', {
        key: 'road-es',
        d: 'M 100,50 Q 50,50 50,100',
        stroke: '#fef08a',
        strokeWidth: 16,
        fill: 'none'
      }),
      React.createElement('path', {
        key: 'road-es-dash',
        d: 'M 100,50 Q 50,50 50,100',
        stroke: '#ca8a04',
        strokeWidth: 2,
        strokeDasharray: '6 6',
        fill: 'none'
      })
    );
  } else if (isRoadSouth && isRoadWest && !isRoadNorth && !isRoadEast) {
    svgChildren.push(
      React.createElement('path', {
        key: 'road-sw',
        d: 'M 50,100 Q 50,50 0,50',
        stroke: '#fef08a',
        strokeWidth: 16,
        fill: 'none'
      }),
      React.createElement('path', {
        key: 'road-sw-dash',
        d: 'M 50,100 Q 50,50 0,50',
        stroke: '#ca8a04',
        strokeWidth: 2,
        strokeDasharray: '6 6',
        fill: 'none'
      })
    );
  } else if (isRoadWest && isRoadNorth && !isRoadEast && !isRoadSouth) {
    svgChildren.push(
      React.createElement('path', {
        key: 'road-wn',
        d: 'M 0,50 Q 50,50 50,0',
        stroke: '#fef08a',
        strokeWidth: 16,
        fill: 'none'
      }),
      React.createElement('path', {
        key: 'road-wn-dash',
        d: 'M 0,50 Q 50,50 50,0',
        stroke: '#ca8a04',
        strokeWidth: 2,
        strokeDasharray: '6 6',
        fill: 'none'
      })
    );
  } else if (isRoadNorth && isRoadEast && !isRoadSouth && !isRoadWest) {
    svgChildren.push(
      React.createElement('path', {
        key: 'road-ne',
        d: 'M 50,0 Q 50,50 100,50',
        stroke: '#fef08a',
        strokeWidth: 16,
        fill: 'none'
      }),
      React.createElement('path', {
        key: 'road-ne-dash',
        d: 'M 50,0 Q 50,50 100,50',
        stroke: '#ca8a04',
        strokeWidth: 2,
        strokeDasharray: '6 6',
        fill: 'none'
      })
    );
  }
  // Bifurcações, cruzamentos ou estrada que termina no centro
  else {
    if (isRoadNorth) {
      svgChildren.push(
        React.createElement('path', { key: 'rn', d: 'M 50,0 L 50,50', stroke: '#fef08a', strokeWidth: 16 }),
        React.createElement('path', { key: 'rn-d', d: 'M 50,0 L 50,50', stroke: '#ca8a04', strokeWidth: 2, strokeDasharray: '4 4' })
      );
    }
    if (isRoadEast) {
      svgChildren.push(
        React.createElement('path', { key: 're', d: 'M 50,50 L 100,50', stroke: '#fef08a', strokeWidth: 16 }),
        React.createElement('path', { key: 're-d', d: 'M 50,50 L 100,50', stroke: '#ca8a04', strokeWidth: 2, strokeDasharray: '4 4' })
      );
    }
    if (isRoadSouth) {
      svgChildren.push(
        React.createElement('path', { key: 'rs', d: 'M 50,50 L 50,100', stroke: '#fef08a', strokeWidth: 16 }),
        React.createElement('path', { key: 'rs-d', d: 'M 50,50 L 50,100', stroke: '#ca8a04', strokeWidth: 2, strokeDasharray: '4 4' })
      );
    }
    if (isRoadWest) {
      svgChildren.push(
        React.createElement('path', { key: 'rw', d: 'M 0,50 L 50,50', stroke: '#fef08a', strokeWidth: 16 }),
        React.createElement('path', { key: 'rw-d', d: 'M 0,50 L 50,50', stroke: '#ca8a04', strokeWidth: 2, strokeDasharray: '4 4' })
      );
    }
    if (isRoadNorth || isRoadEast || isRoadSouth || isRoadWest) {
      // Centro da interseção
      svgChildren.push(
        React.createElement('circle', { key: 'r-center', cx: 50, cy: 50, r: 10, fill: '#fef08a' }),
        React.createElement('circle', { key: 'r-center-p', cx: 50, cy: 50, r: 4, fill: '#ca8a04' })
      );
    }
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
        fill: '#f3f4f6',
        stroke: '#4b5563',
        strokeWidth: 2,
        rx: 3
      }),
      // Telhado vermelho triangular
      React.createElement('polygon', {
        key: 'monastery-roof',
        points: '50,22 28,38 72,38',
        fill: '#dc2626',
        stroke: '#991b1b',
        strokeWidth: 2
      }),
      // Cruz dourada no topo
      React.createElement('line', { key: 'cross-v', x1: 50, y1: 15, x2: 50, y2: 24, stroke: '#eab308', strokeWidth: 3 }),
      React.createElement('line', { key: 'cross-h', x1: 46, y1: 18, x2: 54, y2: 18, stroke: '#eab308', strokeWidth: 3 }),
      // Porta
      React.createElement('rect', { key: 'monastery-door', x: 45, y: 52, width: 10, height: 14, fill: '#78350f', rx: 2 })
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
      stroke: 'rgba(0,0,0,0.25)',
      strokeWidth: 2,
      rx: 8
    })
  );

  // 6. Meeples colocados
  if (meeples && meeples.length > 0) {
    for (let i = 0; i < meeples.length; i++) {
      const m = meeples[i];
      const coords = MEEPLE_COORDS[m.segment] || MEEPLE_COORDS[MeeplePosition.CENTER];
      const player = players[m.playerIndex];
      const colorHex = player?.color?.hex || '#ef4444';

      svgChildren.push(
        React.createElement('g', {
          key: `meeple-${i}-${m.segment}`,
          transform: `translate(${coords.cx - 15}, ${coords.cy - 15})`
        }, [
          React.createElement(MeepleIcon, { key: 'icon', colorHex, size: 30 })
        ])
      );
    }
  }

  return React.createElement('div', {
    className: `carcassonne-tile ${isGhost ? 'tile-ghost' : ''}`,
    style: {
      width: size,
      height: size,
      cursor: onClick ? 'pointer' : 'default'
    },
    onClick: onClick
  }, [
    React.createElement('svg', {
      key: 'svg-canvas',
      viewBox: '0 0 100 100',
      width: '100%',
      height: '100%',
      style: { display: 'block', borderRadius: '8px' }
    }, svgChildren),
    showLabel && tile.x !== undefined && tile.y !== undefined ? React.createElement('span', {
      key: 'coords-label',
      className: 'tile-coords-tag'
    }, `${tile.x},${tile.y}`) : null
  ]);
}
