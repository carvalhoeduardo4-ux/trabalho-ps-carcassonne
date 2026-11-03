#!/usr/bin/env bash
set -euo pipefail

backend_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
exec python3 - "$backend_dir" "$@" <<'PY'
import json
import os
from pathlib import Path
import re
import signal
import subprocess
import sys
import tempfile
import time
import urllib.error
import urllib.request


def check(condition, message):
    if not condition:
        raise RuntimeError(message)


def interrupt(signum, frame):
    raise KeyboardInterrupt(f"Sinal {signum}")


def main():
    backend = Path(sys.argv[1])
    check(len(sys.argv) <= 3, "Uso: smoke-backend.sh [caminho-do-jar]")
    jars = ([Path(sys.argv[2]).resolve()] if len(sys.argv) == 3
            else list((backend / "target").glob("*.jar")))
    check(len(jars) == 1 and jars[0].is_file(),
          "Esperado um jar em backend/target; execute Maven verify.")
    java = str(Path(os.environ["JAVA_HOME"]) / "bin/java") if os.environ.get("JAVA_HOME") else "java"
    timeout = float(os.environ.get("SMOKE_STARTUP_TIMEOUT", "60"))
    check(timeout > 0, "SMOKE_STARTUP_TIMEOUT deve ser positivo.")
    signal.signal(signal.SIGINT, interrupt)
    signal.signal(signal.SIGTERM, interrupt)

    with tempfile.TemporaryDirectory(prefix="smoke-", dir=backend / "target") as temporary:
        log_path = Path(temporary) / "server.log"
        with log_path.open("w") as log:
            server = subprocess.Popen([
                java, f"-Djava.io.tmpdir={temporary}", "-jar", str(jars[0]),
                "--server.address=127.0.0.1", "--server.port=0",
                "--spring.output.ansi.enabled=never",
            ], cwd=backend, stdout=log, stderr=subprocess.STDOUT)

        try:
            deadline = time.monotonic() + timeout
            base = None
            client = urllib.request.build_opener(urllib.request.ProxyHandler({}))
            while time.monotonic() < deadline:
                check(server.poll() is None, "O servidor encerrou durante a inicialização.")
                match = re.search(r"Tomcat started on port (\d+) \(http\)", log_path.read_text())
                if match:
                    base = f"http://127.0.0.1:{match.group(1)}/api/game"
                    try:
                        client.open(base, timeout=1).close()
                    except urllib.error.HTTPError as error:
                        error.close()
                        break
                    except urllib.error.URLError:
                        pass
                    else:
                        break
                time.sleep(0.1)
            else:
                raise RuntimeError("Tempo limite aguardando readiness HTTP.")

            def request(method, path="", payload=None):
                data = json.dumps(payload).encode() if payload is not None else None
                req = urllib.request.Request(base + path, data=data, method=method,
                                             headers={"Content-Type": "application/json"})
                with client.open(req, timeout=5) as response:
                    check(response.status == 200,
                          f"{method} {path or '/'} retornou HTTP {response.status}.")
                    return json.loads(response.read().decode())

            players = [{"name": "Alice", "color": "RED"}, {"name": "Bob", "color": "BLUE"}]
            initial = request("POST", payload={"players": players})
            check(initial["phase"] == "DRAW_TILE" and initial["currentPlayerIndex"] == 0,
                  "Estado inicial ou primeiro jogador incorreto.")
            check(len(initial["board"]) == 1, "O tabuleiro inicial deve ter uma peça.")
            check(request("GET") == initial, "GET diverge da partida criada.")

            drawn = request("POST", "/draw")
            check(drawn["phase"] == "PLACE_TILE" and drawn["drawnTile"] is not None,
                  "A compra não avançou para a colocação.")
            placements = request("GET", "/placements")
            check(placements and placements == drawn["placements"],
                  "A compra deve oferecer colocações legais consistentes.")

            placement = placements[0]
            placed = request("POST", "/tile", placement)
            check(placed["phase"] == "PLACE_MEEPLE" and len(placed["board"]) == 2,
                  "A colocação legal não atualizou o tabuleiro.")
            check(placed["placedTile"]["x"] == placement["x"]
                  and placed["placedTile"]["y"] == placement["y"]
                  and placed["placedTile"]["rotation"] == placement["rotation"],
                  "A peça colocada diverge da opção legal escolhida.")

            skipped = request("POST", "/skip")
            check(skipped["phase"] == "DRAW_TILE" and skipped["currentPlayerIndex"] == 1,
                  "Pular o meeple não avançou o turno.")
            check(skipped["board"] == placed["board"] and len(skipped["board"]) == 2,
                  "O tabuleiro mudou ao pular o meeple.")
            check(skipped["drawnTile"] is None and skipped["placedTile"] is None,
                  "O próximo turno manteve uma peça pendente.")
            print("Smoke HTTP concluído.", flush=True)
        except BaseException:
            print(log_path.read_text(), file=sys.stderr)
            raise
        finally:
            signal.signal(signal.SIGINT, signal.SIG_IGN)
            signal.signal(signal.SIGTERM, signal.SIG_IGN)
            if server.poll() is None:
                server.terminate()
                try:
                    server.wait(timeout=10)
                except subprocess.TimeoutExpired:
                    server.kill()
                    server.wait(timeout=5)


try:
    main()
except KeyboardInterrupt:
    print("Smoke interrompido.", file=sys.stderr)
    sys.exit(130)
except Exception as error:
    print(f"Smoke FALHOU: {error}", file=sys.stderr)
    sys.exit(1)
PY
