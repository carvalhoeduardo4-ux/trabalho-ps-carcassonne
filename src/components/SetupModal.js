import React, { useState } from 'react';
import { PLAYER_COLORS } from '../core/types.js';
import { MeepleIcon } from './TileSvg.js';

export function SetupModal({ onStartGame }) {
  const [playerCount, setPlayerCount] = useState(2);
  const [playersConfig, setPlayersConfig] = useState([
    { name: 'Jogador 1', colorId: 'red' },
    { name: 'Jogador 2', colorId: 'blue' },
    { name: 'Jogador 3', colorId: 'green' },
    { name: 'Jogador 4', colorId: 'yellow' },
    { name: 'Jogador 5', colorId: 'purple' }
  ]);

  const handleNameChange = (index, newName) => {
    const updated = [...playersConfig];
    updated[index].name = newName;
    setPlayersConfig(updated);
  };

  const handleStart = () => {
    const selected = playersConfig.slice(0, playerCount);
    onStartGame(selected);
  };

  return React.createElement('div', { className: 'modal-backdrop' }, [
    React.createElement('div', { className: 'modal-card setup-modal' }, [
      React.createElement('div', { className: 'modal-header' }, [
        React.createElement('h2', { className: 'modal-title' }, '🏰 Carcassonne — Nova Partida'),
        React.createElement('p', { className: 'modal-subtitle' }, 
          'Configure os jogadores locais (2 a 5 participantes no mesmo dispositivo).'
        )
      ]),

      React.createElement('div', { className: 'form-group' }, [
        React.createElement('label', { className: 'form-label' }, 'Número de Jogadores:'),
        React.createElement('div', { className: 'player-count-buttons' }, [2, 3, 4, 5].map(n => 
          React.createElement('button', {
            key: `count-${n}`,
            className: `btn btn-count ${playerCount === n ? 'active' : ''}`,
            onClick: () => setPlayerCount(n)
          }, `${n} Jogadores`)
        ))
      ]),

      React.createElement('div', { className: 'players-setup-list' }, 
        playersConfig.slice(0, playerCount).map((cfg, idx) => {
          const color = PLAYER_COLORS.find(c => c.id === cfg.colorId);
          return React.createElement('div', { key: `p-setup-${idx}`, className: 'player-setup-row' }, [
            React.createElement('div', { className: 'setup-avatar-box' }, [
              React.createElement(MeepleIcon, { colorHex: color.hex, size: 30 })
            ]),
            React.createElement('input', {
              type: 'text',
              className: 'input-text',
              value: cfg.name,
              onChange: (e) => handleNameChange(idx, e.target.value),
              placeholder: `Nome do Jogador ${idx + 1}`
            }),
            React.createElement('span', { 
              className: 'color-tag',
              style: { backgroundColor: color.bgHex, color: color.textHex }
            }, color.name)
          ]);
        })
      ),

      React.createElement('div', { className: 'modal-actions' }, [
        React.createElement('button', {
          className: 'btn btn-primary btn-lg btn-start',
          onClick: handleStart
        }, '⚔️ Começar o Jogo')
      ])
    ])
  ]);
}
