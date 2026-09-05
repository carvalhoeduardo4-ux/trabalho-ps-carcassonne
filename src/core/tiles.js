import { EdgeType, Direction, DIRECTIONS_ORDER } from './types.js';

/**
 * Catálogo de tipos de peças do Carcassonne Simplificado
 * Cada tipo define:
 * - id: Identificador único do tipo
 * - name: Nome descritivo em português
 * - edges: { NORTH, EAST, SOUTH, WEST }
 * - cityGroups: Grupos de bordas de cidade conectadas internamente
 * - roadGroups: Grupos de bordas de estrada conectadas internamente
 * - hasMonastery: Se possui mosteiro no centro
 * - count: Quantidade recomendada no monte inicial
 */

export const TILE_DEFINITIONS = {
  // Peça Inicial Clássica: Cidade no Norte, Estrada de Leste a Oeste, Campo no Sul
  STARTER: {
    id: 'STARTER',
    name: 'Cidade com Estrada Reta',
    edges: {
      [Direction.NORTH]: EdgeType.CITY,
      [Direction.EAST]: EdgeType.ROAD,
      [Direction.SOUTH]: EdgeType.FIELD,
      [Direction.WEST]: EdgeType.ROAD
    },
    cityGroups: [[Direction.NORTH]],
    roadGroups: [[Direction.EAST, Direction.WEST]],
    hasMonastery: false,
    count: 0 // Não vai no monte, vai direto na mesa em (0,0)
  },

  // Estrada Reta
  ROAD_STRAIGHT: {
    id: 'ROAD_STRAIGHT',
    name: 'Estrada Reta',
    edges: {
      [Direction.NORTH]: EdgeType.FIELD,
      [Direction.EAST]: EdgeType.ROAD,
      [Direction.SOUTH]: EdgeType.FIELD,
      [Direction.WEST]: EdgeType.ROAD
    },
    cityGroups: [],
    roadGroups: [[Direction.EAST, Direction.WEST]],
    hasMonastery: false,
    count: 5
  },

  // Curva de Estrada
  ROAD_CURVE: {
    id: 'ROAD_CURVE',
    name: 'Curva de Estrada',
    edges: {
      [Direction.NORTH]: EdgeType.FIELD,
      [Direction.EAST]: EdgeType.ROAD,
      [Direction.SOUTH]: EdgeType.ROAD,
      [Direction.WEST]: EdgeType.FIELD
    },
    cityGroups: [],
    roadGroups: [[Direction.EAST, Direction.SOUTH]],
    hasMonastery: false,
    count: 5
  },

  // Bifurcação em T (3 estradas se encontram e terminam)
  ROAD_T_JUNCTION: {
    id: 'ROAD_T_JUNCTION',
    name: 'Bifurcação de Estradas (T)',
    edges: {
      [Direction.NORTH]: EdgeType.FIELD,
      [Direction.EAST]: EdgeType.ROAD,
      [Direction.SOUTH]: EdgeType.ROAD,
      [Direction.WEST]: EdgeType.ROAD
    },
    cityGroups: [],
    roadGroups: [[Direction.EAST], [Direction.SOUTH], [Direction.WEST]],
    hasMonastery: false,
    count: 3
  },

  // Cruzamento de 4 vias (todas terminam no centro)
  ROAD_CROSSROADS: {
    id: 'ROAD_CROSSROADS',
    name: 'Cruzamento de 4 Estradas',
    edges: {
      [Direction.NORTH]: EdgeType.ROAD,
      [Direction.EAST]: EdgeType.ROAD,
      [Direction.SOUTH]: EdgeType.ROAD,
      [Direction.WEST]: EdgeType.ROAD
    },
    cityGroups: [],
    roadGroups: [[Direction.NORTH], [Direction.EAST], [Direction.SOUTH], [Direction.WEST]],
    hasMonastery: false,
    count: 1
  },

  // Cidade em 1 Borda com Campo
  CITY_EDGE: {
    id: 'CITY_EDGE',
    name: 'Muralha de Cidade no Campo',
    edges: {
      [Direction.NORTH]: EdgeType.CITY,
      [Direction.EAST]: EdgeType.FIELD,
      [Direction.SOUTH]: EdgeType.FIELD,
      [Direction.WEST]: EdgeType.FIELD
    },
    cityGroups: [[Direction.NORTH]],
    roadGroups: [],
    hasMonastery: false,
    count: 4
  },

  // Cidade em 1 Borda com Estrada Reta
  CITY_EDGE_ROAD: {
    id: 'CITY_EDGE_ROAD',
    name: 'Cidade com Estrada Reta',
    edges: {
      [Direction.NORTH]: EdgeType.CITY,
      [Direction.EAST]: EdgeType.ROAD,
      [Direction.SOUTH]: EdgeType.FIELD,
      [Direction.WEST]: EdgeType.ROAD
    },
    cityGroups: [[Direction.NORTH]],
    roadGroups: [[Direction.EAST, Direction.WEST]],
    hasMonastery: false,
    count: 3
  },

  // Cidade em Canto (2 bordas conectadas)
  CITY_CORNER: {
    id: 'CITY_CORNER',
    name: 'Esquina de Cidade',
    edges: {
      [Direction.NORTH]: EdgeType.CITY,
      [Direction.EAST]: EdgeType.CITY,
      [Direction.SOUTH]: EdgeType.FIELD,
      [Direction.WEST]: EdgeType.FIELD
    },
    cityGroups: [[Direction.NORTH, Direction.EAST]],
    roadGroups: [],
    hasMonastery: false,
    count: 3
  },

  // Mosteiro Isolado no Campo
  MONASTERY_ISOLATED: {
    id: 'MONASTERY_ISOLATED',
    name: 'Mosteiro no Campo',
    edges: {
      [Direction.NORTH]: EdgeType.FIELD,
      [Direction.EAST]: EdgeType.FIELD,
      [Direction.SOUTH]: EdgeType.FIELD,
      [Direction.WEST]: EdgeType.FIELD
    },
    cityGroups: [],
    roadGroups: [],
    hasMonastery: true,
    count: 2
  },

  // Mosteiro com Estrada de Acesso
  MONASTERY_ROAD: {
    id: 'MONASTERY_ROAD',
    name: 'Mosteiro com Estrada',
    edges: {
      [Direction.NORTH]: EdgeType.FIELD,
      [Direction.EAST]: EdgeType.FIELD,
      [Direction.SOUTH]: EdgeType.ROAD,
      [Direction.WEST]: EdgeType.FIELD
    },
    cityGroups: [],
    roadGroups: [[Direction.SOUTH]],
    hasMonastery: true,
    count: 3
  }
};

/**
 * Rotaciona uma direção em 90° no sentido horário N vezes (steps)
 */
export function rotateDirection(direction, steps = 1) {
  const currentIndex = DIRECTIONS_ORDER.indexOf(direction);
  const newIndex = (currentIndex + (steps % 4) + 4) % 4;
  return DIRECTIONS_ORDER[newIndex];
}

/**
 * Retorna as bordas e conexões rotacionadas em N graus (0, 90, 180, 270)
 */
export function getRotatedTile(tileDefinition, rotation = 0) {
  const steps = Math.floor((rotation % 360) / 90);
  if (steps === 0) {
    return {
      ...tileDefinition,
      rotation: 0
    };
  }

  // Rotacionar bordas
  const rotatedEdges = {};
  for (const dir of DIRECTIONS_ORDER) {
    const originalDir = rotateDirection(dir, -steps);
    rotatedEdges[dir] = tileDefinition.edges[originalDir];
  }

  // Rotacionar grupos de cidades
  const rotatedCityGroups = (tileDefinition.cityGroups || []).map(group =>
    group.map(dir => rotateDirection(dir, steps))
  );

  // Rotacionar grupos de estradas
  const rotatedRoadGroups = (tileDefinition.roadGroups || []).map(group =>
    group.map(dir => rotateDirection(dir, steps))
  );

  return {
    ...tileDefinition,
    rotation: ((rotation % 360) + 360) % 360,
    edges: rotatedEdges,
    cityGroups: rotatedCityGroups,
    roadGroups: rotatedRoadGroups
  };
}

/**
 * Gera um baralho completo embaralhado
 */
export function createDeck() {
  const deck = [];
  let uid = 1;

  for (const key of Object.keys(TILE_DEFINITIONS)) {
    const def = TILE_DEFINITIONS[key];
    for (let i = 0; i < def.count; i++) {
      deck.push({
        uid: `tile_${uid++}`,
        typeId: def.id,
        name: def.name,
        baseDefinition: def,
        edges: { ...def.edges },
        cityGroups: def.cityGroups.map(g => [...g]),
        roadGroups: def.roadGroups.map(g => [...g]),
        hasMonastery: def.hasMonastery,
        rotation: 0
      });
    }
  }

  // Embaralhar (Fisher-Yates)
  for (let i = deck.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [deck[i], deck[j]] = [deck[j], deck[i]];
  }

  return deck;
}

/**
 * Cria a peça inicial posicionada em (0,0)
 */
export function createStarterTile() {
  const def = TILE_DEFINITIONS.STARTER;
  return {
    uid: 'tile_starter',
    typeId: def.id,
    name: def.name,
    baseDefinition: def,
    edges: { ...def.edges },
    cityGroups: def.cityGroups.map(g => [...g]),
    roadGroups: def.roadGroups.map(g => [...g]),
    hasMonastery: def.hasMonastery,
    rotation: 0,
    x: 0,
    y: 0,
    meeples: [] // [{ playerIndex, segment }]
  };
}
