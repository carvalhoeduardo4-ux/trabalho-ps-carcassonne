import { toRenderTile, meepleSegment } from './tileAdapter.js';

const positionKey = (x, y) => `${x},${y}`;

/**
 * Preserva a identidade das tiles que não mudaram, mesmo quando o backend
 * reidrata board/features/meeples em arrays e objetos novos.
 *
 * O ghost de meeple não entra na assinatura: ele é uma camada independente.
 */
export function reconcileBoardTiles(previousCache, board, meeples) {
  const meeplesByPosition = new Map();
  for (const meeple of meeples) {
    const key = positionKey(meeple.x, meeple.y);
    const occupants = meeplesByPosition.get(key);
    if (occupants) occupants.push(meeple);
    else meeplesByPosition.set(key, [meeple]);
  }

  const nextCache = new Map();
  const tiles = new Map();

  for (const sourceTile of board) {
    const key = positionKey(sourceTile.x, sourceTile.y);
    const occupants = meeplesByPosition.get(key) || [];
    const signature = JSON.stringify([
      sourceTile.rotation,
      sourceTile.features,
      occupants.map(meeple => [meeple.playerIndex, meeple.featureId])
    ]);
    const previous = previousCache.get(key);

    if (previous?.signature === signature) {
      nextCache.set(key, previous);
      tiles.set(key, previous.tile);
      continue;
    }

    const tileMeeples = occupants.map(meeple => ({
      segment: meepleSegment(sourceTile.features, meeple.featureId, sourceTile.rotation),
      playerIndex: meeple.playerIndex
    }));
    const tile = {
      ...toRenderTile(sourceTile.features, sourceTile.rotation),
      x: sourceTile.x,
      y: sourceTile.y,
      __sourceFeatures: sourceTile.features,
      __sourceRotation: sourceTile.rotation,
      meeples: tileMeeples
    };
    const entry = { signature, tile };
    nextCache.set(key, entry);
    tiles.set(key, tile);
  }

  return { cache: nextCache, tiles };
}
