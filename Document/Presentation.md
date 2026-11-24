# Camada de Apresentação

A camada de apresentação é a responsável por
**exibir o jogo ao jogador** e por receber as
escolhas dele. No projeto, ela corresponde ao
pacote `view`.

## Situação atual

Por enquanto existe apenas a interface em **terminal**
(`view/terminal`). Uma interface gráfica pode ser criada
no futuro, mas ela não faz parte do escopo atual.

## Como ela se comunica com o core

A camada de apresentação se comunica com o `core`
por meio dos **presenters**.

Os presenters são **interfaces** declaradas no próprio
`core` (pacote `core/presenters`). O `core` chama essas
interfaces sempre que precisa mostrar algo ao jogador,
sem saber como nem onde isso será exibido. Quem implementa
as interfaces é a camada de apresentação (em `view/terminal/presenters`, no caso do terminal).

Dessa forma, a dependência fica assim:

- a `view` depende do `core`, porque implementa as interfaces que ele define;
- o `core` **não depende da `view`**, porque conhece apenas as interfaces.

```
core                                  view/terminal
────                                  ─────────────
chama CombatPresenter     ◀── implementa ── presenter de combate
chama MenuPresenter       ◀── implementa ── presenter de menu
chama InventoryPresenter  ◀── implementa ── presenter de inventário
```

## Por que funciona assim: modularidade

Essa organização existe para manter o projeto
**modular**. Como o `core` só fala com interfaces,
trocar o terminal por uma interface gráfica exige
apenas implementar essas mesmas interfaces na nova
camada de apresentação.

O `core` não precisa ser alterado nem saber que a
interface mudou, e o funcionamento do jogo continua
o mesmo.