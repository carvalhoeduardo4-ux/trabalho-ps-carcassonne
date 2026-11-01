export const Direction = {
  NORTH: 'NORTH',
  EAST: 'EAST',
  SOUTH: 'SOUTH',
  WEST: 'WEST'
};

export const DIRECTIONS_ORDER = [Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST];

export const MeeplePosition = {
  NORTH: 'NORTH',
  EAST: 'EAST',
  SOUTH: 'SOUTH',
  WEST: 'WEST',
  CENTER: 'CENTER'
};

/** Fases do turno devolvidas pelo backend (`phase`). */
export const TurnPhase = {
  DRAW_TILE: 'DRAW_TILE',
  PLACE_TILE: 'PLACE_TILE',
  PLACE_MEEPLE: 'PLACE_MEEPLE',
  GAME_OVER: 'GAME_OVER'
};

/** Rotações do backend, no sentido horário. */
export const ROTATIONS = ['DEG_0', 'DEG_90', 'DEG_180', 'DEG_270'];

export const rotationDegrees = (rotation) => ROTATIONS.indexOf(rotation) * 90;

/** `id` é o nome de `PlayerColor` no backend. */
export const PLAYER_COLORS = [
  { id: 'RED', name: 'Vermelho', hex: '#ef4444', bgHex: '#fee2e2', textHex: '#991b1b' },
  { id: 'BLUE', name: 'Azul', hex: '#3b82f6', bgHex: '#dbeafe', textHex: '#1e40af' },
  { id: 'GREEN', name: 'Verde', hex: '#10b981', bgHex: '#d1fae5', textHex: '#065f46' },
  { id: 'YELLOW', name: 'Amarelo', hex: '#f59e0b', bgHex: '#fef3c7', textHex: '#92400e' },
  { id: 'BLACK', name: 'Preto', hex: '#374151', bgHex: '#e5e7eb', textHex: '#111827' }
];

export const colorById = (id) => PLAYER_COLORS.find(c => c.id === id) || PLAYER_COLORS[0];
