#!/bin/bash

# Abre uma nova janela do terminal, entra na pasta e roda o comando
x-terminal-emulator -e bash -c 'cd "/home/linux/Projetos/Jogos/Joguinho de turno/Joguinho de turno" && javac -d bin -sourcepath src/main/java src/main/java/dete/org/Main.java && java -cp bin Main; exec bash'