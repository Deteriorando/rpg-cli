# RPG CLI

Um RPG de terminal feito em Java, com combate por turnos, classes e
um sistema de inventário.

### Sobre

Olá! Seja bem-vindo ao RPG CLI, um RPG feito para terminal, sendo um jogo
bem simples para se explorar as mecânicas.

Fiz esse jogo para ser simples de fazer, e não é nada tão sério. Porém
decidi fazê-lo em Java, por ser uma linguagem que eu domino e quero
aprimorar as habilidades nela.

O jogo é feito para ser simples, mas divertido, para as pessoas jogarem em
um terminal comum. Talvez em algum momento futuro eu faça o jogo crescer com
uma interface gráfica e mais mecânicas. Mas por enquanto é só isso.

Feito com Java 25 e Maven.

**ATENÇÃO: Ainda em Desenvolvimento para a versão 1.0!**

### Como jogar

Em todas as telas, digite o número da opção e aperte ENTER.

No menu principal:

1. **Start:** começa um combate contra um inimigo sorteado.
2. **Perfil:** mostra os atributos do seu personagem.
3. **Escolha uma Classe:** mostra a lista de classes numerada para você trocar de classe.
4. **Sair:** fecha o jogo.

Durante o combate, a cada rodada você escolhe uma ação:

1. **Atacar:** causa dano ao inimigo. Existe uma chance de acerto crítico,
   em que o dano é multiplicado.
2. **Defender:** reduz em 15% o dano do próximo ataque que você receber.
3. **Usar item:** abre a sua mochila para usar um item (veja o inventário abaixo).

Depois da sua ação, o inimigo age, e o jogo pede ENTER para seguir para a
próxima rodada. Se você digitar uma opção inválida (um número que não existe ou
um texto), perde a vez. A luta termina quando a vida de um dos dois chega a
zero, e no fim os atributos do seu personagem voltam aos da classe.

### Sistemas principais

**Entidades e classes:** o jogador (`Player`) e o inimigo (`Enemy`) têm vida,
dano, multiplicador e uma classe: Human, Wizard, Witch, Warrior ou Berserk. Cada
classe tem seus próprios valores iniciais, um conjunto para o jogador e outro
para o inimigo. Detalhes em [Sistema de entidade](Document/EntitySystem.md).

**Combate:** é por turnos, com um `Controller` que conduz as rodadas. O inimigo
é novo a cada combate, com classe e nome sorteados, e a cada turno sorteia uma
ação: atacar, defender ou regenerar (se a regeneração estiver em recarga, ele
ataca). A regeneração e o ataque têm 30% de chance de aplicar o multiplicador.

**Inventário:** o jogador carrega uma mochila com **8 slots**. Os inimigos não
têm inventário, só o jogador.

- **Empilhamento:** itens empilháveis (como a poção) dividem o mesmo slot até
  o limite da pilha. Quando a pilha enche, o que sobrou abre um novo slot.
- **Mochila cheia:** se não couber tudo, nada é adicionado. A mochila nunca
  fica preenchida pela metade.
- **Usar itens:** no combate, a opção *Usar item* mostra a mochila numerada,
  com a quantidade de cada pilha. Você escolhe o número do item, ou digita 0 para
  voltar sem perder o turno. Usar um item gasta a sua ação da rodada, e itens
  consumíveis perdem uma unidade a cada uso.
- **Largar itens:** a regra já existe (itens não largáveis são recusados),
  mas o jogo ainda não tem uma opção para largar.

**Itens:** o único item hoje é a **Poção de Vida**, que recupera vida de quem a
usa (como usa a regeneração, tem 30% de chance de ter a cura multiplicada). Você
começa o jogo com 10 poções de cura 80. Cada item tem propriedades (empilhável,
consumível, equipável, item-chave, não largável e negociável), e algumas
combinações são proibidas para evitar itens contraditórios.

Mais regras em [Sistema de inventário](Document/Inventory.md) e
[Sistema de itens](Document/Items.md).

### Arquitetura

O código é dividido em duas partes: o **`core`**, com as regras do jogo, e a
**`view/terminal`**, tudo o que conversa com o terminal.

```
src/main/java/dete/org
├── Main.java                # ponto de entrada
├── init/                    # GameLoop: monta o jogo e roda o laço principal
├── core/                    # regras do jogo, sem nada de terminal
│   ├── controller/          # fluxo do combate (Controller e as ações do jogador e do inimigo)
│   ├── entity/              # Entity, Player, Enemy, classes e atributos
│   ├── inventory/           # Inventory, Item, propriedades e itens
│   ├── input/               # PlayerInput, UserAction e EnemyInput
│   ├── presenters/          # interfaces que o core usa para mostrar algo ao jogador
│   └── dto/                 # resultados simples, como o AttackResult
└── view/terminal/           # tudo o que fala com o terminal
    ├── presenters/          # implementações dos presenters para o terminal
    └── ...                  # telas (CombatUI, MenuIU, InventoryUI), InputScanner e TerminalUI
```

- **A dependência vai só em um sentido:** a `view` e o `init` usam o `core`,
  e o `core` nunca importa nada da `view`.
- **O `core` não lê nem escreve no terminal.** Para mostrar algo, ele chama uma
  interface de `presenters` (`CombatPresenter`, `MenuPresenter` e
  `InventoryPresenter`). Para receber uma escolha do jogador, ele usa o
  `PlayerInput`.
- **O terminal implementa essas interfaces** com `TerminalActionPresenter`,
  `TerminalMenuPresenter`, `TerminalInventoryPresenter` e `InputScanner`.
- **O `GameLoop` cria essas implementações** e as entrega ao `Controller`.

Uma interface gráfica poderia entrar implementando as mesmas interfaces
(os três presenters e o `PlayerInput`), sem mudar as regras do `core`.
E isso é feito para o core ficar independente de qualquer interface gráfica
que deseje implementar a qualquer momento.

### Documentação

- [Sistema de entidade](Document/EntitySystem.md): atributos, classes e como as entidades são criadas
- [Sistema de inventário](Document/Inventory.md): slots, empilhamento e regras
- [Sistema de itens](Document/Items.md): itens e propriedades
- [Guia de comandos](Document/COMMANDS.md): como buildar e executar o jogo

### Como executar

Requisitos: **JDK 25** e [Maven](https://maven.apache.org/).
Recomendação de IDE: [IntelliJ IDEA](https://www.jetbrains.com/idea/download).

Rode o [build.sh](build.sh) para fazer o build do jogo e depois execute o `.jar`
gerado:

```bash
bash build.sh
java -jar target/RPG-CLI-0.4.jar
```

Mais detalhes no [Guia de comandos](Document/COMMANDS.md).

### Estado atual / próximos passos

**O que funciona hoje:**
- Combate por turnos contra inimigos aleatórios, com as cinco classes.
- Inventário de 8 slots, com empilhamento, e a Poção de Vida.
- O `core` separado do terminal, por meio de presenters e do `PlayerInput`.

**O que ainda falta:**
- Os itens só aparecem no início do jogo (ainda não há itens deixados pelos inimigos).
- As propriedades *equipável*, *item-chave* e *negociável* existem, mas ainda não têm efeito.
- Largar itens: a regra existe, mas ainda não há uma opção no jogo.
- O combate ainda é um laço que espera a escolha do jogador, o que precisará ser
  adaptado para uma interface gráfica.

**Ideias para os próximos passos:**
- Interface gráfica.