import React from 'react';
import { MeepleIcon } from './TileSvg.js';
import { Modal } from './Modal.js';

const MEDALS = { 1: '🥇', 2: '🥈', 3: '🥉' };

export function GameOverModal({ players = [], onRestart, onClose }) {
  const ranked = players
    .map((p, idx) => ({ ...p, number: idx + 1 }))
    .sort((a, b) => b.score - a.score);
  const best = ranked.length > 0 ? ranked[0].score : 0;
  const winners = ranked.filter(p => p.score === best);
  const rankOf = (p) => 1 + ranked.filter(o => o.score > p.score).length; // empatados dividem a posição (RF34)
  const isTie = winners.length > 1;

  return React.createElement(Modal, { titleId: 'gameover-title', onClose, className: 'gameover-modal' },
    React.createElement('div', { className: 'gameover-header' },
      React.createElement('div', { className: 'trophy-icon', 'aria-hidden': 'true' }, '🏆'),
      React.createElement('h2', { id: 'gameover-title', className: 'winner-title' }, isTie ? 'Empate!' : 'Partida concluída!'),
      React.createElement('p', { className: 'winner-announcement' },
        isTie
          ? `${winners.map(w => w.name).join(' e ')} empataram com ${best} pontos.`
          : ['Vitória de ',
            React.createElement('strong', { key: 'w-name', style: { color: winners[0]?.color?.hex } }, winners[0]?.name),
            ` com ${best} pontos!`]
      )
    ),

    React.createElement('ol', { className: 'final-podium' },
      ranked.map((p) => {
        const rank = rankOf(p);
        const isWinner = p.score === best;
        return React.createElement('li', {
          key: `${p.name}-${p.number}`,
          className: `podium-row ${isWinner ? 'winner-row' : ''}`
        },
          React.createElement('div', { className: 'podium-rank' }, `${MEDALS[rank] || ''} ${rank}º`),
          React.createElement('div', { className: 'podium-player' },
            React.createElement(MeepleIcon, { colorHex: p.color.hex, size: 28, label: String(p.number) }),
            React.createElement('span', { className: 'podium-name' }, p.name)
          ),
          React.createElement('div', { className: 'podium-points' },
            React.createElement('strong', null, p.score),
            React.createElement('span', null, ' pts')
          )
        );
      })
    ),

    React.createElement('div', { className: 'modal-actions modal-actions-row' },
      React.createElement('button', { type: 'button', className: 'btn btn-outline', onClick: onClose }, 'Ver tabuleiro'),
      React.createElement('button', { type: 'button', className: 'btn btn-primary', onClick: onRestart },
        React.createElement('span', { 'aria-hidden': 'true' }, '🔄'),
        'Nova partida'
      )
    )
  );
}
