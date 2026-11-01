import React, { useRef, useEffect, useMemo, useState } from 'react';
import { TileSvg } from './TileSvg.js';
import { toRenderTile, meepleSegment } from '../ui/tileAdapter.js';

const keyOf = (x, y) => `${x},${y}`;

/** Tamanhos de peça (px) para cada nível de zoom. */
const ZOOM_STEPS = [60, 80, 100, 120, 140];
const DEFAULT_ZOOM = 2;

/** Distância mínima (px) para um clique virar arraste. */
const DRAG_THRESHOLD = 5;

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
  scoredKeys = [],
  phase,
  players = [],
  previewTile = null,
  lastPlacedKey = null,
  meeplePreview = null,
  currentPlayerIndex = 0,
  showCoords = false,
  onToggleCoords,
  turnNotice = null
}) {
  const boardRef = useRef(null);
  const [flashKey, setFlashKey] = useState(null); // célula que acabou de receber um clique inválido
  const [hoverKey, setHoverKey] = useState(null); // posição válida sob o mouse (pré-visualização)
  const [zoom, setZoom] = useState(DEFAULT_ZOOM);
  const [dragging, setDragging] = useState(false);
  const drag = useRef(null);
  const bounds = getBounds(board, 1);
  const tileSize = ZOOM_STEPS[zoom];

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
      // Meeple semitransparente na região que o jogador está avaliando
      if (meeplePreview && meeplePreview.x === t.x && meeplePreview.y === t.y) {
        tileMeeples.push({
          segment: meepleSegment(t.features, meeplePreview.featureId, t.rotation),
          playerIndex: currentPlayerIndex,
          ghost: true
        });
      }
      map.set(keyOf(t.x, t.y), { ...render, x: t.x, y: t.y, meeples: tileMeeples });
    });
    return map;
  }, [board, meeples, meeplePreview, currentPlayerIndex]);

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

  /** Leva a visão até a última peça colocada (ou à peça inicial). */
  const centerBoard = (smooth = true) => {
    const el = boardRef.current;
    if (!el) return;
    const target = el.querySelector(`[data-key="${lastPlacedKey || '0,0'}"]`);
    if (target) {
      target.scrollIntoView({ block: 'center', inline: 'center', behavior: smooth ? 'smooth' : 'auto' });
    } else {
      el.scrollLeft = (el.scrollWidth - el.clientWidth) / 2;
      el.scrollTop = (el.scrollHeight - el.clientHeight) / 2;
    }
  };

  useEffect(() => {
    centerBoard(false);
  }, []);

  const changeZoom = (delta) => {
    const el = boardRef.current;
    // guarda o ponto central para não "pular" ao mudar o zoom
    const cx = (el.scrollLeft + el.clientWidth / 2) / el.scrollWidth;
    const cy = (el.scrollTop + el.clientHeight / 2) / el.scrollHeight;
    setZoom(z => Math.min(ZOOM_STEPS.length - 1, Math.max(0, z + delta)));
    requestAnimationFrame(() => {
      el.scrollLeft = cx * el.scrollWidth - el.clientWidth / 2;
      el.scrollTop = cy * el.scrollHeight - el.clientHeight / 2;
    });
  };

  // Arrastar o tabuleiro com o mouse (no toque a rolagem nativa já resolve)
  const onPointerDown = (e) => {
    if (e.pointerType !== 'mouse' || e.button !== 0) return;
    const el = boardRef.current;
    drag.current = { x: e.clientX, y: e.clientY, left: el.scrollLeft, top: el.scrollTop, moved: false };
  };

  const onPointerMove = (e) => {
    const d = drag.current;
    if (!d) return;
    const dx = e.clientX - d.x;
    const dy = e.clientY - d.y;
    if (!d.moved && Math.hypot(dx, dy) < DRAG_THRESHOLD) return;
    if (!d.moved) {
      d.moved = true;
      setDragging(true);
    }
    boardRef.current.scrollLeft = d.left - dx;
    boardRef.current.scrollTop = d.top - dy;
  };

  const onPointerUp = () => {
    if (!drag.current) return;
    setDragging(false);
    // espera o evento de clique passar, para o onClickCapture saber que foi arraste
    setTimeout(() => { drag.current = null; }, 0);
  };

  // Um arraste que termina em cima de uma posição não deve colocar a peça
  const onClickCapture = (e) => {
    if (drag.current && drag.current.moved) {
      e.stopPropagation();
      e.preventDefault();
    }
  };

  const rows = [];

  // NORTH = y+1: desenha de cima para baixo (maxY -> minY)
  for (let y = bounds.maxY; y >= bounds.minY; y--) {
    const cells = [];
    for (let x = bounds.minX; x <= bounds.maxX; x++) {
      const key = keyOf(x, y);
      const placedTile = tilesByKey.get(key);
      const isValidSlot = phase === 'PLACE_TILE' && validMap.has(key);
      const rejectsClick = phase === 'PLACE_TILE' && !isValidSlot;
      const classes = ['grid-cell'];
      if (rejectsClick) classes.push('grid-cell-blocked');
      if (flashKey === key) classes.push('invalid-flash');
      if (scoredKeys.includes(key)) classes.push('score-flash');
      if (key === lastPlacedKey && placedTile) classes.push('last-placed');

      cells.push(
        React.createElement('div', {
          key: `cell-${x}-${y}`,
          'data-key': key,
          className: classes.join(' '),
          style: { width: tileSize, height: tileSize },
          onClick: rejectsClick ? () => handleInvalidClick(x, y) : undefined,
          onMouseEnter: isValidSlot ? () => setHoverKey(key) : undefined,
          onMouseLeave: isValidSlot ? () => setHoverKey(null) : undefined
        },
          placedTile ? React.createElement(TileSvg, {
            tile: placedTile,
            size: tileSize - 4,
            players: players,
            showLabel: showCoords
          }) : isValidSlot ? React.createElement(TileSvg, {
            highlightValid: true,
            ghostTile: hoverKey === key && !dragging ? previewTile : null,
            size: tileSize - 4,
            onClick: () => onPlaceTile(x, y)
          }) : React.createElement('div', {
            className: 'empty-grid-slot',
            style: { width: tileSize - 4, height: tileSize - 4 }
          })
        )
      );
    }

    rows.push(
      React.createElement('div', {
        key: `row-${y}`,
        className: 'grid-row'
      }, cells)
    );
  }

  return React.createElement('div', { className: 'board-wrapper' },
    React.createElement('div', { className: 'board-header-bar' },
      React.createElement('div', { className: 'board-stats' },
        React.createElement('span', { className: 'badge' }, `Peças no tabuleiro: ${board.length}`),
        phase === 'PLACE_TILE' ? React.createElement('span', { className: 'badge badge-action' },
          `${validPlacements.length} ${validPlacements.length === 1 ? 'posição válida' : 'posições válidas'} nesta rotação`
        ) : null
      ),
      React.createElement('div', { className: 'board-tools' },
        React.createElement('div', { className: 'zoom-controls', role: 'group', 'aria-label': 'Zoom do tabuleiro' },
          React.createElement('button', {
            type: 'button',
            className: 'btn btn-sm btn-outline btn-icon-only',
            onClick: () => changeZoom(-1),
            disabled: zoom === 0,
            'aria-label': 'Diminuir zoom',
            title: 'Diminuir zoom'
          }, '−'),
          React.createElement('button', {
            type: 'button',
            className: 'btn btn-sm btn-outline btn-icon-only',
            onClick: () => changeZoom(1),
            disabled: zoom === ZOOM_STEPS.length - 1,
            'aria-label': 'Aumentar zoom',
            title: 'Aumentar zoom'
          }, '+')
        ),
        React.createElement('button', {
          type: 'button',
          className: 'btn btn-sm btn-outline',
          'aria-pressed': showCoords,
          onClick: onToggleCoords,
          title: 'Mostrar as coordenadas de cada peça'
        }, 'Coordenadas'),
        React.createElement('button', {
          type: 'button',
          className: 'btn btn-sm btn-outline',
          onClick: () => centerBoard(),
          title: 'Ir até a última peça colocada'
        },
          React.createElement('span', { 'aria-hidden': 'true' }, '🎯'),
          'Centralizar'
        )
      )
    ),
    React.createElement('div', { className: 'board-viewport' },
      // Aviso de troca de turno sobre o topo do tabuleiro
      turnNotice ? React.createElement('div', {
        key: `turn-${turnNotice.id}`,
        className: 'turn-banner',
        style: { '--player-color': turnNotice.color },
        'aria-hidden': 'true'
      }, `Vez de ${turnNotice.name}`) : null,
      React.createElement('div', {
        ref: boardRef,
        className: `board-scroll-container${dragging ? ' is-dragging' : ''}`,
        'aria-label': 'Tabuleiro',
        onPointerDown,
        onPointerMove,
        onPointerUp,
        onPointerLeave: onPointerUp,
        onClickCapture
      },
        React.createElement('div', {
          className: 'board-canvas'
        }, rows)
      )
    )
  );
}
