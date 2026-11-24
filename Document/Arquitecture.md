# Arquitetura do Projeto

Este documento descreve como o projeto é organizado
em camadas, o que cada camada faz e como elas se comunicam.

## Visão geral

O projeto é dividido em três camadas:

| Camada | Pacote | Papel |
|--------|--------|-------|
| **Core** | `core` | Base do projeto. Contém todas as regras do jogo e **define as interfaces** pelas quais se comunica com o mundo externo. Não depende de nenhuma outra camada. |
| **View** | `view/terminal` | Interface com o jogador. **Implementa** as interfaces definidas pelo `core` usando o terminal. |
| **Init** | `init` | Montagem e execução. Cria o jogo, liga a `view` ao `core` e roda o laço principal. |

O ponto de entrada é a classe `Main.java`.

## Estrutura de pacotes

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

## Regra de dependência

A dependência vai em um único sentido:

```
   init ─────┐
             ├──▶ core
   view ─────┘
```

- `init` e `view` usam o `core`.
- O `core` **nunca** importa nada de fora dele.

Essa regra garante que as regras do jogo possam
ser entendidas, testadas e evoluídas sem conhecer
o terminal ou qualquer outra interface.

## Como o core e a view se comunicam

O `core` não lê nem escreve no terminal. Em vez
disso, ele **define os contratos** de comunicação
com o jogador, e a camada `view` **os implementa**.
Esse é o ponto central da arquitetura: o `core` dita
a interface, e quem depende dela é a `view`, não o contrário.

### Saída: presenters

Para mostrar algo ao jogador, o `core` chama uma
interface de **presenter**, declarada em `core/presenters`:

- `CombatPresenter`: exibição do combate;
- `MenuPresenter`: exibição dos menus;
- `InventoryPresenter`: exibição do inventário.

As implementações para o terminal ficam em `view/terminal/presenters`.

### Entrada: PlayerInput

Para receber uma escolha do jogador, o `core` usa a interface **`PlayerInput`**, declarada em `core/input`. Na versão atual, a implementação lê os dados do terminal por meio do `InputScanner`.

### Resumo do fluxo

```
core (define as interfaces)          view/terminal (implementa)
───────────────────────────          ──────────────────────────
CombatPresenter     ◀── implementa ── presenter de combate
MenuPresenter       ◀── implementa ── presenter de menu
InventoryPresenter  ◀── implementa ── presenter de inventário
PlayerInput         ◀── implementa ── leitura via terminal
```

O `init` cria essas implementações e as entrega ao `core` ao montar o jogo.

## Por que essa organização

Como o `core` só conhece as interfaces, o terminal ou uma futura interface gráfica
precisam apenas implementar `CombatPresenter`, `MenuPresenter`, `InventoryPresenter`
e `PlayerInput`. As regras do jogo continuam as mesmas, sem nenhuma alteração no `core`.