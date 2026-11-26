#!/bin/bash

# Compila e executa o RPG CLI.
# A pasta do projeto é descoberta a partir da localização deste script
# (scripts/bash/), então o caminho funciona mesmo se o projeto for movido.

PROJECT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"

# Empacota o jogo com os recursos e as dependências Maven.
RUN_CMD='cd "$1" && mvn -q package && java -jar target/RPG-CLI-0.4.jar'

# Abre numa nova janela do terminal, se o emulador existir; senão, roda aqui mesmo.
if command -v x-terminal-emulator >/dev/null 2>&1; then
    x-terminal-emulator -e bash -c "$RUN_CMD; exec bash" bash "$PROJECT_DIR"
else
    bash -c "$RUN_CMD" bash "$PROJECT_DIR"
fi
