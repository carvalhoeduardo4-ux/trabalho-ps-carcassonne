import React from 'react';
import { MeepleIcon } from './TileSvg.js';

export function GameOverModal({ players = [], onRestart }) {
  const sortedPlayers = [...players].sort((a, b) => b.score - a.score);
  const winner = sortedPlayers[0];

  return React.createElement('div', { className: 'modal-backdrop' }, [
    React.createElement('div', { className: 'modal-card gameover-modal' }, [
      React.createElement('div', { className: 'gameover-header' }, [
        React.createElement('div', { className: 'trophy-icon' }, '🏆'),
        React.createElement('h2', { className: 'winner-title' }, 'Partida Concluída!'),
        React.createElement('p', { className: 'winner-announcement' }, [
          'Parabéns ao campeão ',
          React.createElement('strong', { 
            key: 'w-name', 
            style: { color: winner?.color?.hex } 
          }, winner?.name),
          ` com ${winner?.score} pontos!`
        ])
      ]),

      React.createElement('div', { className: 'final-podium' }, 
        sortedPlayers.map((p, idx) => {
          const isWinner = idx === 0;
          return React.createElement('div', {
            key: `${p.name}-${idx}`,
            className: `podium-row ${isWinner ? 'winner-row' : ''}`
          }, [
            React.createElement('div', { className: 'podium-rank' }, 
              isWinner ? '🥇 1º' : idx === 1 ? '🥈 2º' : idx === 2 ? '🥉 3º' : `${idx + 1}º`
            ),
            React.createElement('div', { className: 'podium-player' }, [
              React.createElement(MeepleIcon, { key: 'm', colorHex: p.color.hex, size: 28 }),
              React.createElement('span', { key: 'n', className: 'podium-name' }, p.name)
            ]),
            React.createElement('div', { className: 'podium-points' }, [
              React.createElement('strong', { key: 'pts' }, p.score),
              React.createElement('span', { key: 'lbl' }, ' pts')
            ])
          ]);
        })
      ),

      React.createElement('div', { className: 'modal-actions' }, [
        React.createElement('button', {
          className: 'btn btn-primary btn-lg',
          onClick: onRestart
        }, '🔄 Iniciar Nova Partida')
      ])
    ])
  ]);
}
