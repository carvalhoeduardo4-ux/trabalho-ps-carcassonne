import React, { useRef, useEffect, useMemo, useState } from 'react';
import { TileSvg } from './TileSvg.js';
import { StablePlacedTile } from './StablePlacedTile.js';
import { reconcileBoardTiles } from '../ui/boardTileCache.js';

const keyOf = (x, y) => `${x},${y}`;

/** Tamanhos de peça (px) para cada nível de zoom. */
const ZOOM_STEPS = [60, 80, 100, 120, 140];
const DEFAULT_ZOOM = 2;

/** Distância mínima (px) para um clique virar arraste. */
const DRAG_THRESHOLD = 5;

// Mantém previews de encaixe estáveis; peças colocadas usam cache por assinatura.
const MemoizedTileSvg = React.memo(TileSvg);

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

function GameBoardComponent({
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
  turnNotice = null,
  readOnly = false,  // vez da IA: nenhuma casa aceita clique
  aiTarget = null    // { x, y } onde a IA vai colocar a peça
}) {
  const boardRef = useRef(null);
  const [flashKey, setFlashKey] = useState(null); // célula que acabou de receber um clique inválido
  const [hoverKey, setHoverKey] = useState(null); // posição válida sob o mouse (pré-visualização)
  const [zoom, setZoom] = useState(DEFAULT_ZOOM);
  const drag = useRef(null);
  const dragFrame = useRef(null);
  const dragPoint = useRef(null);
  const tileCache = useRef(new Map());
  const bounds = useMemo(() => getBounds(board, 1), [board]);
  const tileSize = ZOOM_STEPS[zoom];

  // O backend devolve o estado inteiro a cada ação. A assinatura permite
  // reaproveitar as mesmas referências para todas as tiles que não mudaram.
  const baseTilesByKey = useMemo(() => {
    const result = reconcileBoardTiles(tileCache.current, board, meeples);
    tileCache.current = result.cache;
    return result.tiles;
  }, [board, meeples]);

  const validMap = useMemo(
    () => new Set(validPlacements.map(p => keyOf(p.x, p.y))),
    [validPlacements]
  );
  const scoredSet = useMemo(() => new Set(scoredKeys), [scoredKeys]);
  const meeplePreviewKey = meeplePreview ? keyOf(meeplePreview.x, meeplePreview.y) : null;

  useEffect(() => {
    if (!flashKey) return undefined;
    const timer = setTimeout(() => setFlashKey(null), 600);
    return () => clearTimeout(timer);
  }, [flashKey]);

  useEffect(() => {
    if (phase !== 'PLACE_TILE' && hoverKey !== null) setHoverKey(null);
  }, [phase, hoverKey]);

  /** Explica por que a célula clicada não aceita a peça atual. */
  const invalidReason = (x, y) => {
    if (baseTilesByKey.has(keyOf(x, y))) return 'occupied';
    const touches = [[1, 0], [-1, 0], [0, 1], [0, -1]]
      .some(([dx, dy]) => baseTilesByKey.has(keyOf(x + dx, y + dy)));
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

  // Garante que a jogada escolhida pela IA fique visível
  useEffect(() => {
    if (!aiTarget || !boardRef.current) return;
    // Aguarda o commit do alvo para não misturar medição de layout e início do
    // scroll suave no mesmo frame da atualização de React.
    const frame = requestAnimationFrame(() => {
      const target = boardRef.current?.querySelector(`[data-key="${keyOf(aiTarget.x, aiTarget.y)}"]`);
      if (target) target.scrollIntoView({ block: 'nearest', inline: 'nearest', behavior: 'smooth' });
    });
    return () => cancelAnimationFrame(frame);
  }, [aiTarget]);

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
      boardRef.current?.classList.add('is-dragging');
      // PLACE_MEEPLE não possui hover de encaixe: nenhum setState no pan.
      if (phase === 'PLACE_TILE' && hoverKey !== null) setHoverKey(null);
    }
    dragPoint.current = { left: d.left - dx, top: d.top - dy };
    if (dragFrame.current === null) {
      dragFrame.current = requestAnimationFrame(() => {
        dragFrame.current = null;
        const point = dragPoint.current;
        const el = boardRef.current;
        if (point && el) {
          el.scrollLeft = point.left;
          el.scrollTop = point.top;
        }
      });
    }
  };

  const onPointerUp = () => {
    const d = drag.current;
    if (!d) return;
    if (dragFrame.current !== null) {
      cancelAnimationFrame(dragFrame.current);
      dragFrame.current = null;
      const point = dragPoint.current;
      if (d.moved && point && boardRef.current) {
        boardRef.current.scrollLeft = point.left;
        boardRef.current.scrollTop = point.top;
      }
    }
    boardRef.current?.classList.remove('is-dragging');
    // espera o evento de clique passar, para o onClickCapture saber que foi arraste
    setTimeout(() => { if (drag.current === d) drag.current = null; }, 0);
  };

  // Um arraste que termina em cima de uma posição não deve colocar a peça
  const onClickCapture = (e) => {
    if (drag.current && drag.current.moved) {
      e.stopPropagation();
      e.preventDefault();
    }
  };

  useEffect(() => () => {
    if (dragFrame.current !== null) cancelAnimationFrame(dragFrame.current);
    boardRef.current?.classList.remove('is-dragging');
  }, []);

  const rows = [];

  // NORTH = y+1: desenha de cima para baixo (maxY -> minY)
  for (let y = bounds.maxY; y >= bounds.minY; y--) {
    const cells = [];
    for (let x = bounds.minX; x <= bounds.maxX; x++) {
      const key = keyOf(x, y);
      const placedTile = baseTilesByKey.get(key);
      const isValidSlot = !readOnly && phase === 'PLACE_TILE' && validMap.has(key);
      const rejectsClick = !readOnly && phase === 'PLACE_TILE' && !isValidSlot;
      const isAiTarget = !!aiTarget && aiTarget.x === x && aiTarget.y === y && !placedTile;
      const classes = ['grid-cell'];
      if (rejectsClick) classes.push('grid-cell-blocked');
      if (flashKey === key) classes.push('invalid-flash');
      if (scoredSet.has(key)) classes.push('score-flash');
      if (key === lastPlacedKey && placedTile) classes.push('last-placed');
      if (isAiTarget) classes.push('ai-target');

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
          placedTile ? React.createElement(StablePlacedTile, {
            tile: placedTile,
            size: tileSize - 4,
            players: players,
            showLabel: showCoords,
            previewMeeple: key === meeplePreviewKey ? meeplePreview : null,
            currentPlayerIndex
          }) : isAiTarget ? React.createElement(MemoizedTileSvg, {
            highlightValid: true,
            ghostTile: previewTile,
            size: tileSize - 4
          }) : isValidSlot ? React.createElement(MemoizedTileSvg, {
            highlightValid: true,
            ghostTile: hoverKey === key && !(drag.current && drag.current.moved) ? previewTile : null,
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
        readOnly ? React.createElement('span', { className: 'badge badge-ai' },
          React.createElement('span', { 'aria-hidden': 'true' }, '🤖 '),
          'Computador jogando…'
        ) : phase === 'PLACE_TILE' ? React.createElement('span', { className: 'badge badge-action' },
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
        className: 'board-scroll-container',
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

// Estados do App sem impacto visual no tabuleiro (busy/error/modais) não
// recompõem a grade quando as props do board continuam idênticas.
export const GameBoard = React.memo(GameBoardComponent);
