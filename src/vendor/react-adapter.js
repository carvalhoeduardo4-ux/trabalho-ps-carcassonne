const React = window.React || {
  createElement: (type, props, ...children) => ({ type, props: { ...props, children } }),
  useState: (init) => [init, () => {}],
  useEffect: () => {},
  useRef: () => ({ current: null })
};
export default React;
export const { createElement, useState, useEffect, useRef, useMemo, useCallback } = React;
