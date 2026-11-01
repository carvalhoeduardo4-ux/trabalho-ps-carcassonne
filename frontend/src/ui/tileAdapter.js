import { DIRECTIONS_ORDER, ROTATIONS } from './constants.js';

/** Gira um lado no sentido horário (mesma convenção do backend). */
export function rotateSide(side, rotation) {
  const steps = Math.max(0, ROTATIONS.indexOf(rotation));
  return DIRECTIONS_ORDER[(DIRECTIONS_ORDER.indexOf(side) + steps) % 4];
}

const LANE_PRIORITY = ['CENTER', 'LEFT', 'RIGHT'];

/**
 * Converte as features do backend (sem rotação) no formato que o TileSvg desenha:
 * { edges, cities, hasMonastery } já considerando a rotação.
 */
export function toRenderTile(features = [], rotation = 'DEG_0') {
  const edges = {};
  DIRECTIONS_ORDER.forEach(d => { edges[d] = 'FIELD'; });

  features.forEach(f => {
    if (f.type !== 'CITY' && f.type !== 'ROAD') return;
    f.edgeContacts.forEach(c => {
      const side = rotateSide(c.side, rotation);
      const current = edges[side];
      const better = c.lane === 'CENTER' || current === 'FIELD';
      if (better) edges[side] = f.type;
    });
  });

  // Cada cidade da peça: lados que ela ocupa (já girados) e quantos brasões tem
  const cities = features
    .filter(f => f.type === 'CITY')
    .map(f => ({
      sides: [...new Set(f.edgeContacts.map(c => rotateSide(c.side, rotation)))],
      shields: f.shieldCount || 0
    }));

  return {
    edges,
    cities,
    hasMonastery: features.some(f => f.type === 'MONASTERY')
  };
}

/** Posição visual do meeple dentro da peça, a partir da feature ocupada. */
export function meepleSegment(features = [], featureId, rotation = 'DEG_0') {
  const feature = features.find(f => f.id === featureId);
  if (!feature || feature.type === 'MONASTERY' || feature.edgeContacts.length === 0) return 'CENTER';
  const contacts = [...feature.edgeContacts].sort(
    (a, b) => LANE_PRIORITY.indexOf(a.lane) - LANE_PRIORITY.indexOf(b.lane)
  );
  return rotateSide(contacts[0].side, rotation);
}

export const FEATURE_LABELS = { CITY: 'Cidade', ROAD: 'Estrada', MONASTERY: 'Mosteiro', FIELD: 'Campo' };

/** Rótulo legível para um `FeatureRef` na peça atual. */
export function featureLabel(features = [], featureId, rotation = 'DEG_0') {
  const feature = features.find(f => f.id === featureId);
  if (!feature) return `Região ${featureId}`;
  const type = FEATURE_LABELS[feature.type] || feature.type;
  const seg = meepleSegment(features, featureId, rotation);
  const dir = { NORTH: 'Norte', EAST: 'Leste', SOUTH: 'Sul', WEST: 'Oeste', CENTER: 'Centro' }[seg];
  return `${type} (${dir})`;
}
