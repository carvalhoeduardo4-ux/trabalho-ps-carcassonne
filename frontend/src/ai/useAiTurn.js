import { useState, useEffect, useRef } from 'react';
import * as api from '../api/gameApi.js';
import { isAiPlayer, AI_THINK_MS, AI_PREVIEW_MS, AI_MAX_RETRIES } from '../ui/constants.js';

const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

/** Erro usado para abandonar um passo da IA sem exibir aviso ao usuário. */
function cancelledError() {
  const e = new Error('Jogada da IA cancelada.');
  e.cancelled = true;
  return e;
}

/** A vez é de um jogador controlado pelo computador numa partida em andamento. */
export function isAiTurn(game) {
  return !!game && game.status === 'ACTIVE' && isAiPlayer(game.players[game.currentPlayerIndex]);
}

/**
 * Joga automaticamente enquanto o jogador atual for IA.
 *
 * Cada passo pede a decisão ao backend (GET /ai/decision), mostra a jogada
 * por um instante e a aplica pelos mesmos endpoints do humano, sempre dentro
 * de `run` (que serializa as ações, liga `busy` e chama `settle`). O novo
 * estado devolvido por `settle` dispara o passo seguinte.
 *
 * - `run(action)` deve devolver true (sucesso), false (erro) ou null (ignorada
 *   porque outra ação estava em andamento).
 * - `resync()` recarrega o estado do backend; devolve true/false como `run`.
 * - `enabled = false` pausa a IA (ex.: modal aberto).
 */
export function useAiTurn({ game, enabled, run, resync, setRotation, setMeeplePreview }) {
  const [aiTarget, setAiTarget] = useState(null); // casa onde a IA vai colocar a peça
  const [failed, setFailed] = useState(false);
  const [retryTick, setRetryTick] = useState(0);
  const failures = useRef(0);
  const token = useRef(0);

  const aiTurn = isAiTurn(game);

  useEffect(() => {
    if (!enabled || !aiTurn || failed) return undefined;

    const myToken = ++token.current;
    const alive = () => token.current === myToken;

    const step = async () => {
      const result = await run(async () => {
        const decision = await api.getAiDecision();
        if (!alive()) throw cancelledError();

        switch (decision.action) {
          case 'PLACE_TILE': {
            const { x, y, rotation } = decision.placement;
            setRotation(rotation); // a pré-visualização aparece na rotação escolhida
            setAiTarget({ x, y });
            await sleep(AI_PREVIEW_MS);
            if (!alive()) throw cancelledError();
            return api.placeTile({ x, y, rotation });
          }
          case 'PLACE_MEEPLE': {
            const option = decision.feature; // { x, y, featureId }, mesmo formato de meepleOptions
            setMeeplePreview(option);
            await sleep(AI_PREVIEW_MS);
            if (!alive()) throw cancelledError();
            return api.placeMeeple(option);
          }
          case 'SKIP_MEEPLE':
            return api.skipMeeple();
          case 'DRAW_TILE':
            return api.drawTile();
          default:
            throw new Error(`Decisão desconhecida da IA: ${decision.action}`);
        }
      });

      setAiTarget(null);

      if (result === true) {
        failures.current = 0;
        return;
      }
      if (!alive()) {
        // Pausada ou substituída no meio do passo: limpa o destaque e sai
        setMeeplePreview(null);
        return;
      }
      if (result === null) {
        // Outra ação estava em andamento: tenta de novo em seguida
        setRetryTick(t => t + 1);
        return;
      }

      // Falhou (ex.: estado desatualizado ou servidor fora do ar)
      failures.current += 1;
      setMeeplePreview(null);
      if (failures.current >= AI_MAX_RETRIES || !(await resync())) {
        setFailed(true);
      }
      // Se o resync deu certo, o novo `game` dispara outra tentativa
    };

    const timer = setTimeout(step, AI_THINK_MS);

    return () => {
      token.current++;
      clearTimeout(timer);
    };
  }, [game, enabled, aiTurn, failed, retryTick]);

  /** Retoma a IA depois de ela ter parado por falhas. */
  const retry = () => {
    failures.current = 0;
    setFailed(false);
  };

  /** Esquece falhas anteriores (ex.: ao começar outra partida). */
  const reset = () => {
    token.current++;
    failures.current = 0;
    setFailed(false);
    setAiTarget(null);
  };

  return { aiTurn, aiTarget, failed, retry, reset };
}
