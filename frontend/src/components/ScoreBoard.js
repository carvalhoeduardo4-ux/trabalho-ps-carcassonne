import React from 'react';
import { MeepleIcon } from './TileSvg.js';

export function ScoreBoard({ players = [], currentPlayerIndex = 0, gains = {}, gainId = null }) {
  return React.createElement('div', { className: 'scoreboard-panel card' }, [
    React.createElement('div', { className: 'scoreboard-header' }, [
      React.createElement('h3', { className: 'panel-title' }, '🏆 Placar de Jogadores')
    ]),

    React.createElement('div', { className: 'players-list' }, 
      players.map((p, idx) => {
        const isCurrent = idx === currentPlayerIndex;
        const gain = gains[idx];
        return React.createElement('div', {
          key: `${p.name}-${idx}`,
          className: `player-card ${isCurrent ? 'player-active' : ''} ${gain ? 'score-pulse' : ''}`,
          style: { borderLeftColor: p.color.hex }
        }, [
          React.createElement('div', { className: 'player-card-info' }, [
            React.createElement('div', { className: 'player-avatar-box' }, [
              React.createElement(MeepleIcon, { colorHex: p.color.hex, size: 28 }),
              isCurrent ? React.createElement('span', { className: 'turn-dot' }) : null
            ]),
            React.createElement('div', null, [
              React.createElement('div', { className: 'player-name-row' }, [
                React.createElement('strong', { className: 'player-name' }, p.name),
                isCurrent ? React.createElement('span', { className: 'current-badge' }, 'Vez') : null
              ]),
              React.createElement('div', { className: 'player-subtext' }, [
                React.createElement('span', { className: 'meeple-count' }, `👤 ${p.availableMeeples} meeples`)
              ])
            ])
          ]),
          React.createElement('div', { className: 'player-score' }, [
            React.createElement('span', { className: 'score-number' }, p.score),
            React.createElement('span', { className: 'score-unit' }, 'pts'),
            gain ? React.createElement('span', {
              key: `gain-${gainId}`,
              className: 'score-gain',
              'aria-label': `${p.name} ganhou ${gain} pontos`
            }, `+${gain}`) : null
          ])
        ]);
      })
    ),

    React.createElement('div', { className: 'history-section' }, [
      React.createElement('h4', { className: 'history-title' }, '📜 Histórico da Partida'),
      React.createElement('div', { className: 'history-list' }, [
        React.createElement('div', { key: 'empty', className: 'history-item' }, 'Histórico ainda não disponível.')
      ])
    ])
  ]);
}
