import React, { useRef } from 'react';

/**
 * Janela modal: role="dialog" com título associado; clicar no fundo escuro
 * fecha a janela quando há onClose (sem onClose, o modal é obrigatório).
 */
export function Modal({ titleId, onClose, className = '', children }) {
  const onCloseRef = useRef(onClose);
  onCloseRef.current = onClose; // sempre a versão mais nova da função

  return React.createElement('div', {
    className: 'modal-backdrop',
    onMouseDown: (e) => {
      if (e.target === e.currentTarget && onCloseRef.current) onCloseRef.current();
    }
  },
    React.createElement('div', {
      className: `modal-card ${className}`,
      role: 'dialog',
      'aria-modal': 'true',
      'aria-labelledby': titleId
    }, children)
  );
}

/** Botão "×" padrão dos modais. */
export function CloseButton({ onClick }) {
  return React.createElement('button', {
    type: 'button',
    className: 'btn-close',
    onClick,
    'aria-label': 'Fechar',
    title: 'Fechar'
  }, React.createElement('span', { 'aria-hidden': 'true' }, '×'));
}
