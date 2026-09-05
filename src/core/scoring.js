import { Direction, DIRECTIONS_ORDER, OPPOSITE_DIRECTION, DIRECTION_OFFSETS, MeeplePosition } from './types.js';
import { coordsToKey, keyToCoords } from './board.js';

/**
 * Utilitário: encontra o grupo de bordas (cidade ou estrada) que contém uma determinada direção
 */
function findGroupIndex(groups, direction) {
  for (let i = 0; i < groups.length; i++) {
    if (groups[i].includes(direction)) {
      return i;
    }
  }
  return -1;
}

/**
 * Executa BFS para rastrear uma estrutura conectada (Cidade ou Estrada)
 * a partir de uma peça (startX, startY) e um grupo específico de bordas.
 * 
 * Retorna:
 * {
 *   isClosed: boolean,
 *   tilesCount: number,
 *   uniqueTileKeys: Set<string>,
 *   meeples: Array<{ playerIndex: number, tileKey: string, segment: string }>
 * }
 */
export function traceStructure(board, startX, startY, structureType, initialGroup) {
  const visitedGroups = new Set(); // formato: "x,y:groupIndex"
  const uniqueTileKeys = new Set();
  const meeples = [];
  let isClosed = true;

  const startKey = coordsToKey(startX, startY);
  const startTile = board[startKey];
  if (!startTile) {
    return { isClosed: false, tilesCount: 0, uniqueTileKeys, meeples };
  }

  const groupsList = structureType === 'CITY' ? startTile.cityGroups : startTile.roadGroups;
  const startGroupIndex = groupsList.findIndex(g => 
    g.length === initialGroup.length && g.every(d => initialGroup.includes(d))
  );

  const queue = [{ x: startX, y: startY, groupIndex: startGroupIndex >= 0 ? startGroupIndex : 0 }];

  while (queue.length > 0) {
    const current = queue.shift();
    const currentKey = coordsToKey(current.x, current.y);
    const stateKey = `${currentKey}:${current.groupIndex}`;

    if (visitedGroups.has(stateKey)) {
      continue;
    }
    visitedGroups.add(stateKey);
    uniqueTileKeys.add(currentKey);

    const tile = board[currentKey];
    if (!tile) continue;

    const currentGroups = structureType === 'CITY' ? tile.cityGroups : tile.roadGroups;
    const edgeGroup = currentGroups[current.groupIndex] || [];

    // Coletar meeples colocados neste segmento
    if (tile.meeples && tile.meeples.length > 0) {
      for (const m of tile.meeples) {
        if (edgeGroup.includes(m.segment)) {
          meeples.push({ ...m, tileKey: currentKey });
        }
      }
    }

    // Para cada borda deste grupo, verificar o vizinho correspondente
    for (const dir of edgeGroup) {
      const offset = DIRECTION_OFFSETS[dir];
      const nx = current.x + offset.dx;
      const ny = current.y + offset.dy;
      const neighborKey = coordsToKey(nx, ny);
      const neighbor = board[neighborKey];

      if (!neighbor) {
        // Borda aberta para o vazio: a estrutura não está fechada!
        isClosed = false;
      } else {
        // Vizinho existe: encontrar o grupo correspondente na borda oposta
        const oppDir = OPPOSITE_DIRECTION[dir];
        const neighborGroups = structureType === 'CITY' ? neighbor.cityGroups : neighbor.roadGroups;
        const neighborGroupIndex = findGroupIndex(neighborGroups, oppDir);

        if (neighborGroupIndex >= 0) {
          const neighborStateKey = `${neighborKey}:${neighborGroupIndex}`;
          if (!visitedGroups.has(neighborStateKey)) {
            queue.push({ x: nx, y: ny, groupIndex: neighborGroupIndex });
          }
        }
      }
    }
  }

  return {
    isClosed,
    tilesCount: uniqueTileKeys.size,
    uniqueTileKeys,
    meeples
  };
}

/**
 * Valida se um Meeple pode ser colocado em um segmento da peça recém-colocada.
 * Regra: Impede meeple se a estrutura conectada já tiver meeple de qualquer jogador.
 */
export function canPlaceMeepleOnSegment(board, x, y, segment, playerMeeplesAvailable) {
  if (playerMeeplesAvailable <= 0) {
    return { canPlace: false, reason: 'Sem meeples restantes no estoque.' };
  }

  const tileKey = coordsToKey(x, y);
  const tile = board[tileKey];
  if (!tile) {
    return { canPlace: false, reason: 'Peça não encontrada.' };
  }

  // Mosteiro no centro
  if (segment === MeeplePosition.CENTER) {
    if (!tile.hasMonastery) {
      return { canPlace: false, reason: 'Esta peça não possui mosteiro.' };
    }
    const hasCenterMeeple = (tile.meeples || []).some(m => m.segment === MeeplePosition.CENTER);
    if (hasCenterMeeple) {
      return { canPlace: false, reason: 'Mosteiro já ocupado.' };
    }
    return { canPlace: true, structureType: 'MONASTERY' };
  }

  // Cidade
  const cityGroup = (tile.cityGroups || []).find(g => g.includes(segment));
  if (cityGroup) {
    const trace = traceStructure(board, x, y, 'CITY', cityGroup);
    if (trace.meeples.length > 0) {
      return { canPlace: false, reason: 'Esta cidade já possui meeple em sua rede conectada.' };
    }
    return { canPlace: true, structureType: 'CITY' };
  }

  // Estrada
  const roadGroup = (tile.roadGroups || []).find(g => g.includes(segment));
  if (roadGroup) {
    const trace = traceStructure(board, x, y, 'ROAD', roadGroup);
    if (trace.meeples.length > 0) {
      return { canPlace: false, reason: 'Esta estrada já possui meeple em sua rede conectada.' };
    }
    return { canPlace: true, structureType: 'ROAD' };
  }

  return { canPlace: false, reason: 'Não é possível colocar meeple em campo nesta versão.' };
}

/**
 * Retorna todos os segmentos válidos onde o jogador pode colocar um Meeple na peça colocada
 */
export function getAvailableMeepleOptions(board, x, y, playerMeeplesAvailable) {
  if (playerMeeplesAvailable <= 0) return [];

  const tile = board[coordsToKey(x, y)];
  if (!tile) return [];

  const options = [];

  // Mosteiro
  if (tile.hasMonastery) {
    const validation = canPlaceMeepleOnSegment(board, x, y, MeeplePosition.CENTER, playerMeeplesAvailable);
    if (validation.canPlace) {
      options.push({ segment: MeeplePosition.CENTER, label: 'Mosteiro (Centro)', type: 'MONASTERY' });
    }
  }

  // Cidades (um botão por grupo de cidade)
  const evaluatedCityGroups = new Set();
  for (const group of tile.cityGroups || []) {
    const rep = group[0];
    if (!evaluatedCityGroups.has(rep)) {
      const validation = canPlaceMeepleOnSegment(board, x, y, rep, playerMeeplesAvailable);
      if (validation.canPlace) {
        options.push({
          segment: rep,
          label: `Cidade (${translateSegment(rep)})`,
          type: 'CITY'
        });
      }
      group.forEach(d => evaluatedCityGroups.add(d));
    }
  }

  // Estradas (um botão por grupo de estrada)
  const evaluatedRoadGroups = new Set();
  for (const group of tile.roadGroups || []) {
    const rep = group[0];
    if (!evaluatedRoadGroups.has(rep)) {
      const validation = canPlaceMeepleOnSegment(board, x, y, rep, playerMeeplesAvailable);
      if (validation.canPlace) {
        options.push({
          segment: rep,
          label: `Estrada (${translateSegment(rep)})`,
          type: 'ROAD'
        });
      }
      group.forEach(d => evaluatedRoadGroups.add(d));
    }
  }

  return options;
}

function translateSegment(seg) {
  switch (seg) {
    case 'NORTH': return 'Norte';
    case 'EAST': return 'Leste';
    case 'SOUTH': return 'Sul';
    case 'WEST': return 'Oeste';
    case 'CENTER': return 'Centro';
    default: return seg;
  }
}

/**
 * Atribui pontos ao jogador com mais meeples (ou a ambos se empatar)
 */
function calculateMajorityPoints(meeples, points) {
  if (meeples.length === 0) return [];

  const countByPlayer = {};
  for (const m of meeples) {
    countByPlayer[m.playerIndex] = (countByPlayer[m.playerIndex] || 0) + 1;
  }

  let maxCount = 0;
  for (const pIdx in countByPlayer) {
    if (countByPlayer[pIdx] > maxCount) {
      maxCount = countByPlayer[pIdx];
    }
  }

  const winners = [];
  for (const pIdx in countByPlayer) {
    if (countByPlayer[pIdx] === maxCount) {
      winners.push(Number(pIdx));
    }
  }

  return winners.map(playerIndex => ({
    playerIndex,
    points
  }));
}

/**
 * Avalia fechamento de estruturas e calcula pontuações imediatas
 * após a colocação de uma peça (e meeple opcional).
 * 
 * Retorna os pontos ganhos, meeples devolvidos e mensagens de evento.
 */
export function evaluateImmediateScoring(board, lastPlacedX, lastPlacedY) {
  const events = [];
  const pointsAwarded = {}; // playerIndex -> points
  const meeplesToReturn = []; // Array de { playerIndex, tileKey, segment }
  const processedStructureKeys = new Set();

  // 1. Verificar todos os mosteiros ao redor (incluindo o recém-colocado e vizinhos 3x3)
  for (let dx = -1; dx <= 1; dx++) {
    for (let dy = -1; dy <= 1; dy++) {
      const mx = lastPlacedX + dx;
      const my = lastPlacedY + dy;
      const mKey = coordsToKey(mx, my);
      const mTile = board[mKey];

      if (mTile && mTile.hasMonastery && mTile.meeples) {
        const centerMeeple = mTile.meeples.find(m => m.segment === MeeplePosition.CENTER);
        if (centerMeeple) {
          // Checar se os 8 vizinhos estão preenchidos
          let neighborsCount = 0;
          for (let ndx = -1; ndx <= 1; ndx++) {
            for (let ndy = -1; ndy <= 1; ndy++) {
              if (ndx === 0 && ndy === 0) continue;
              if (board[coordsToKey(mx + ndx, my + ndy)]) {
                neighborsCount++;
              }
            }
          }

          if (neighborsCount === 8) {
            // Mosteiro fechado! 9 pontos
            const pIdx = centerMeeple.playerIndex;
            pointsAwarded[pIdx] = (pointsAwarded[pIdx] || 0) + 9;
            meeplesToReturn.push({ playerIndex: pIdx, tileKey: mKey, segment: MeeplePosition.CENTER });
            events.push({
              type: 'MONASTERY_COMPLETED',
              playerIndex: pIdx,
              points: 9,
              message: `Jogador completou um Mosteiro em (${mx}, ${my}) e ganhou 9 pontos!`
            });
          }
        }
      }
    }
  }

  // 2. Verificar cidades e estradas conectadas à peça recém-colocada
  const lastKey = coordsToKey(lastPlacedX, lastPlacedY);
  const lastTile = board[lastKey];

  if (lastTile) {
    // Verificar cidades
    for (const group of lastTile.cityGroups || []) {
      const rep = group[0];
      const trace = traceStructure(board, lastPlacedX, lastPlacedY, 'CITY', group);
      const structureId = `CITY:[${Array.from(trace.uniqueTileKeys).sort().join(';')}]`;

      if (trace.isClosed && !processedStructureKeys.has(structureId) && trace.meeples.length > 0) {
        processedStructureKeys.add(structureId);
        const cityPoints = trace.tilesCount * 2;
        const winners = calculateMajorityPoints(trace.meeples, cityPoints);

        for (const w of winners) {
          pointsAwarded[w.playerIndex] = (pointsAwarded[w.playerIndex] || 0) + w.points;
        }

        trace.meeples.forEach(m => meeplesToReturn.push(m));
        events.push({
          type: 'CITY_COMPLETED',
          winners: winners.map(w => w.playerIndex),
          points: cityPoints,
          tilesCount: trace.tilesCount,
          message: `Cidade fechada com ${trace.tilesCount} peças (+${cityPoints} pontos)!`
        });
      }
    }

    // Verificar estradas
    for (const group of lastTile.roadGroups || []) {
      const rep = group[0];
      const trace = traceStructure(board, lastPlacedX, lastPlacedY, 'ROAD', group);
      const structureId = `ROAD:[${Array.from(trace.uniqueTileKeys).sort().join(';')}]`;

      if (trace.isClosed && !processedStructureKeys.has(structureId) && trace.meeples.length > 0) {
        processedStructureKeys.add(structureId);
        const roadPoints = trace.tilesCount * 1;
        const winners = calculateMajorityPoints(trace.meeples, roadPoints);

        for (const w of winners) {
          pointsAwarded[w.playerIndex] = (pointsAwarded[w.playerIndex] || 0) + w.points;
        }

        trace.meeples.forEach(m => meeplesToReturn.push(m));
        events.push({
          type: 'ROAD_COMPLETED',
          winners: winners.map(w => w.playerIndex),
          points: roadPoints,
          tilesCount: trace.tilesCount,
          message: `Estrada fechada com ${trace.tilesCount} peças (+${roadPoints} pontos)!`
        });
      }
    }
  }

  // Devolver os meeples removendo-os do tabuleiro
  for (const m of meeplesToReturn) {
    const t = board[m.tileKey];
    if (t && t.meeples) {
      t.meeples = t.meeples.filter(meeple => 
        !(meeple.playerIndex === m.playerIndex && meeple.segment === m.segment)
      );
    }
  }

  return {
    pointsAwarded,
    meeplesReturned: meeplesToReturn,
    events
  };
}

/**
 * Pontuação final do fim de jogo (quando o monte de peças zera).
 * Pontua estruturas incompletas:
 * - Cidade incompleta: 1 ponto por peça
 * - Estrada incompleta: 1 ponto por peça
 * - Mosteiro incompleto: 1 ponto pelo mosteiro + 1 ponto por vizinho colocado
 */
export function evaluateFinalScoring(board) {
  const pointsAwarded = {};
  const events = [];
  const processedStructureKeys = new Set();

  for (const key of Object.keys(board)) {
    const tile = board[key];
    const { x, y } = keyToCoords(key);

    // Mosteiros incompletos
    if (tile.hasMonastery && tile.meeples) {
      const centerMeeple = tile.meeples.find(m => m.segment === MeeplePosition.CENTER);
      if (centerMeeple) {
        let count = 1; // Próprio mosteiro
        for (let dx = -1; dx <= 1; dx++) {
          for (let dy = -1; dy <= 1; dy++) {
            if (dx === 0 && dy === 0) continue;
            if (board[coordsToKey(x + dx, y + dy)]) {
              count++;
            }
          }
        }
        const pIdx = centerMeeple.playerIndex;
        pointsAwarded[pIdx] = (pointsAwarded[pIdx] || 0) + count;
        events.push({
          type: 'FINAL_MONASTERY',
          playerIndex: pIdx,
          points: count,
          message: `Mosteiro incompleto pontuou ${count} pontos para o jogador.`
        });
      }
    }

    // Cidades incompletas
    for (const group of tile.cityGroups || []) {
      const trace = traceStructure(board, x, y, 'CITY', group);
      const structureId = `FINAL_CITY:[${Array.from(trace.uniqueTileKeys).sort().join(';')}]`;
      if (!processedStructureKeys.has(structureId) && trace.meeples.length > 0) {
        processedStructureKeys.add(structureId);
        const cityPoints = trace.tilesCount * 1; // 1 pt por peça no final
        const winners = calculateMajorityPoints(trace.meeples, cityPoints);
        for (const w of winners) {
          pointsAwarded[w.playerIndex] = (pointsAwarded[w.playerIndex] || 0) + w.points;
        }
        events.push({
          type: 'FINAL_CITY',
          winners: winners.map(w => w.playerIndex),
          points: cityPoints,
          message: `Cidade incompleta com ${trace.tilesCount} peças rendeu ${cityPoints} pontos finais.`
        });
      }
    }

    // Estradas incompletas
    for (const group of tile.roadGroups || []) {
      const trace = traceStructure(board, x, y, 'ROAD', group);
      const structureId = `FINAL_ROAD:[${Array.from(trace.uniqueTileKeys).sort().join(';')}]`;
      if (!processedStructureKeys.has(structureId) && trace.meeples.length > 0) {
        processedStructureKeys.add(structureId);
        const roadPoints = trace.tilesCount * 1;
        const winners = calculateMajorityPoints(trace.meeples, roadPoints);
        for (const w of winners) {
          pointsAwarded[w.playerIndex] = (pointsAwarded[w.playerIndex] || 0) + w.points;
        }
        events.push({
          type: 'FINAL_ROAD',
          winners: winners.map(w => w.playerIndex),
          points: roadPoints,
          message: `Estrada incompleta com ${trace.tilesCount} peças rendeu ${roadPoints} pontos finais.`
        });
      }
    }
  }

  return {
    pointsAwarded,
    events
  };
}
