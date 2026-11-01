const BASE_URL = 'http://localhost:8080/api';

async function request(path, options = {}) {
  let response;
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      headers: { 'Content-Type': 'application/json' },
      ...options
    });
  } catch (e) {
    // Erro de rede (servidor fora do ar): marcado para a interface diferenciar de erros da API
    const err = new Error('Não foi possível conectar ao servidor do jogo.');
    err.offline = true;
    throw err;
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
