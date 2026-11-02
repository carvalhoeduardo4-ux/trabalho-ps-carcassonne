import React from 'react';
import { Modal, CloseButton } from './Modal.js';

export function RulesModal({ onClose }) {
  return React.createElement(Modal, { titleId: 'rules-title', onClose, className: 'rules-modal' },
    React.createElement('div', { className: 'modal-header' },
      React.createElement('h2', { id: 'rules-title', className: 'modal-title' },
        React.createElement('span', { 'aria-hidden': 'true' }, '📜 '),
        'Como jogar'
      ),
      React.createElement(CloseButton, { onClick: onClose })
    ),
    React.createElement('div', { className: 'rules-content' },
      React.createElement('h3', null, '1. Ciclo do turno'),
      React.createElement('p', null, 'A cada turno, uma peça é sorteada do monte. Você pode girá-la em múltiplos de 90° e posicioná-la em qualquer posição válida (destacada no tabuleiro). Passe o mouse sobre uma posição para ver como a peça vai ficar.'),

      React.createElement('h3', null, '2. Encaixe válido'),
      React.createElement('p', null, 'A nova peça deve tocar ao menos uma peça já colocada. Todas as bordas em contato devem ser do mesmo tipo: cidade com cidade, estrada com estrada, campo com campo.'),

      React.createElement('h3', null, '3. Colocação de meeples'),
      React.createElement('p', null, 'Depois de colocar a peça, você pode (opcionalmente) colocar 1 meeple em uma estrada, cidade, mosteiro ou campo dela. Passe o mouse sobre cada opção para ver onde o meeple vai ficar. Não é permitido colocar meeple em uma estrutura que já tenha meeple de qualquer jogador.'),

      React.createElement('h3', null, '4. Pontuação'),
      React.createElement('ul', null,
        React.createElement('li', null, 'Estrada fechada: 1 ponto por peça.'),
        React.createElement('li', null, 'Cidade fechada: 2 pontos por peça e 2 pontos por brasão (escudo azul).'),
        React.createElement('li', null, 'Mosteiro concluído (8 peças em volta): 9 pontos.'),
        React.createElement('li', null, 'Ao fechar a estrutura, quem tiver mais meeples nela leva os pontos (empatados pontuam todos) e os meeples voltam ao estoque.')
      ),

      React.createElement('h3', null, '5. Fim de jogo'),
      React.createElement('p', null, 'A partida termina quando o monte acaba. Vence quem tiver mais pontos.')
    ),
    React.createElement('div', { className: 'modal-actions' },
      React.createElement('button', { type: 'button', className: 'btn btn-primary', onClick: onClose }, 'Entendido!')
    )
  );
}
