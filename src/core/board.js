import { Direction, DIRECTIONS_ORDER, OPPOSITE_DIRECTION, DIRECTION_OFFSETS } from './types.js';

export function coordsToKey(x, y) {
  return `${x},${y}`;
}

export function keyToCoords(key) {
  const [x, y] = key.split(',').map(Number);
  return { x, y };
}

/**
 * Retorna os vizinhos ortogonais de uma coordenada (x, y)
 */
export function getOrthogonalNeighbors(board, x, y) {
  const neighbors = {};
  for (const dir of DIRECTIONS_ORDER) {
    const offset = DIRECTION_OFFSETS[dir];
    const key = coordsToKey(x + offset.dx, y + offset.dy);
    neighbors[dir] = board[key] || null;
  }
  return neighbors;
}

/**
 * Valida se a peça pode ser posicionada nas coordenadas (x, y)
 * Regras:
 * 1. Posição deve estar vazia (sem sobreposição)
 * 2. Deve ter ao menos um vizinho ortogonal existente
 * 3. Todas as bordas em contato com vizinhos existentes devem bater exatamente
 */
export function canPlaceTile(board, tile, x, y) {
  const key = coordsToKey(x, y);
  if (board[key]) {
    return false; // Sobreposição
  }

  const neighbors = getOrthogonalNeighbors(board, x, y);
  let hasAdjacent = false;

  for (const dir of DIRECTIONS_ORDER) {
    const neighbor = neighbors[dir];
    if (neighbor) {
      hasAdjacent = true;
      const oppositeDir = OPPOSITE_DIRECTION[dir];
      // A borda da nova peça deve ser idêntica à borda oposta do vizinho
      if (tile.edges[dir] !== neighbor.edges[oppositeDir]) {
        return false;
      }
    }
  }

  return hasAdjacent;
}

/**
 * Encontra todas as posições válidas no tabuleiro para a peça atual
 */
export function getValidPlacements(board, tile) {
  const candidates = new Set();

  // Coleta todas as posições vazias adjacentes a alguma peça existente
  for (const key of Object.keys(board)) {
    const { x, y } = keyToCoords(key);
    for (const dir of DIRECTIONS_ORDER) {
      const offset = DIRECTION_OFFSETS[dir];
      const nx = x + offset.dx;
      const ny = y + offset.dy;
      const nKey = coordsToKey(nx, ny);
      if (!board[nKey]) {
        candidates.add(nKey);
      }
    }
  }

  const validPositions = [];
  for (const nKey of candidates) {
    const { x, y } = keyToCoords(nKey);
    if (canPlaceTile(board, tile, x, y)) {
      validPositions.push({ x, y });
    }
  }

  return validPositions;
}

/**
 * Calcula os limites atuais do tabuleiro com margem para renderização
 */
export function getBoardBounds(board, padding = 1) {
  const keys = Object.keys(board);
  if (keys.length === 0) {
    return { minX: -1, maxX: 1, minY: -1, maxY: 1 };
  }

  let minX = Infinity;
  let maxX = -Infinity;
  let minY = Infinity;
  let maxY = -Infinity;

  for (const key of keys) {
    const { x, y } = keyToCoords(key);
    if (x < minX) minX = x;
    if (x > maxX) maxX = x;
    if (y < minY) minY = y;
    if (y > maxY) maxY = y;
  }

  return {
    minX: minX - padding,
    maxX: maxX + padding,
    minY: minY - padding,
    maxY: maxY + padding
  };
}
