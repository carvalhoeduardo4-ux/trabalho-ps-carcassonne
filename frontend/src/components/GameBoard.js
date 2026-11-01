import React, { useRef, useEffect, useMemo, useState } from 'react';
import { TileSvg } from './TileSvg.js';
import { toRenderTile, meepleSegment } from '../ui/tileAdapter.js';

const keyOf = (x, y) => `${x},${y}`;

/** Limites do tabuleiro esparso (coordenadas podem ser negativas), com margem. */
function getBounds(board, margin = 1) {
  if (board.length === 0) return { minX: -margin, maxX: margin, minY: -margin, maxY: margin };
  const xs = board.map(t => t.x);
  const ys = board.map(t => t.y);
  return {
    minX: Math.min(...xs) - margin,
    maxX: Math.max(...xs) + margin,
    minY: Math.min(...ys) - margin,
    maxY: Math.max(...ys) + margin
  };
}

export function GameBoard({
  board = [],
  meeples = [],
  validPlacements = [],
  onPlaceTile,
  onInvalidPlacement,
  phase,
  players = []
}) {
  const boardRef = useRef(null);
  const [flashKey, setFlashKey] = useState(null); // célula que acabou de receber um clique inválido
  const bounds = getBounds(board, 1);

  // Peças indexadas por posição, já no formato de desenho (com meeples)
  const tilesByKey = useMemo(() => {
    const map = new Map();
    board.forEach(t => {
      const render = toRenderTile(t.features, t.rotation);
      const tileMeeples = meeples
        .filter(m => m.x === t.x && m.y === t.y)
        .map(m => ({
          segment: meepleSegment(t.features, m.featureId, t.rotation),
          playerIndex: m.playerIndex
        }));
      map.set(keyOf(t.x, t.y), { ...render, x: t.x, y: t.y, meeples: tileMeeples });
    });
    return map;
  }, [board, meeples]);

  const validMap = new Set(validPlacements.map(p => keyOf(p.x, p.y)));

  useEffect(() => {
    if (!flashKey) return undefined;
    const timer = setTimeout(() => setFlashKey(null), 600);
    return () => clearTimeout(timer);
  }, [flashKey]);

  /** Explica por que a célula clicada não aceita a peça atual. */
  const invalidReason = (x, y) => {
    if (tilesByKey.has(keyOf(x, y))) return 'occupied';
    const touches = [[1, 0], [-1, 0], [0, 1], [0, -1]]
      .some(([dx, dy]) => tilesByKey.has(keyOf(x + dx, y + dy)));
    return touches ? 'incompatible' : 'isolated';
  };

  const handleInvalidClick = (x, y) => {
    setFlashKey(keyOf(x, y));
    if (onInvalidPlacement) onInvalidPlacement(invalidReason(x, y));
  };

  const centerBoard = () => {
    if (boardRef.current) {
      const el = boardRef.current;
      el.scrollLeft = (el.scrollWidth - el.clientWidth) / 2;
      el.scrollTop = (el.scrollHeight - el.clientHeight) / 2;
    }
  };

  useEffect(() => {
    centerBoard();
  }, []);

  const tileSize = 100;
  const rows = [];

  // NORTH = y+1: desenha de cima para baixo (maxY -> minY)
  for (let y = bounds.maxY; y >= bounds.minY; y--) {
    const cells = [];
    for (let x = bounds.minX; x <= bounds.maxX; x++) {
      const key = keyOf(x, y);
      const placedTile = tilesByKey.get(key);
      const isValidSlot = phase === 'PLACE_TILE' && validMap.has(key);
      const rejectsClick = phase === 'PLACE_TILE' && !isValidSlot;

      cells.push(
        React.createElement('div', {
          key: `cell-${x}-${y}`,
          className: `grid-cell${rejectsClick ? ' grid-cell-blocked' : ''}${flashKey === key ? ' invalid-flash' : ''}`,
          style: { width: tileSize, height: tileSize },
          onClick: rejectsClick ? () => handleInvalidClick(x, y) : undefined
        }, [
          placedTile ? React.createElement(TileSvg, {
            key: `tile-${key}`,
            tile: placedTile,
            size: tileSize - 4,
            players: players,
            showLabel: true
          }) : isValidSlot ? React.createElement(TileSvg, {
            key: `valid-${key}`,
            highlightValid: true,
            size: tileSize - 4,
            onClick: () => onPlaceTile(x, y)
          }) : React.createElement('div', {
            key: `empty-${key}`,
            className: 'empty-grid-slot',
            style: { width: tileSize - 4, height: tileSize - 4 }
          })
        ])
      );
    }

    rows.push(
      React.createElement('div', {
        key: `row-${y}`,
        className: 'grid-row'
      }, cells)
    );
  }

  return React.createElement('div', { className: 'board-wrapper' }, [
    React.createElement('div', { className: 'board-header-bar' }, [
      React.createElement('div', { className: 'board-stats' }, [
        React.createElement('span', { className: 'badge' }, `Peças no tabuleiro: ${board.length}`),
        phase === 'PLACE_TILE' ? React.createElement('span', { className: 'badge badge-action' }, 
          `✨ ${validPlacements.length} posições válidas disponíveis`
        ) : null
      ]),
      React.createElement('button', {
        className: 'btn btn-sm btn-outline',
        onClick: centerBoard,
        title: 'Centralizar visualização no tabuleiro'
      }, '🎯 Centralizar Tabuleiro')
    ]),
    React.createElement('div', {
      ref: boardRef,
      className: 'board-scroll-container'
    }, [
      React.createElement('div', {
        className: 'board-canvas'
      }, rows)
    ])
  ]);
}
