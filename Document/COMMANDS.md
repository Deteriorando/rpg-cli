## Guia de comandos no Linux para executar o jogo

Todos os comandos abaixo devem ser rodados **na pasta raiz do projeto**
(a que contém o `pom.xml` e a pasta `src`).

Requisitos: **JDK 25** (ou mais novo) e, para a opção 1, o [Maven](https://maven.apache.org/).

### Opção 1: com o Maven (recomendada)

Gerar o `.jar` e jogar:

```bash
mvn package
java -jar target/RPG-CLI-0.4.jar
```

O nome do arquivo segue o `artifactId` e a `version` do `pom.xml`
(`RPG-CLI` e `0.4`), então ele muda quando a versão mudar.

Se preferir rodar sem gerar o `.jar`:

```bash
mvn compile
java -cp target/classes dete.org.Main
```

### Opção 2: sem o Maven, só com o `javac`

Compilar (as classes vão para a pasta `bin`):

```bash
javac -encoding UTF-8 -d bin -sourcepath src/main/java src/main/java/dete/org/Main.java
```

Executar:

```bash
java -cp bin dete.org.Main
```

Compilar e rodar em uma única linha (útil no dia a dia):

```bash
javac -encoding UTF-8 -d bin -sourcepath src/main/java src/main/java/dete/org/Main.java && java -cp bin dete.org.Main
```

### Gerar um `.jar` sem o Maven

Depois de compilar com o `javac` (opção 2), o `jar` cria o arquivo já informando
a classe principal. Não é preciso criar um `manifest.txt`:

```bash
jar cfe RPG-CLI.jar dete.org.Main -C bin .
```

Para jogar:

```bash
java -jar RPG-CLI.jar
```

### Observações

- A classe principal é `dete.org.Main`, com o nome do pacote. Usar só `Main`
  dá o erro de "classe principal não encontrada".
- O jogo usa acentos e emojis. Se eles aparecerem quebrados, confira se o
  terminal está configurado para UTF-8. No Windows, o Windows Terminal costuma
  funcionar sem ajustes; no `cmd` antigo, rode `chcp 65001` antes.