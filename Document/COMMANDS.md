## Guia de comandos no Linux para executar o jogo

Todos os comandos abaixo devem ser rodados **na pasta raiz do projeto**
(a que contém o `pom.xml` e a pasta `src`).

Requisitos: **JDK 25** (ou mais novo) e o [Maven](https://maven.apache.org/).

### Buildar o jogo

O projeto tem o script `build.sh`, que roda o comando:

```bash
mvn clean package
```

- `clean` apaga a pasta `target`, com os arquivos de builds anteriores.
- `package` compila o jogo e gera o arquivo `.jar` dentro de `target`.

Para rodar o script:

```bash
bash build.sh
```

O arquivo `build.sh` não tem permissão de execução. Se preferir usar
`./build.sh`, rode antes `chmod +x build.sh`. Também dá para rodar o comando
`mvn clean package` direto.

No fim do build, o arquivo gerado é o `target/RPG-CLI-0.4.jar`. O nome vem do
`artifactId` e da `version` do `pom.xml` (`RPG-CLI` e `0.4`), então ele muda
quando a versão mudar.

### Executar o jogo

Com o `.jar` gerado, execute:

```bash
java -jar <nome do arquivo>
```

Por exemplo:

```bash
java -jar target/RPG-CLI-0.4.jar
```

No lugar de `<nome do arquivo>`, use o caminho do `.jar` que está em `target`.

### Observações

- O jogo usa acentos e emojis. Se eles aparecerem quebrados, confira se o
  terminal está configurado para UTF-8. No Windows, o Windows Terminal costuma
  funcionar sem ajustes; no `cmd` antigo, rode `chcp 65001` antes.