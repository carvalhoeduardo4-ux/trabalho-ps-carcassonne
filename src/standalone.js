/**
 * Carcassonne Digital — Standalone Bundle (Medieval Layout)
 * Fullscreen board + Top HUD + Bottom Action Bar
 */

function loadStandaloneApp() {
  if (!window.React || !window.ReactDOM) {
    console.error('React não carregado.');
    return;
  }

  const { createElement: e, useState, useEffect, useRef } = window.React;

  // ==========================================
  // 1. Constantes e Tipos
  // ==========================================
  const Direction = { NORTH: 'NORTH', EAST: 'EAST', SOUTH: 'SOUTH', WEST: 'WEST' };
  const DIRECTIONS_ORDER = [Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST];
  const OPPOSITE_DIRECTION = {
    [Direction.NORTH]: Direction.SOUTH,
    [Direction.EAST]: Direction.WEST,
    [Direction.SOUTH]: Direction.NORTH,
    [Direction.WEST]: Direction.EAST
  };
  const DIRECTION_OFFSETS = {
    [Direction.NORTH]: { dx: 0, dy: 1 },
    [Direction.EAST]: { dx: 1, dy: 0 },
    [Direction.SOUTH]: { dx: 0, dy: -1 },
    [Direction.WEST]: { dx: -1, dy: 0 }
  };
  const MeeplePosition = { NORTH: 'NORTH', EAST: 'EAST', SOUTH: 'SOUTH', WEST: 'WEST', CENTER: 'CENTER' };
  const TurnPhase = { PLACE_TILE: 'PLACE_TILE', PLACE_MEEPLE: 'PLACE_MEEPLE', GAME_OVER: 'GAME_OVER' };

  const PLAYER_COLORS = [
    { id: 'red', name: 'Vermelho', hex: '#dc2626', bgHex: '#fee2e2', textHex: '#991b1b' },
    { id: 'blue', name: 'Azul', hex: '#2563eb', bgHex: '#dbeafe', textHex: '#1e40af' },
    { id: 'green', name: 'Verde', hex: '#16a34a', bgHex: '#d1fae5', textHex: '#065f46' },
    { id: 'yellow', name: 'Amarelo', hex: '#eab308', bgHex: '#fef3c7', textHex: '#92400e' },
    { id: 'purple', name: 'Roxo', hex: '#9333ea', bgHex: '#ede9fe', textHex: '#5b21b6' }
  ];

  // ==========================================
  // 2. Definição de Peças
  // ==========================================
  const TILE_DEFINITIONS = {
    STARTER: {
      id: 'STARTER', name: 'Cidade + Estrada',
      edges: { [Direction.NORTH]: 'CITY', [Direction.EAST]: 'ROAD', [Direction.SOUTH]: 'FIELD', [Direction.WEST]: 'ROAD' },
      cityGroups: [[Direction.NORTH]], roadGroups: [[Direction.EAST, Direction.WEST]],
      hasMonastery: false, count: 0
    },
    ROAD_STRAIGHT: {
      id: 'ROAD_STRAIGHT', name: 'Estrada Reta',
      edges: { [Direction.NORTH]: 'FIELD', [Direction.EAST]: 'ROAD', [Direction.SOUTH]: 'FIELD', [Direction.WEST]: 'ROAD' },
      cityGroups: [], roadGroups: [[Direction.EAST, Direction.WEST]],
      hasMonastery: false, count: 6
    },
    ROAD_CURVE: {
      id: 'ROAD_CURVE', name: 'Curva de Estrada',
      edges: { [Direction.NORTH]: 'FIELD', [Direction.EAST]: 'ROAD', [Direction.SOUTH]: 'ROAD', [Direction.WEST]: 'FIELD' },
      cityGroups: [], roadGroups: [[Direction.EAST, Direction.SOUTH]],
      hasMonastery: false, count: 6
    },
    ROAD_T_JUNCTION: {
      id: 'ROAD_T_JUNCTION', name: 'Bifurcação (T)',
      edges: { [Direction.NORTH]: 'FIELD', [Direction.EAST]: 'ROAD', [Direction.SOUTH]: 'ROAD', [Direction.WEST]: 'ROAD' },
      cityGroups: [], roadGroups: [[Direction.EAST], [Direction.SOUTH], [Direction.WEST]],
      hasMonastery: false, count: 3
    },
    CITY_EDGE: {
      id: 'CITY_EDGE', name: 'Muralha',
      edges: { [Direction.NORTH]: 'CITY', [Direction.EAST]: 'FIELD', [Direction.SOUTH]: 'FIELD', [Direction.WEST]: 'FIELD' },
      cityGroups: [[Direction.NORTH]], roadGroups: [],
      hasMonastery: false, count: 4
    },
    CITY_EDGE_ROAD: {
      id: 'CITY_EDGE_ROAD', name: 'Cidade + Estrada',
      edges: { [Direction.NORTH]: 'CITY', [Direction.EAST]: 'ROAD', [Direction.SOUTH]: 'FIELD', [Direction.WEST]: 'ROAD' },
      cityGroups: [[Direction.NORTH]], roadGroups: [[Direction.EAST, Direction.WEST]],
      hasMonastery: false, count: 3
    },
    CITY_CORNER: {
      id: 'CITY_CORNER', name: 'Esquina de Cidade',
      edges: { [Direction.NORTH]: 'CITY', [Direction.EAST]: 'CITY', [Direction.SOUTH]: 'FIELD', [Direction.WEST]: 'FIELD' },
      cityGroups: [[Direction.NORTH, Direction.EAST]], roadGroups: [],
      hasMonastery: false, count: 3
    },
    MONASTERY_ROAD: {
      id: 'MONASTERY_ROAD', name: 'Mosteiro',
      edges: { [Direction.NORTH]: 'FIELD', [Direction.EAST]: 'FIELD', [Direction.SOUTH]: 'ROAD', [Direction.WEST]: 'FIELD' },
      cityGroups: [], roadGroups: [[Direction.SOUTH]],
      hasMonastery: true, count: 3
    }
  };

  function rotateDirection(direction, steps = 1) {
    const idx = DIRECTIONS_ORDER.indexOf(direction);
    return DIRECTIONS_ORDER[(idx + (steps % 4) + 4) % 4];
  }

  function getRotatedTile(tileDef, rotation = 0) {
    const steps = Math.floor((rotation % 360) / 90);
    if (steps === 0) return { ...tileDef, rotation: 0 };
    const rotatedEdges = {};
    for (const dir of DIRECTIONS_ORDER) rotatedEdges[dir] = tileDef.edges[rotateDirection(dir, -steps)];
    return {
      ...tileDef, rotation: ((rotation % 360) + 360) % 360,
      edges: rotatedEdges,
      cityGroups: (tileDef.cityGroups || []).map(g => g.map(d => rotateDirection(d, steps))),
      roadGroups: (tileDef.roadGroups || []).map(g => g.map(d => rotateDirection(d, steps)))
    };
  }

  function createDeck() {
    const deck = [];
    let uid = 1;
    for (const key of Object.keys(TILE_DEFINITIONS)) {
      const def = TILE_DEFINITIONS[key];
      for (let i = 0; i < def.count; i++) {
        deck.push({
          uid: `tile_${uid++}`, typeId: def.id, name: def.name,
          edges: { ...def.edges }, cityGroups: def.cityGroups.map(g => [...g]),
          roadGroups: def.roadGroups.map(g => [...g]), hasMonastery: def.hasMonastery, rotation: 0
        });
      }
    }
    for (let i = deck.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [deck[i], deck[j]] = [deck[j], deck[i]];
    }
    return deck;
  }

  // ==========================================
  // 3. Tabuleiro e Validação
  // ==========================================
  const coordsToKey = (x, y) => `${x},${y}`;
  const keyToCoords = (k) => { const [x, y] = k.split(',').map(Number); return { x, y }; };

  function canPlaceTile(board, tile, x, y) {
    if (board[coordsToKey(x, y)]) return false;
    let hasAdj = false;
    for (const dir of DIRECTIONS_ORDER) {
      const off = DIRECTION_OFFSETS[dir];
      const n = board[coordsToKey(x + off.dx, y + off.dy)];
      if (n) { hasAdj = true; if (tile.edges[dir] !== n.edges[OPPOSITE_DIRECTION[dir]]) return false; }
    }
    return hasAdj;
  }

  function getValidPlacements(board, tile) {
    const cands = new Set();
    for (const key of Object.keys(board)) {
      const { x, y } = keyToCoords(key);
      for (const dir of DIRECTIONS_ORDER) {
        const off = DIRECTION_OFFSETS[dir];
        const nk = coordsToKey(x + off.dx, y + off.dy);
        if (!board[nk]) cands.add(nk);
      }
    }
    const valid = [];
    for (const nk of cands) { const { x, y } = keyToCoords(nk); if (canPlaceTile(board, tile, x, y)) valid.push({ x, y }); }
    return valid;
  }

  function getBoardBounds(board, pad = 1) {
    let minX = 0, maxX = 0, minY = 0, maxY = 0;
    for (const key of Object.keys(board)) {
      const { x, y } = keyToCoords(key);
      if (x < minX) minX = x; if (x > maxX) maxX = x;
      if (y < minY) minY = y; if (y > maxY) maxY = y;
    }
    return { minX: minX - pad, maxX: maxX + pad, minY: minY - pad, maxY: maxY + pad };
  }

  // ==========================================
  // 4. Pontuação
  // ==========================================
  function traceStructure(board, sx, sy, type, initialGroup) {
    const visited = new Set(), tiles = new Set(), meeples = [];
    let closed = true;
    const tile = board[coordsToKey(sx, sy)];
    if (!tile) return { isClosed: false, tilesCount: 0, uniqueTileKeys: tiles, meeples };
    const groups = type === 'CITY' ? tile.cityGroups : tile.roadGroups;
    const gi = groups.findIndex(g => g.length === initialGroup.length && g.every(d => initialGroup.includes(d)));
    const queue = [{ x: sx, y: sy, groupIndex: gi >= 0 ? gi : 0 }];

    while (queue.length > 0) {
      const cur = queue.shift();
      const ck = coordsToKey(cur.x, cur.y);
      const sk = `${ck}:${cur.groupIndex}`;
      if (visited.has(sk)) continue;
      visited.add(sk); tiles.add(ck);
      const t = board[ck]; if (!t) continue;
      const cg = (type === 'CITY' ? t.cityGroups : t.roadGroups)[cur.groupIndex] || [];
      if (t.meeples) for (const m of t.meeples) if (cg.includes(m.segment)) meeples.push({ ...m, tileKey: ck });
      for (const dir of cg) {
        const off = DIRECTION_OFFSETS[dir];
        const nk = coordsToKey(cur.x + off.dx, cur.y + off.dy);
        const nb = board[nk];
        if (!nb) { closed = false; } else {
          const ng = (type === 'CITY' ? nb.cityGroups : nb.roadGroups);
          const ni = ng.findIndex(g => g.includes(OPPOSITE_DIRECTION[dir]));
          if (ni >= 0 && !visited.has(`${nk}:${ni}`)) queue.push({ x: cur.x + off.dx, y: cur.y + off.dy, groupIndex: ni });
        }
      }
    }
    return { isClosed: closed, tilesCount: tiles.size, uniqueTileKeys: tiles, meeples };
  }

  function getAvailableMeepleOptions(board, x, y, avail) {
    if (avail <= 0) return [];
    const tile = board[coordsToKey(x, y)]; if (!tile) return [];
    const opts = [];
    if (tile.hasMonastery && !(tile.meeples || []).some(m => m.segment === 'CENTER'))
      opts.push({ segment: 'CENTER', label: 'Mosteiro' });
    for (const g of tile.cityGroups || []) {
      const t = traceStructure(board, x, y, 'CITY', g);
      if (t.meeples.length === 0) opts.push({ segment: g[0], label: `Cidade (${g[0]})` });
    }
    for (const g of tile.roadGroups || []) {
      const t = traceStructure(board, x, y, 'ROAD', g);
      if (t.meeples.length === 0) opts.push({ segment: g[0], label: `Estrada (${g[0]})` });
    }
    return opts;
  }

  function calcMajority(meeples) {
    const cnt = {}; for (const m of meeples) cnt[m.playerIndex] = (cnt[m.playerIndex] || 0) + 1;
    let max = 0; for (const p in cnt) if (cnt[p] > max) max = cnt[p];
    return Object.keys(cnt).filter(p => cnt[p] === max).map(Number);
  }

  function evaluateScoring(board, lx, ly) {
    const pts = {}, ret = [], evts = [];
    // Mosteiros
    for (let dx = -1; dx <= 1; dx++) for (let dy = -1; dy <= 1; dy++) {
      const mx = lx + dx, my = ly + dy, mk = coordsToKey(mx, my), mt = board[mk];
      if (mt && mt.hasMonastery && mt.meeples) {
        const cm = mt.meeples.find(m => m.segment === 'CENTER');
        if (cm) {
          let cnt = 0;
          for (let a = -1; a <= 1; a++) for (let b = -1; b <= 1; b++) if (!(a === 0 && b === 0) && board[coordsToKey(mx + a, my + b)]) cnt++;
          if (cnt === 8) {
            pts[cm.playerIndex] = (pts[cm.playerIndex] || 0) + 9;
            ret.push({ playerIndex: cm.playerIndex, tileKey: mk, segment: 'CENTER' });
            evts.push(`⛪ Mosteiro completo! +9 pts`);
          }
        }
      }
    }
    const lt = board[coordsToKey(lx, ly)];
    if (lt) {
      for (const g of lt.cityGroups || []) {
        const t = traceStructure(board, lx, ly, 'CITY', g);
        if (t.isClosed && t.meeples.length > 0) {
          const p = t.tilesCount * 2;
          calcMajority(t.meeples).forEach(w => { pts[w] = (pts[w] || 0) + p; });
          t.meeples.forEach(m => ret.push(m));
          evts.push(`🏰 Cidade fechada (${t.tilesCount} peças)! +${p} pts`);
        }
      }
      for (const g of lt.roadGroups || []) {
        const t = traceStructure(board, lx, ly, 'ROAD', g);
        if (t.isClosed && t.meeples.length > 0) {
          const p = t.tilesCount;
          calcMajority(t.meeples).forEach(w => { pts[w] = (pts[w] || 0) + p; });
          t.meeples.forEach(m => ret.push(m));
          evts.push(`🛤️ Estrada fechada (${t.tilesCount} peças)! +${p} pts`);
        }
      }
    }
    for (const m of ret) {
      const t = board[m.tileKey];
      if (t && t.meeples) t.meeples = t.meeples.filter(i => !(i.playerIndex === m.playerIndex && i.segment === m.segment));
    }
    return { pointsAwarded: pts, meeplesReturned: ret, events: evts };
  }

  // ==========================================
  // 5. Componentes SVG
  // ==========================================
  function MeepleIcon({ colorHex = '#dc2626', size = 26 }) {
    return e('svg', { width: size, height: size, viewBox: '0 0 100 100', style: { filter: 'drop-shadow(0 2px 3px rgba(0,0,0,0.5))' } }, [
      e('circle', { key: 'h', cx: 50, cy: 22, r: 14, fill: colorHex, stroke: '#1a0f08', strokeWidth: 3 }),
      e('path', { key: 'b', d: 'M 35,36 C 30,36 18,48 16,56 C 14,64 26,66 30,58 L 36,50 L 32,88 C 32,94 44,94 46,88 L 50,68 L 54,88 C 56,94 68,94 68,88 L 64,50 L 70,58 C 74,66 86,64 84,56 C 82,48 70,36 65,36 Z', fill: colorHex, stroke: '#1a0f08', strokeWidth: 3, strokeLinejoin: 'round' })
    ]);
  }

  function TileSvg({ tile, size = 100, onClick, highlightValid, players = [] }) {
    if (!tile) {
      if (highlightValid) {
        return e('div', { className: 'valid-slot', style: { width: size, height: size }, onClick, title: 'Posicionar peça aqui' }, [
          e('div', { key: 'p', className: 'valid-pulse' }),
          e('span', { key: 'i', className: 'valid-plus-icon' }, '+')
        ]);
      }
      return e('div', { className: 'empty-grid-slot', style: { width: size, height: size } });
    }

    const edges = tile.edges || {};
    const sc = []; // SVG children

    // Background: lush green field
    sc.push(e('defs', { key: 'defs' }, [
      e('linearGradient', { key: 'g1', id: 'fieldGrad', x1: '0', y1: '0', x2: '1', y2: '1' }, [
        e('stop', { key: 's1', offset: '0%', stopColor: '#6abf4b' }),
        e('stop', { key: 's2', offset: '100%', stopColor: '#4a9e32' })
      ])
    ]));
    sc.push(e('rect', { key: 'bg', x: 0, y: 0, width: 100, height: 100, fill: 'url(#fieldGrad)' }));
    // Field texture lines
    for (let i = 0; i < 5; i++) {
      sc.push(e('line', { key: `fl${i}`, x1: 10 + i * 20, y1: 0, x2: 10 + i * 20, y2: 100, stroke: 'rgba(0,80,0,0.08)', strokeWidth: 1 }));
    }

    // Cities
    const cn = edges[Direction.NORTH] === 'CITY', ce = edges[Direction.EAST] === 'CITY';
    const cs = edges[Direction.SOUTH] === 'CITY', cw = edges[Direction.WEST] === 'CITY';
    const isCorner = (a, b, groups) => a && b && groups && groups[0] && groups[0].length >= 2;

    if (isCorner(cn, ce, tile.cityGroups)) {
      sc.push(e('path', { key: 'c-ne', d: 'M 30,0 L 100,0 L 100,70 Q 70,70 70,50 Q 70,30 50,30 Q 30,30 30,0 Z', fill: '#8B5E3C', stroke: '#5C3A1E', strokeWidth: 2 }));
      // Battlements
      sc.push(e('path', { key: 'bt-ne', d: 'M 30,0 L 35,8 L 45,8 L 45,0 M 55,0 L 55,8 L 65,8 L 65,0 M 75,0 L 75,8 L 85,8 L 85,0', fill: 'none', stroke: '#5C3A1E', strokeWidth: 1.5 }));
    } else {
      if (cn) {
        sc.push(e('path', { key: 'c-n', d: 'M 10,0 Q 20,32 50,32 Q 80,32 90,0 Z', fill: '#8B5E3C', stroke: '#5C3A1E', strokeWidth: 2 }));
        sc.push(e('path', { key: 'bt-n', d: 'M 20,0 L 20,6 L 30,6 L 30,0 M 42,0 L 42,6 L 52,6 L 52,0 M 64,0 L 64,6 L 74,6 L 74,0', fill: 'none', stroke: '#5C3A1E', strokeWidth: 1.5 }));
      }
      if (ce) {
        sc.push(e('path', { key: 'c-e', d: 'M 100,10 Q 68,20 68,50 Q 68,80 100,90 Z', fill: '#8B5E3C', stroke: '#5C3A1E', strokeWidth: 2 }));
      }
      if (cs) {
        sc.push(e('path', { key: 'c-s', d: 'M 10,100 Q 20,68 50,68 Q 80,68 90,100 Z', fill: '#8B5E3C', stroke: '#5C3A1E', strokeWidth: 2 }));
      }
      if (cw) {
        sc.push(e('path', { key: 'c-w', d: 'M 0,10 Q 32,20 32,50 Q 32,80 0,90 Z', fill: '#8B5E3C', stroke: '#5C3A1E', strokeWidth: 2 }));
      }
    }

    // Roads
    const rn = edges[Direction.NORTH] === 'ROAD', re = edges[Direction.EAST] === 'ROAD';
    const rs = edges[Direction.SOUTH] === 'ROAD', rw = edges[Direction.WEST] === 'ROAD';
    const roadColor = '#d4b896';
    const roadEdge = '#a0845a';
    const roadW = 14;

    if (re && rw && !rn && !rs) {
      sc.push(e('rect', { key: 'r-ew-bg', x: 0, y: 43, width: 100, height: roadW, fill: roadColor, stroke: roadEdge, strokeWidth: 1 }));
    } else if (rn && rs && !re && !rw) {
      sc.push(e('rect', { key: 'r-ns-bg', x: 43, y: 0, width: roadW, height: 100, fill: roadColor, stroke: roadEdge, strokeWidth: 1 }));
    } else if (re && rs && !rn && !rw) {
      sc.push(e('path', { key: 'r-es', d: 'M 100,43 L 57,43 Q 43,43 43,57 L 43,100', fill: 'none', stroke: roadColor, strokeWidth: roadW }));
      sc.push(e('path', { key: 'r-es-e', d: 'M 100,43 L 57,43 Q 43,43 43,57 L 43,100', fill: 'none', stroke: roadEdge, strokeWidth: roadW + 2, opacity: 0.3 }));
    } else if (rs && rw && !rn && !re) {
      sc.push(e('path', { key: 'r-sw', d: 'M 43,100 L 43,57 Q 43,43 30,43 L 0,43', fill: 'none', stroke: roadColor, strokeWidth: roadW }));
    } else if (rw && rn && !re && !rs) {
      sc.push(e('path', { key: 'r-wn', d: 'M 0,43 L 30,43 Q 43,43 43,30 L 43,0', fill: 'none', stroke: roadColor, strokeWidth: roadW }));
    } else if (rn && re && !rs && !rw) {
      sc.push(e('path', { key: 'r-ne', d: 'M 43,0 L 43,30 Q 43,43 57,43 L 100,43', fill: 'none', stroke: roadColor, strokeWidth: roadW }));
    } else {
      // Junction / individual segments to center
      if (rn) sc.push(e('rect', { key: 'rn', x: 43, y: 0, width: roadW, height: 50, fill: roadColor }));
      if (re) sc.push(e('rect', { key: 're', x: 50, y: 43, width: 50, height: roadW, fill: roadColor }));
      if (rs) sc.push(e('rect', { key: 'rs', x: 43, y: 50, width: roadW, height: 50, fill: roadColor }));
      if (rw) sc.push(e('rect', { key: 'rw', x: 0, y: 43, width: 50, height: roadW, fill: roadColor }));
      if (rn || re || rs || rw) {
        sc.push(e('circle', { key: 'rc', cx: 50, cy: 50, r: 8, fill: roadColor, stroke: roadEdge, strokeWidth: 1 }));
      }
    }

    // Monastery
    if (tile.hasMonastery) {
      sc.push(e('rect', { key: 'm-base', x: 32, y: 35, width: 36, height: 30, fill: '#f5e6d0', stroke: '#8B5E3C', strokeWidth: 2, rx: 2 }));
      sc.push(e('polygon', { key: 'm-roof', points: '50,20 26,38 74,38', fill: '#b91c1c', stroke: '#7f1d1d', strokeWidth: 2 }));
      sc.push(e('line', { key: 'm-cv', x1: 50, y1: 13, x2: 50, y2: 22, stroke: '#eab308', strokeWidth: 3 }));
      sc.push(e('line', { key: 'm-ch', x1: 46, y1: 16, x2: 54, y2: 16, stroke: '#eab308', strokeWidth: 3 }));
      sc.push(e('rect', { key: 'm-door', x: 45, y: 52, width: 10, height: 13, fill: '#5C3A1E', rx: 2 }));
      sc.push(e('rect', { key: 'm-win1', x: 36, y: 42, width: 6, height: 6, fill: '#7dd3fc', stroke: '#8B5E3C', strokeWidth: 1 }));
      sc.push(e('rect', { key: 'm-win2', x: 58, y: 42, width: 6, height: 6, fill: '#7dd3fc', stroke: '#8B5E3C', strokeWidth: 1 }));
    }

    // Border
    sc.push(e('rect', { key: 'brd', x: 0, y: 0, width: 100, height: 100, fill: 'none', stroke: 'rgba(0,0,0,0.2)', strokeWidth: 1 }));

    // Meeples on tile
    const mCoords = { NORTH: {cx:50,cy:16}, EAST: {cx:84,cy:50}, SOUTH: {cx:50,cy:84}, WEST: {cx:16,cy:50}, CENTER: {cx:50,cy:50} };
    for (let i = 0; i < (tile.meeples || []).length; i++) {
      const m = tile.meeples[i];
      const pos = mCoords[m.segment] || mCoords.CENTER;
      const pColor = players[m.playerIndex]?.color?.hex || '#dc2626';
      sc.push(e('g', { key: `m-${i}`, transform: `translate(${pos.cx - 12}, ${pos.cy - 12})` }, [
        e(MeepleIcon, { key: 'ic', colorHex: pColor, size: 24 })
      ]));
    }

    return e('div', {
      className: 'carcassonne-tile',
      style: { width: size, height: size, cursor: onClick ? 'pointer' : 'default' },
      onClick
    }, [
      e('svg', { key: 'svg', viewBox: '0 0 100 100', width: '100%', height: '100%', style: { display: 'block' } }, sc)
    ]);
  }

  // ==========================================
  // 6. App Principal
  // ==========================================
  function App() {
    const [players, setPlayers] = useState([
      { id: 'p0', name: 'Jogador 1', color: PLAYER_COLORS[0], score: 0, meeplesCount: 7 },
      { id: 'p1', name: 'Jogador 2', color: PLAYER_COLORS[1], score: 0, meeplesCount: 7 }
    ]);
    const [curPI, setCurPI] = useState(0);
    const [deck, setDeck] = useState(() => createDeck());
    const [board, setBoard] = useState(() => ({
      '0,0': { ...TILE_DEFINITIONS.STARTER, rotation: 0, x: 0, y: 0, meeples: [] }
    }));
    const [curTileRaw, setCurTileRaw] = useState(() => {
      const d = createDeck();
      setDeck(d.slice(0, -1));
      return d[d.length - 1];
    });
    const [curRot, setCurRot] = useState(0);
    const [phase, setPhase] = useState(TurnPhase.PLACE_TILE);
    const [lastCoords, setLastCoords] = useState(null);
    const [meepleOpts, setMeepleOpts] = useState([]);
    const [history, setHistory] = useState(['Partida iniciada!']);
    const [showRules, setShowRules] = useState(false);
    const [gameOver, setGameOver] = useState(false);

    const curTile = curTileRaw ? getRotatedTile(curTileRaw, curRot) : null;
    const validPlacements = (phase === TurnPhase.PLACE_TILE && curTile) ? getValidPlacements(board, curTile) : [];
    const curPlayer = players[curPI];

    const handleRotate = () => setCurRot(prev => (prev + 90) % 360);

    const handlePlaceTile = (x, y) => {
      if (!curTile || phase !== TurnPhase.PLACE_TILE) return;
      const placed = { ...curTile, x, y, meeples: [] };
      const newBoard = { ...board, [coordsToKey(x, y)]: placed };
      setBoard(newBoard);
      setLastCoords({ x, y });
      const opts = getAvailableMeepleOptions(newBoard, x, y, curPlayer.meeplesCount);
      if (opts.length > 0 && curPlayer.meeplesCount > 0) {
        setMeepleOpts(opts);
        setPhase(TurnPhase.PLACE_MEEPLE);
      } else {
        advanceTurn(newBoard, { x, y });
      }
    };

    const handlePlaceMeeple = (segment) => {
      if (!lastCoords || phase !== TurnPhase.PLACE_MEEPLE) return;
      const { x, y } = lastCoords;
      const key = coordsToKey(x, y);
      const tile = board[key];
      tile.meeples = [...(tile.meeples || []), { playerIndex: curPI, segment }];
      setPlayers(prev => prev.map((p, i) => i === curPI ? { ...p, meeplesCount: p.meeplesCount - 1 } : p));
      setHistory(prev => [`${curPlayer.name} colocou meeple (${segment})`, ...prev]);
      advanceTurn(board, lastCoords);
    };

    const handleSkipMeeple = () => {
      advanceTurn(board, lastCoords);
    };

    const advanceTurn = (curBoard, lc) => {
      const sc = evaluateScoring(curBoard, lc.x, lc.y);
      if (Object.keys(sc.pointsAwarded).length > 0) {
        setPlayers(prev => prev.map((p, i) => ({
          ...p,
          score: p.score + (sc.pointsAwarded[i] || 0),
          meeplesCount: p.meeplesCount + sc.meeplesReturned.filter(m => m.playerIndex === i).length
        })));
        sc.events.forEach(ev => setHistory(prev => [ev, ...prev]));
      }
      if (deck.length === 0) {
        setGameOver(true); setPhase(TurnPhase.GAME_OVER);
        setHistory(prev => ['🏁 Monte esgotado! Fim de jogo.', ...prev]);
        return;
      }
      const next = deck[deck.length - 1];
      setDeck(prev => prev.slice(0, -1));
      setCurTileRaw(next);
      setCurRot(0);
      setPhase(TurnPhase.PLACE_TILE);
      setLastCoords(null);
      setMeepleOpts([]);
      setCurPI(prev => (prev + 1) % players.length);
    };

    // Board grid
    const bounds = getBoardBounds(board, 1);
    const validMap = new Set(validPlacements.map(p => coordsToKey(p.x, p.y)));
    const tileSize = 90;
    const rows = [];
    for (let y = bounds.maxY; y >= bounds.minY; y--) {
      const cells = [];
      for (let x = bounds.minX; x <= bounds.maxX; x++) {
        const k = coordsToKey(x, y);
        const placed = board[k];
        const isValid = validMap.has(k);
        cells.push(e('div', { key: `c-${x}-${y}`, className: 'grid-cell', style: { width: tileSize, height: tileSize } }, [
          placed ? e(TileSvg, { tile: placed, size: tileSize, players }) :
          isValid ? e(TileSvg, { highlightValid: true, size: tileSize, onClick: () => handlePlaceTile(x, y) }) :
          e('div', { className: 'empty-grid-slot', style: { width: tileSize, height: tileSize } })
        ]));
      }
      rows.push(e('div', { key: `r-${y}`, className: 'grid-row' }, cells));
    }

    // Sorted players for game over
    const sortedPlayers = [...players].sort((a, b) => b.score - a.score);

    return e('div', { className: 'app-root' }, [
      // ===== TOP NAVBAR =====
      e('header', { key: 'nav', className: 'app-navbar' }, [
        e('div', { className: 'nav-brand' }, [
          e('span', { className: 'brand-icon' }, '🏰'),
          e('h1', { className: 'brand-title' }, 'Carcassonne'),
          e('span', { className: 'brand-subtitle' }, 'Hot-Seat')
        ]),
        e('div', { className: 'nav-actions' }, [
          e('button', { className: 'btn btn-sm btn-outline', onClick: () => setShowRules(true) }, '📖 Regras'),
          e('button', { className: 'btn btn-sm btn-primary', onClick: () => window.location.reload() }, '🔄 Nova Partida')
        ])
      ]),

      // ===== PLAYERS HUD (Top Bar) =====
      e('div', { key: 'hud', className: 'players-hud' },
        players.map((p, idx) => e('div', {
          key: p.id,
          className: `player-hud-card ${idx === curPI ? 'active' : ''}`
        }, [
          e('div', { key: 'av', className: 'player-hud-avatar' }, [
            e(MeepleIcon, { colorHex: p.color.hex, size: 32 })
          ]),
          e('div', { key: 'info', className: 'player-hud-info' }, [
            e('div', { className: 'player-hud-name' }, [
              p.name,
              idx === curPI ? e('span', { key: 'badge', className: 'player-hud-badge', style: { marginLeft: 6 } }, 'VEZ') : null
            ]),
            e('div', { className: 'player-hud-stats' }, [
              e('span', { key: 'm' }, `👤 ${p.meeplesCount}`),
              e('span', { key: 'p' }, `⭐ ${p.score} pts`)
            ])
          ]),
          e('div', { key: 'sc', className: 'player-hud-score' }, p.score)
        ]))
      ),

      // ===== GAME AREA =====
      e('main', { key: 'main', className: 'game-layout' }, [
        e('section', { key: 'board', className: 'board-area' }, [
          e('div', { className: 'board-wrapper' }, [
            // Tile Pile (top-right floating)
            e('div', { className: 'tile-pile' }, [
              e('div', { key: 'stack', className: 'tile-pile-stack' }, [
                e('div', { key: 'l1', className: 'tile-pile-layer', style: { width: 32, height: 32, bottom: 0, left: 0, background: '#8B5E3C' } }),
                e('div', { key: 'l2', className: 'tile-pile-layer', style: { width: 32, height: 32, bottom: 3, left: 3, background: '#a0845a' } }),
                e('div', { key: 'l3', className: 'tile-pile-layer', style: { width: 32, height: 32, bottom: 6, left: 6, background: '#c4a67a' } })
              ]),
              e('div', { key: 'num', className: 'tile-pile-number' }, deck.length),
              e('div', { key: 'lbl', className: 'tile-pile-label' }, 'restantes')
            ]),

            // Board scroll area
            e('div', { className: 'board-scroll-container' }, [
              e('div', { className: 'board-canvas' }, rows)
            ])
          ])
        ])
      ]),

      // ===== BOTTOM ACTION BAR =====
      e('div', { key: 'action-bar', className: 'action-bar' }, [
        // Left: Meeple stock
        e('div', { key: 'stock', className: 'action-meeple-stock' },
          Array.from({ length: 7 }).map((_, i) =>
            e('div', {
              key: `ms-${i}`,
              className: `meeple-stock-item ${i >= curPlayer.meeplesCount ? 'used' : ''}`
            }, [e(MeepleIcon, { colorHex: curPlayer.color.hex, size: 18 })])
          )
        ),

        // Center: Current Tile with rotation arrows
        phase === TurnPhase.PLACE_TILE && curTile ? e('div', { key: 'tile-area', className: 'action-tile-area' }, [
          e('button', { key: 'rotL', className: 'btn-rotate-arrow', onClick: handleRotate, title: 'Girar' }, '⟲'),
          e('div', { key: 'frame', className: 'action-tile-frame' }, [
            e('div', { className: 'action-tile-inner' }, [
              e(TileSvg, { tile: curTile, size: 80 })
            ]),
            e('div', { key: 'rbadge', className: 'action-rotation-badge' }, `${curRot}°`)
          ]),
          e('button', { key: 'rotR', className: 'btn-rotate-arrow', onClick: handleRotate, title: 'Girar' }, '⟳')
        ]) : null,

        // Right: Action buttons
        phase === TurnPhase.PLACE_TILE ? e('div', { key: 'btns', className: 'action-buttons' }, [
          e('div', { className: 'action-help-text' },
            validPlacements.length > 0
              ? `Clique em uma das ${validPlacements.length} posições douradas no tabuleiro`
              : 'Gire a peça para encontrar posições válidas'
          )
        ]) : null,

        phase === TurnPhase.PLACE_MEEPLE ? e('div', { key: 'meeple-act', className: 'meeple-action-inline' }, [
          e('div', { key: 'label', style: { color: '#c4a67a', fontSize: 13, fontWeight: 600, marginRight: 8 } }, 'Colocar Meeple:'),
          e('div', { key: 'opts', className: 'meeple-options-inline' },
            meepleOpts.map(opt =>
              e('button', {
                key: opt.segment,
                className: 'btn-meeple-option',
                onClick: () => handlePlaceMeeple(opt.segment)
              }, opt.label)
            )
          ),
          e('button', { key: 'skip', className: 'btn btn-skip', onClick: handleSkipMeeple }, '⏭️ Pular')
        ]) : null,

        phase === TurnPhase.GAME_OVER ? e('div', { key: 'go', style: { color: '#eab308', fontSize: 16, fontWeight: 700, fontFamily: 'Cinzel, serif' } }, '🏆 Partida Finalizada!') : null
      ]),

      // ===== RULES MODAL =====
      showRules ? e('div', { key: 'rules', className: 'modal-backdrop' }, [
        e('div', { className: 'modal-card' }, [
          e('div', { className: 'modal-header' }, [
            e('h2', { className: 'modal-title' }, '📜 Regras do Carcassonne'),
            e('button', { className: 'btn-close', onClick: () => setShowRules(false) }, '×')
          ]),
          e('div', { className: 'rules-content' }, [
            e('h4', null, '1. Turno'),
            e('p', null, 'Sorteie 1 peça, gire conforme desejar e posicione em uma casa válida (dourada) no tabuleiro.'),
            e('h4', null, '2. Encaixe'),
            e('p', null, 'A peça deve encostar em ao menos uma peça existente. Todas as bordas em contato devem combinar: estrada↔estrada, cidade↔cidade, campo↔campo.'),
            e('h4', null, '3. Meeples'),
            e('p', null, 'Após posicionar, você pode colocar 1 meeple em um segmento da peça. Bloqueado se a estrutura já tiver meeple conectado.'),
            e('h4', null, '4. Pontuação'),
            e('ul', null, [
              e('li', { key: '1' }, '🛤️ Estrada fechada: 1 pt/peça'),
              e('li', { key: '2' }, '🏰 Cidade fechada: 2 pts/peça'),
              e('li', { key: '3' }, '⛪ Mosteiro cercado (8 vizinhos): 9 pts'),
              e('li', { key: '4' }, 'Meeples retornam ao estoque ao pontuar')
            ]),
            e('h4', null, '5. Fim de Jogo'),
            e('p', null, 'Quando o monte zerar, estruturas incompletas são pontuadas e vence quem tiver mais pontos!')
          ]),
          e('div', { className: 'modal-actions' }, [
            e('button', { className: 'btn btn-primary', onClick: () => setShowRules(false) }, 'Entendido!')
          ])
        ])
      ]) : null,

      // ===== GAME OVER MODAL =====
      gameOver ? e('div', { key: 'gameover', className: 'modal-backdrop' }, [
        e('div', { className: 'modal-card gameover-modal' }, [
          e('div', { key: 'trophy', className: 'trophy-icon' }, '🏆'),
          e('h2', { className: 'modal-title', style: { textAlign: 'center' } }, 'Partida Concluída!'),
          e('p', { className: 'winner-announcement' }, [
            'Parabéns ao campeão ',
            e('strong', { key: 'n', style: { color: sortedPlayers[0]?.color?.hex } }, sortedPlayers[0]?.name),
            ` com ${sortedPlayers[0]?.score} pontos!`
          ]),
          e('div', { className: 'final-podium' },
            sortedPlayers.map((p, idx) => e('div', {
              key: p.id,
              className: `podium-row ${idx === 0 ? 'winner-row' : ''}`
            }, [
              e('div', { key: 'r', className: 'podium-rank' }, idx === 0 ? '🥇 1º' : idx === 1 ? '🥈 2º' : `${idx + 1}º`),
              e('div', { key: 'p', className: 'podium-player' }, [
                e(MeepleIcon, { key: 'm', colorHex: p.color.hex, size: 24 }),
                e('span', { key: 'n', className: 'podium-name' }, p.name)
              ]),
              e('div', { key: 'pts', className: 'podium-points' }, [
                e('strong', null, p.score), ' pts'
              ])
            ]))
          ),
          e('div', { className: 'modal-actions' }, [
            e('button', { className: 'btn btn-primary btn-lg', onClick: () => window.location.reload() }, '🔄 Nova Partida')
          ])
        ])
      ]) : null
    ]);
  }

  const root = window.ReactDOM.createRoot(document.getElementById('root'));
  root.render(e(App));
}

window.loadStandaloneApp = loadStandaloneApp;
