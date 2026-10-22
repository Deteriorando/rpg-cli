# Sistema de entidade

O sistema de entidade foi feito para representar os personagens do
mundo do jogo. Existe uma classe abstrata genérica, `Entity`, e cada tipo
de entidade herda dela:

- `Player`: o jogador. É o único que possui inventário (veja `Inventory.md`).
- `Enemy`: o inimigo. Tem um nome e se concentra em combate; não possui inventário.

### Características de uma entidade

Toda entidade possui:

- Vida (`Life`), dano (`Damage`) e multiplicador (`Multiplier`);
- Uma classe (`Classes`): Human, Wizard, Witch, Warrior ou Berserk;
- Um efeito ativo opcional (`Effect`), como a postura defensiva;
- Um tempo de recarga da regeneração (`regenCooldown`).

### Limites dos atributos

Cada atributo gerencia os seus próprios valores e tem um intervalo válido:

| Atributo      | Mínimo | Máximo |
|---------------|--------|--------|
| Vida          | 820    | 2200   |
| Dano          | 10     | 120    |
| Multiplicador | 1      | 10     |

### Criação e alteração de valores

Os limites se comportam de forma diferente na criação e depois dela:

- **Ao criar a entidade (construtor):** qualquer valor fora do intervalo lança
`IllegalArgumentException`. Uma classe nula lança `NullPointerException`.
- **Ao alterar depois (setters):**
  - Vida: valores abaixo de 0 viram 0 (a entidade morre) e valores acima de 2200
  são cortados para 2200. Não lança exceção. Por isso a vida pode ficar
  abaixo de 820 durante o jogo; o mínimo de 820 só vale na criação.
  - Dano e Multiplicador: valores abaixo do mínimo lançam
  `IllegalArgumentException`, e valores acima do máximo são cortados para o máximo.

### Comportamentos

- **Ataque:** causa o dano da entidade. Existe 30% de chance de acerto
crítico, em que o dano é multiplicado pelo multiplicador.
- **Efeito:** se o alvo tiver um efeito ativo (como a postura defensiva), ele
reduz o dano recebido uma única vez e depois é removido.
- **Regeneração:** recupera vida igual ao valor da regeneração vezes o
multiplicador, e depois entra em recarga por 4 rodadas.
- **Morte:** uma entidade está viva enquanto a vida for maior que 0.
