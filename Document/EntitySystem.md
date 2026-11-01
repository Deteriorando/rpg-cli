# Sistema de entidade

O sistema de entidade foi feito para representar os personagens do
mundo do jogo. Existe uma classe abstrata genérica, `Entity`, e cada tipo
de entidade herda dela:

- `Player`: o jogador. É o único que possui inventário (veja `Inventory.md`).
- `Enemy`: o inimigo. Se concentra em combate e não possui inventário.

### Características de uma entidade

Toda entidade possui:

- Um nome (`getName()`). Se nenhum nome for informado, usa o nome da classe
  Java (por exemplo, "Player");
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
    abaixo de 820 durante o jogo; o mínimo de 820 só vale na criação. A vida
    tem os métodos `takeDamage` e `regenerate`, que passam por essa mesma regra.
  - Dano e Multiplicador: valores abaixo do mínimo lançam
    `IllegalArgumentException`, e valores acima do máximo são cortados para o máximo.

### Comportamentos

- **Ataque:** causa o dano da entidade. Existe 30% de chance de acerto
  crítico, em que o dano é multiplicado pelo multiplicador. O ataque devolve um
  `AttackResult` (o dano causado e se foi crítico), e quem mostra isso na tela é
  o presenter, não a entidade.
- **Efeito:** se o alvo tiver um efeito ativo (como a postura defensiva), ele
  reduz o dano recebido uma única vez e depois é removido.
- **Regeneração:** existe 30% de chance de o valor da regeneração ser
  multiplicado pelo multiplicador. O valor calculado é somado à vida (que nunca
  passa de 2200) e devolvido. Quando o inimigo regenera, a regeneração entra em
  recarga por 4 rodadas (`triggerRegenCooldown`). A Poção de Vida usa essa mesma
  regeneração, então vale a mesma regra de 30%.
- **Morte:** uma entidade está viva enquanto a vida for maior que 0.

### Atributos por classe e criação de entidades

Cada classe (`Classes`) guarda os seus atributos iniciais (vida, dano e
multiplicador) em um único lugar: o próprio enum, em objetos `ClassStats`.
Cada classe tem dois conjuntos, um para o jogador e outro para o inimigo, que
podem ser diferentes.

- **Jogador:** `new Player(Classes.HUMAN, "Dete")`. Os atributos vêm da classe.
  O nome é opcional (`new Player(Classes.HUMAN)` usa "Player"). Para trocar de
  classe usa-se `player.changeClass(...)`, que o jogo chama pela opção *Escolha
  uma Classe* do menu (`ConfigurePlayer`). No fim de cada combate,
  `player.resetToClassStats()` remove o efeito ativo, zera a recarga e restaura
  os atributos da classe.
- **Inimigo:** `new Enemy(classe, nome)`. A cada combate,
  `ConfigureEnemy.createRandomEnemy()` cria um inimigo novo, com classe e nome
  sorteados, então nada de uma luta passa para a próxima.
- **Sem construtor com números soltos:** não é possível criar uma entidade
  com uma classe e atributos que não combinam, porque os atributos sempre vêm
  da classe.
- **Nova classe:** basta adicionar uma constante em `Classes` com os dois
  `ClassStats` (jogador e inimigo).
- Os limites da tabela acima continuam valendo: se um `ClassStats` estiver
  fora deles, a `IllegalArgumentException` aparece ao criar a entidade.