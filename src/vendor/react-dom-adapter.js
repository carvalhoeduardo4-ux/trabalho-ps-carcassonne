const ReactDOM = window.ReactDOM || {
  createRoot: (container) => ({
    render: (element) => {}
  })
};
export default ReactDOM;
export const { createRoot } = ReactDOM;
