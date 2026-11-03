import { get, post } from './client.js';

/** Cada chamada devolve o `GameStateDto` completo da partida. */
export const startGame = (players) => post('/game', { players });
export const getState = () => get('/game');
export const drawTile = () => post('/game/draw');
export const getPlacements = () => get('/game/placements');
export const placeTile = ({ x, y, rotation }) => post('/game/tile', { x, y, rotation });
export const getMeepleOptions = () => get('/game/meeples');
export const placeMeeple = ({ x, y, featureId }) => post('/game/meeple', { x, y, featureId });
export const skipMeeple = () => post('/game/skip');
/** Jogada sorteada pelo backend para o jogador atual (IA). Não altera o estado. */
export const getAiDecision = () => get('/game/ai/decision');
