/**
 * Tipos e Constantes do Núcleo do Carcassonne
 */

export const EdgeType = {
  FIELD: 'FIELD',
  ROAD: 'ROAD',
  CITY: 'CITY'
};

export const Direction = {
  NORTH: 'NORTH',
  EAST: 'EAST',
  SOUTH: 'SOUTH',
  WEST: 'WEST'
};

export const DIRECTIONS_ORDER = [
  Direction.NORTH,
  Direction.EAST,
  Direction.SOUTH,
  Direction.WEST
];

export const OPPOSITE_DIRECTION = {
  [Direction.NORTH]: Direction.SOUTH,
  [Direction.EAST]: Direction.WEST,
  [Direction.SOUTH]: Direction.NORTH,
  [Direction.WEST]: Direction.EAST
};

export const DIRECTION_OFFSETS = {
  [Direction.NORTH]: { dx: 0, dy: 1 },
  [Direction.EAST]: { dx: 1, dy: 0 },
  [Direction.SOUTH]: { dx: 0, dy: -1 },
  [Direction.WEST]: { dx: -1, dy: 0 }
};

export const MeeplePosition = {
  NORTH: 'NORTH',
  EAST: 'EAST',
  SOUTH: 'SOUTH',
  WEST: 'WEST',
  CENTER: 'CENTER'
};

export const TurnPhase = {
  PLACE_TILE: 'PLACE_TILE',
  PLACE_MEEPLE: 'PLACE_MEEPLE',
  GAME_OVER: 'GAME_OVER'
};

export const PLAYER_COLORS = [
  { id: 'red', name: 'Vermelho', hex: '#ef4444', bgHex: '#fee2e2', textHex: '#991b1b' },
  { id: 'blue', name: 'Azul', hex: '#3b82f6', bgHex: '#dbeafe', textHex: '#1e40af' },
  { id: 'green', name: 'Verde', hex: '#10b981', bgHex: '#d1fae5', textHex: '#065f46' },
  { id: 'yellow', name: 'Amarelo', hex: '#f59e0b', bgHex: '#fef3c7', textHex: '#92400e' },
  { id: 'purple', name: 'Roxo', hex: '#8b5cf6', bgHex: '#ede9fe', textHex: '#5b21b6' }
];
