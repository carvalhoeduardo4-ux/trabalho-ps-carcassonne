// Shim: reexporta o React global (UMD) para os módulos ES via import map.
const React = window.React;
export default React;
export const { useState, useEffect, useRef, useMemo, useCallback } = React;
