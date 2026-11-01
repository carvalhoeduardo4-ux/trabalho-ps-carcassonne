const BASE_URL = 'http://localhost:8080/api';

async function request(path, options = {}) {
  let response;
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      headers: { 'Content-Type': 'application/json' },
      ...options
    });
  } catch (e) {
    throw new Error('Não foi possível conectar ao servidor (http://localhost:8080).');
  }
  const body = await response.json().catch(() => null);
  if (!response.ok) {
    throw new Error(body?.message || `Erro ${response.status}`);
  }
  return body;
}

export const post = (path, data) =>
  request(path, { method: 'POST', body: data === undefined ? undefined : JSON.stringify(data) });

export const get = (path) => request(path);
