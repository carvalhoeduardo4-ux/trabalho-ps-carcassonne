import { TurnPhase, PLAYER_COLORS } from './types.js';
import { createDeck, createStarterTile, getRotatedTile } from './tiles.js';
import { canPlaceTile, getValidPlacements, coordsToKey } from './board.js';
import { 
  getAvailableMeepleOptions, 
  evaluateImmediateScoring, 
  evaluateFinalScoring 
} from './scoring.js';

/**
 * Cria o estado inicial de uma nova partida de Carcassonne
 */
export function createInitialGameState(playerConfigs = [
  { name: 'Jogador 1', colorId: 'red' },
  { name: 'Jogador 2', colorId: 'blue' }
]) {
  const players = playerConfigs.map((cfg, idx) => {
    const colorInfo = PLAYER_COLORS.find(c => c.id === cfg.colorId) || PLAYER_COLORS[idx % PLAYER_COLORS.length];
    return {
      id: `p_${idx}`,
      name: cfg.name || `Jogador ${idx + 1}`,
      color: colorInfo,
      score: 0,
      meeplesCount: 7 // Cada jogador começa com 7 meeples no estoque
    };
  });

  const starterTile = createStarterTile();
  const board = {
    [coordsToKey(0, 0)]: starterTile
  };

  const deck = createDeck();
  const firstTileRaw = deck.pop();
  const currentTile = firstTileRaw ? getRotatedTile(firstTileRaw, 0) : null;

  return {
    players,
    currentPlayerIndex: 0,
    deck,
    board,
    currentTileRaw: firstTileRaw,
    currentTileRotation: 0,
    currentTile,
    phase: currentTile ? TurnPhase.PLACE_TILE : TurnPhase.GAME_OVER,
    lastPlacedCoords: null,
    validPlacements: currentTile ? getValidPlacements(board, currentTile) : [],
    history: [
      { id: 'h_start', text: 'Partida iniciada! Peça inicial colocada no centro (0, 0).' }
    ],
    gameOver: false,
    winner: null
  };
}

/**
 * Rotaciona a peça atual em 90° no sentido horário
 */
export function rotateCurrentTile(state) {
  if (state.phase !== TurnPhase.PLACE_TILE || !state.currentTileRaw) {
    return state;
  }

  const newRotation = (state.currentTileRotation + 90) % 360;
  const rotatedTile = getRotatedTile(state.currentTileRaw, newRotation);
  const validPlacements = getValidPlacements(state.board, rotatedTile);

  return {
    ...state,
    currentTileRotation: newRotation,
    currentTile: rotatedTile,
    validPlacements
  };
}

/**
 * Posiciona a peça atual nas coordenadas (x, y)
 */
export function placeCurrentTile(state, x, y) {
  if (state.phase !== TurnPhase.PLACE_TILE || !state.currentTile) {
    return state;
  }

  if (!canPlaceTile(state.board, state.currentTile, x, y)) {
    return state;
  }

  const key = coordsToKey(x, y);
  const placedTile = {
    ...state.currentTile,
    x,
    y,
    meeples: []
  };

  const newBoard = {
    ...state.board,
    [key]: placedTile
  };

  const currentPlayer = state.players[state.currentPlayerIndex];
  const availableMeepleOptions = getAvailableMeepleOptions(
    newBoard, 
    x, 
    y, 
    currentPlayer.meeplesCount
  );

  // Se o jogador não tem meeples ou não há segmentos elegíveis, avança direto
  if (availableMeepleOptions.length === 0 || currentPlayer.meeplesCount <= 0) {
    return resolveTurnAndAdvance({
      ...state,
      board: newBoard,
      lastPlacedCoords: { x, y },
      currentTile: null
    });
  }

  return {
    ...state,
    board: newBoard,
    lastPlacedCoords: { x, y },
    phase: TurnPhase.PLACE_MEEPLE,
    availableMeepleOptions,
    validPlacements: []
  };
}

/**
 * Coloca um meeple no segmento escolhido da peça recém-colocada
 */
export function placeMeeple(state, segment) {
  if (state.phase !== TurnPhase.PLACE_MEEPLE || !state.lastPlacedCoords) {
    return state;
  }

  const { x, y } = state.lastPlacedCoords;
  const key = coordsToKey(x, y);
  const tile = state.board[key];
  if (!tile) return state;

  const currentPlayer = state.players[state.currentPlayerIndex];
  if (currentPlayer.meeplesCount <= 0) return state;

  // Atualizar contagem de meeples do jogador
  const newPlayers = state.players.map((p, idx) => {
    if (idx === state.currentPlayerIndex) {
      return { ...p, meeplesCount: p.meeplesCount - 1 };
    }
    return p;
  });

  // Colocar meeple no tile
  const newTile = {
    ...tile,
    meeples: [...(tile.meeples || []), { playerIndex: state.currentPlayerIndex, segment }]
  };

  const newBoard = {
    ...state.board,
    [key]: newTile
  };

  const newHistory = [
    {
      id: `h_${Date.now()}_meeple`,
      text: `${currentPlayer.name} colocou um meeple em ${segment} da peça em (${x}, ${y}).`
    },
    ...state.history
  ];

  return resolveTurnAndAdvance({
    ...state,
    players: newPlayers,
    board: newBoard,
    history: newHistory
  });
}

/**
 * Pula a colocação de Meeple na jogada atual
 */
export function skipMeeple(state) {
  if (state.phase !== TurnPhase.PLACE_MEEPLE) {
    return state;
  }

  const currentPlayer = state.players[state.currentPlayerIndex];
  const newHistory = [
    {
      id: `h_${Date.now()}_skip`,
      text: `${currentPlayer.name} optou por não colocar Meeple.`
    },
    ...state.history
  ];

  return resolveTurnAndAdvance({
    ...state,
    history: newHistory
  });
}

/**
 * Avalia pontuação imediata, devolve meeples e passa para o próximo jogador
 * (ou encerra o jogo caso o monte tenha acabado).
 */
function resolveTurnAndAdvance(state) {
  const { lastPlacedCoords } = state;
  let newBoard = { ...state.board };
  let newPlayers = state.players.map(p => ({ ...p }));
  let newHistory = [...state.history];

  if (lastPlacedCoords) {
    const scoreResult = evaluateImmediateScoring(newBoard, lastPlacedCoords.x, lastPlacedCoords.y);

    // Atualizar pontos
    for (const pIdx in scoreResult.pointsAwarded) {
      const idx = Number(pIdx);
      newPlayers[idx].score += scoreResult.pointsAwarded[pIdx];
    }

    // Devolver meeples
    for (const m of scoreResult.meeplesReturned) {
      newPlayers[m.playerIndex].meeplesCount += 1;
    }

    // Registrar mensagens
    for (const ev of scoreResult.events) {
      newHistory.unshift({
        id: `h_${Date.now()}_${Math.random()}`,
        text: `🎯 ${ev.message}`
      });
    }
  }

  // Verificar se ainda há peças no monte
  const newDeck = [...state.deck];
  const nextTileRaw = newDeck.pop();

  if (!nextTileRaw) {
    // FIM DE JOGO!
    return finalizeGame({
      ...state,
      players: newPlayers,
      board: newBoard,
      deck: newDeck,
      history: newHistory
    });
  }

  const nextTile = getRotatedTile(nextTileRaw, 0);
  const nextPlayerIndex = (state.currentPlayerIndex + 1) % newPlayers.length;
  const validPlacements = getValidPlacements(newBoard, nextTile);

  return {
    ...state,
    players: newPlayers,
    board: newBoard,
    deck: newDeck,
    currentPlayerIndex: nextPlayerIndex,
    currentTileRaw: nextTileRaw,
    currentTileRotation: 0,
    currentTile: nextTile,
    phase: TurnPhase.PLACE_TILE,
    lastPlacedCoords: null,
    availableMeepleOptions: [],
    validPlacements,
    history: newHistory,
    gameOver: false
  };
}

/**
 * Conclui a partida e realiza a pontuação final
 */
function finalizeGame(state) {
  const finalResult = evaluateFinalScoring(state.board);
  const finalPlayers = state.players.map(p => ({ ...p }));
  const finalHistory = [...state.history];

  for (const pIdx in finalResult.pointsAwarded) {
    const idx = Number(pIdx);
    finalPlayers[idx].score += finalResult.pointsAwarded[pIdx];
  }

  for (const ev of finalResult.events) {
    finalHistory.unshift({
      id: `h_final_${Date.now()}_${Math.random()}`,
      text: `🏆 Final: ${ev.message}`
    });
  }

  // Ordenar jogadores por pontuação
  const sortedPlayers = [...finalPlayers].sort((a, b) => b.score - a.score);
  const winner = sortedPlayers[0];

  finalHistory.unshift({
    id: `h_winner_${Date.now()}`,
    text: `🎉 Partida finalizada! Vencedor: ${winner.name} com ${winner.score} pontos!`
  });

  return {
    ...state,
    players: finalPlayers,
    phase: TurnPhase.GAME_OVER,
    currentTile: null,
    currentTileRaw: null,
    validPlacements: [],
    history: finalHistory,
    gameOver: true,
    winner
  };
}
