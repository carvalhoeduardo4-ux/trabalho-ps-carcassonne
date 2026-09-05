import React, { useRef, useEffect } from 'react';
import { getBoardBounds, coordsToKey } from '../core/board.js';
import { TileSvg } from './TileSvg.js';

export function GameBoard({ 
  board, 
  validPlacements = [], 
  onPlaceTile, 
  currentTile, 
  phase, 
  players = [] 
}) {
  const boardRef = useRef(null);
  const bounds = getBoardBounds(board, 1);

  // Mapear posições válidas para busca rápida O(1)
  const validMap = new Set(validPlacements.map(p => coordsToKey(p.x, p.y)));

  // Centralizar o tabuleiro na carga inicial ou quando solicitado
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

  // Loop de Y (de cima para baixo: do maxY para minY)
  for (let y = bounds.maxY; y >= bounds.minY; y--) {
    const cells = [];
    for (let x = bounds.minX; x <= bounds.maxX; x++) {
      const key = coordsToKey(x, y);
      const placedTile = board[key];
      const isValidSlot = phase === 'PLACE_TILE' && validMap.has(key);

      cells.push(
        React.createElement('div', {
          key: `cell-${x}-${y}`,
          className: 'grid-cell',
          style: { width: tileSize, height: tileSize }
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
        React.createElement('span', { className: 'badge' }, `Peças no tabuleiro: ${Object.keys(board).length}`),
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
