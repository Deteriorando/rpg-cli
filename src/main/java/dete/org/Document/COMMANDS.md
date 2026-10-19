## Guia de comandos no linux para execultável o jogo

- Compilação e Execução


```bash
javac -d bin -sourcepath src/main/java src/main/java src/main/java/dete/org/Main.java
```

- Executar o jogo a partir da pasta bin:

```bash
cd "/home/linux/Projetos/Jogos/Joguinho de turno/rpg-cli"
```

- Compilar e rodar em uma única linha (muito útil no dia a dia):

```bash
  cd "/home/linux/Projetos/Jogos/Joguinho de turno/rpg-cli" &&
  javac -d bin -sourcepath src/main/java src/main/java/dete/org/Main.java && java -cp bin Main
```

Código para fazer um arquivo manifest:
```bash
jar cvfm Joguinho-de-turno.jar manifest.txt Main.Java
```

- Comando para rodar o projeto:
```bash
java -jar meu-projeto.jar
```