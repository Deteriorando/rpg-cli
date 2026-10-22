# Sistema de itens

Os itens são a parte de recursos do jogador. Eles podem ser usados de
algumas formas: podem ser empilháveis, consumíveis, largáveis e assim por
diante, de acordo com as suas propriedades.

Todo item herda da classe abstrata `Item`, e cada item específico (como a
`HealthPotion`) é definido criando uma classe nova. Quem guarda os itens do
jogador é o inventário (veja [Sistema de inventário](Inventory.md)).

### Estrutura de um item

Todo item possui:

- **Id:** um texto que identifica o item. É guardado sempre em maiúsculas.
- **Nome e descrição:** o texto mostrado ao jogador.
- **Tipo (`ItemType`):** hoje existem `TOOL` e `POTION`. O tipo aparece entre
  colchetes quando a mochila é mostrada.
- **Propriedades (`ItemProperties`):** definem como o item se comporta
  (veja abaixo).
- **Limite de pilha (`maxStackSize`):** quantas unidades cabem em um slot.
- **Efeito (`use`):** cada item define o que acontece quando é usado.

As propriedades são definidas na criação e **não mudam depois**. As
propriedades ficam em um conjunto (`Set`), então não podem ser repetidas.

### Propriedades

| Propriedade     | Significado                                | Situação hoje          |
|-----------------|--------------------------------------------|------------------------|
| `STACKABLE`     | Pode ser empilhado em um mesmo slot        | Funciona               |
| `CONSUMABLE`    | Perde uma unidade a cada uso               | Funciona               |
| `NON_DROPPABLE` | Não pode ser largado                       | Funciona (no `Player`) |
| `EQUIPPABLE`    | Pode ser equipado                          | Ainda sem efeito       |
| `KEY_ITEM`      | Item-chave, importante para a história     | Ainda sem efeito       |
| `TRADABLE`      | Pode ser negociado                         | Ainda sem efeito       |

### Regras e validações

Um item pode ter várias propriedades juntas, mas nunca todas, e algumas
combinações são proibidas. As regras são conferidas na criação do item, e uma
violação lança `IllegalArgumentException`:

- `CONSUMABLE`, `EQUIPPABLE` e `KEY_ITEM` não podem estar juntas no mesmo item.
- `NON_DROPPABLE` não pode estar junta com `TRADABLE`.
- Um item `STACKABLE` precisa ter limite de pilha maior ou igual a 1.
- O limite de pilha nunca pode ser negativo.

Passar `null` no lugar da lista de propriedades lança `NullPointerException`.

### Itens e inventário

O inventário só conhece o item pelas suas características. Ele lê as
propriedades e o limite de pilha para decidir como guardar:

- **Identificação pelo id:** dois itens são considerados o mesmo item quando
  têm o mesmo id, e não quando são o mesmo objeto. Por isso, versões do mesmo
  item com efeitos diferentes (por exemplo, uma poção que cura 30 e outra que
  cura 50) precisam de **ids diferentes**. Com o mesmo id, elas empilhariam juntas.
- **`STACKABLE` e `maxStackSize`:** itens empilháveis dividem um slot até o
  limite da pilha, e o que sobra abre um novo slot. Itens não empilháveis ocupam
  um slot por unidade, e o limite de pilha deles é ignorado.
- **`CONSUMABLE`:** ao usar o item, o `Player` remove uma unidade do
  inventário. Itens sem essa propriedade são usados sem se gastar.
- **`NON_DROPPABLE`:** o `Player` recusa largar esse item. (Ainda não existe
  uma tela no jogo para largar itens.)

### Usando um item

O caminho de um item usado durante o combate é:

1. O jogador escolhe a opção *Usar item* e o número do item na mochila.
2. O `Player` confere se possui o item e chama `item.use(this)`, passando
   quem está usando, para o item poder aplicar o efeito nele.
3. Se o item for `CONSUMABLE`, o `Player` remove uma unidade do inventário.

O item não conhece o inventário: ele só sabe o que fazer com quem o usa.
Quem mexe nas quantidades é o `Player`.

### Criando um item novo

Para criar um item, crie uma classe que herda de `Item`, passe os dados no
construtor e escreva o efeito em `use`. Exemplo, a poção de vida:

```java
public class HealthPotion extends Item {
    private final double healAmount;

    public HealthPotion(double healAmount) {
        super("HEALTH_POTION", "Poção de Vida", "Recupera " + healAmount + " de vida.",
                ItemType.POTION, 10, ItemProperties.STACKABLE, ItemProperties.CONSUMABLE);
        this.healAmount = healAmount;
    }

    @Override
    public void use(Entity user) {
        user.setLife(user.getLife().getValue() + healAmount);
    }
}
```

Essa poção é empilhável (até 10 por slot) e consumível. A vida de quem a usa
nunca passa do máximo permitido (veja [Sistema de entidade](EntitySystem.md)).