#!/bin/bash

# Compila e executa o RPG CLI.
# A pasta do projeto é descoberta a partir da localização deste script
# (scripts/bash/), então o caminho funciona mesmo se o projeto for movido.

PROJECT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"

# Compila os fontes em target/classes e roda a classe principal (dete.org.Main)
RUN_CMD="cd \"$PROJECT_DIR\" && javac -d target/classes -sourcepath src/main/java src/main/java/dete/org/Main.java && java -cp target/classes dete.org.Main"

# Abre numa nova janela do terminal, se o emulador existir; senão, roda aqui mesmo.
if command -v x-terminal-emulator >/dev/null 2>&1; then
    x-terminal-emulator -e bash -c "$RUN_CMD; exec bash"
else
    bash -c "$RUN_CMD"
fi
