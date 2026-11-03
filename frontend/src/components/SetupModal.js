import React, { useState } from 'react';
import { PLAYER_COLORS, PlayerType } from '../ui/constants.js';
import { MeepleIcon } from './TileSvg.js';
import { Modal, CloseButton } from './Modal.js';

const MAX_NAME_LENGTH = 20;

const defaultName = (index, type = PlayerType.HUMAN) =>
  type === PlayerType.AI ? `Computador ${index + 1}` : `Jogador ${index + 1}`;

const TYPE_OPTIONS = [
  { type: PlayerType.HUMAN, icon: '👤', label: 'Humano' },
  { type: PlayerType.AI, icon: '🤖', label: 'IA' }
];

export function SetupModal({ onStartGame, onCancel, hasGameInProgress = false, starting = false }) {
  const [playerCount, setPlayerCount] = useState(2);
  const [playersConfig, setPlayersConfig] = useState([
    { name: 'Jogador 1', colorId: 'RED', type: PlayerType.HUMAN },
    { name: 'Jogador 2', colorId: 'BLUE', type: PlayerType.HUMAN },
    { name: 'Jogador 3', colorId: 'GREEN', type: PlayerType.HUMAN },
    { name: 'Jogador 4', colorId: 'YELLOW', type: PlayerType.HUMAN },
    { name: 'Jogador 5', colorId: 'BLACK', type: PlayerType.HUMAN }
  ]);

  const updatePlayer = (index, changes) => {
    setPlayersConfig(prev => prev.map((p, i) => (i === index ? { ...p, ...changes } : p)));
  };

  // Troca humano/IA; o nome só acompanha se ainda for o nome padrão
  const setType = (index, type) => {
    setPlayersConfig(prev => prev.map((p, i) => {
      if (i !== index || p.type === type) return p;
      const isDefaultName = p.name.trim() === '' || p.name === defaultName(i, p.type);
      return { ...p, type, name: isDefaultName ? defaultName(i, type) : p.name };
    }));
  };

  // Nomes e cores precisam ser diferentes entre os jogadores ativos (RF02)
  const active = playersConfig.slice(0, playerCount);
  const names = active.map((p, i) => (p.name.trim() || defaultName(i, p.type)).toLowerCase());
  const allAi = active.every(p => p.type === PlayerType.AI);
  const duplicateName = new Set(names).size !== names.length;
  const duplicateColor = new Set(active.map(p => p.colorId)).size !== active.length;
  const formError = duplicateName
    ? 'Cada jogador precisa de um nome diferente.'
    : duplicateColor ? 'Cada jogador precisa de uma cor diferente.' : null;

  const handleStart = () => {
    if (formError) return;
    const selected = active.map((p, i) => ({
      name: p.name.trim() || defaultName(i, p.type),
      color: p.colorId,
      type: p.type
    }));
    onStartGame(selected);
  };

  return React.createElement(Modal, { titleId: 'setup-title', onClose: onCancel, className: 'setup-modal' },
    React.createElement('div', { className: 'modal-header' },
      React.createElement('div', null,
        React.createElement('h2', { id: 'setup-title', className: 'modal-title' },
          React.createElement('span', { 'aria-hidden': 'true' }, '🏰 '),
          'Nova partida'
        ),
        React.createElement('p', { className: 'modal-subtitle' },
          'Configure de 2 a 5 jogadores no mesmo dispositivo. Cada um pode ser humano ou controlado pelo computador (IA).'
        ),
        hasGameInProgress ? React.createElement('p', { className: 'modal-warning' },
          'A partida atual será descartada.'
        ) : null
      ),
      onCancel ? React.createElement(CloseButton, { onClick: onCancel }) : null
    ),

    React.createElement('div', { className: 'form-group' },
      React.createElement('span', { id: 'count-label', className: 'form-label' }, 'Número de jogadores'),
      React.createElement('div', { className: 'player-count-buttons', role: 'group', 'aria-labelledby': 'count-label' },
        [2, 3, 4, 5].map(n =>
          React.createElement('button', {
            key: `count-${n}`,
            type: 'button',
            className: `btn btn-count ${playerCount === n ? 'active' : ''}`,
            'aria-pressed': playerCount === n,
            onClick: () => setPlayerCount(n)
          }, String(n))
        )
      )
    ),

    React.createElement('div', { className: 'players-setup-list' },
      active.map((cfg, idx) => {
        const color = PLAYER_COLORS.find(c => c.id === cfg.colorId);
        return React.createElement('div', { key: `p-setup-${idx}`, className: 'player-setup-row' },
          React.createElement('div', { className: 'setup-avatar-box' },
            React.createElement(MeepleIcon, { colorHex: color.hex, size: 30, label: String(idx + 1) }),
            cfg.type === PlayerType.AI
              ? React.createElement('span', { className: 'setup-ai-mark', 'aria-hidden': 'true' }, '🤖')
              : null
          ),
          React.createElement('input', {
            type: 'text',
            className: 'input-text',
            value: cfg.name,
            maxLength: MAX_NAME_LENGTH,
            'aria-label': `Nome do jogador ${idx + 1}`,
            onChange: (e) => updatePlayer(idx, { name: e.target.value }),
            placeholder: defaultName(idx, cfg.type)
          }),
          React.createElement('div', {
            className: 'player-type-toggle',
            role: 'group',
            'aria-label': `Tipo do jogador ${idx + 1}`
          },
            TYPE_OPTIONS.map(opt => React.createElement('button', {
              key: opt.type,
              type: 'button',
              className: `btn-type ${cfg.type === opt.type ? 'active' : ''}`,
              'aria-pressed': cfg.type === opt.type,
              'aria-label': opt.label,
              onClick: () => setType(idx, opt.type)
            },
              React.createElement('span', { 'aria-hidden': 'true' }, opt.icon),
              React.createElement('span', null, opt.label)
            ))
          ),
          React.createElement('select', {
            className: 'input-select',
            value: cfg.colorId,
            'aria-label': `Cor do jogador ${idx + 1}`,
            style: { borderLeftColor: color.hex },
            onChange: (e) => updatePlayer(idx, { colorId: e.target.value })
          }, PLAYER_COLORS.map(c => React.createElement('option', {
            key: c.id,
            value: c.id,
            // cores já escolhidas por outro jogador ficam indisponíveis
            disabled: active.some((p, j) => j !== idx && p.colorId === c.id)
          }, c.name)))
        );
      })
    ),

    formError ? React.createElement('p', { className: 'form-error', role: 'alert' }, formError) : null,

    !formError && allAi ? React.createElement('p', { className: 'form-note', role: 'status' },
      'Todos os jogadores são controlados pelo computador: a partida será jogada automaticamente.'
    ) : null,

    React.createElement('div', { className: 'modal-actions modal-actions-row' },
      onCancel ? React.createElement('button', {
        type: 'button',
        className: 'btn btn-outline',
        onClick: onCancel
      }, 'Cancelar') : null,
      React.createElement('button', {
        type: 'button',
        className: 'btn btn-primary btn-lg btn-start',
        disabled: !!formError || starting,
        onClick: handleStart
      },
        React.createElement('span', { 'aria-hidden': 'true' }, '⚔️'),
        starting ? 'Iniciando…' : 'Começar o jogo'
      )
    )
  );
}
