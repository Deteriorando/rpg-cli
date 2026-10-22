# Sistema de inventário

O sistema de inventário é a estrutura responsável por permitir
que o Player armazene, gerencie e utilize itens ao longo de sua
jornada no jogo de terminal. Para garantir um design limpo,
desacoplado e orientado a objetos, o inventário pertence exclusivamente
ao Jogador. Os inimigos (Enemy) concentram-se apenas em lógica de
combate e inteligência artificial, sendo totalmente isentos de
um sistema de inventário.

A classe `Inventory` em si é genérica e não conhece o jogador: ela só
organiza itens em slots. Quem a possui e decide o seu tamanho é o `Player`
(20 slots). Os itens que ela guarda estão descritos em
[Sistema de itens](Items.md).

### Estrutura de Armazenamento e Slots

- Gerenciamento por Slots (InventorySlot): O inventário gerencia os
  itens através de uma lista de slots (List<InventorySlot>), controlando
  de forma independente o limite físico de espaço da mochila (predefinido
  por um limite de slots para equilibrar a jogabilidade).

- Slots somente leitura para o mundo externo: cada slot guarda um item e
  uma quantidade. Quem está fora do Inventory só consegue consultar
  (getItem, getQuantity); criar e alterar slots é responsabilidade exclusiva
  do próprio inventário.

- Encapsulamento e Segurança: A lista interna de slots é protegida contra
  modificações diretas externas através de visualizações imutáveis
  (Collections.unmodifiableList).

### Regras de Negócio e Funcionalidades Principais

- Armazenamento de Itens (addItem): Permite a inserção controlada de
  instâncias da classe abstrata Item.

- Empilhamento Inteligente (Stacking): Itens marcados com a propriedade
  STACKABLE e dotados de uma variável de controle de limite por pilha
  (maxStackSize) são agrupados automaticamente no mesmo slot até atingirem
  o seu limite máximo. Itens não empilháveis ocupam um slot por unidade.

- Transbordo Automático (Spillover): Caso um slot atinja o seu limite de pilha
  (maxStackSize), o excedente de itens transborda automaticamente para abrir um
  novo slot, desde que o limite físico total de slots do inventário não seja
  ultrapassado.

- Adição atômica: antes de alterar qualquer coisa, o inventário confere se
  a quantidade inteira cabe (canAdd). Se não couber, nada é modificado: o
  jogador não fica com a mochila parcialmente preenchida.

- Remoção de Itens (removeItem): remove a quantidade pedida, consumindo
  primeiro os slots mais recentes e apagando os slots que ficarem vazios.
  Assim como a adição, é atômica: se o jogador não tiver a quantidade pedida,
  nada é removido.

- Consultas: countOf devolve quantas unidades de um item o jogador possui
  (somando todos os slots), e contains verifica se ele possui pelo menos
  uma certa quantidade.

- Identificação de itens: dois slots contêm "o mesmo item" quando têm o mesmo
  id (String), e não quando são a mesma instância em memória.

- Listagem (listInventory): imprime no console cada slot numerado, com o nome
  e o tipo do item. Dentro do jogo, a tela da mochila lê os slots por
  `getSlots()` e mostra também a quantidade de cada pilha.

### Validações e Tratamento de Exceções

O sistema substituiu mensagens impressas no console por exceções customizadas e nativas para garantir
um fluxo robusto de tratamento de erros:

- Validação de Nulos e Quantidades: Lança IllegalArgumentException caso haja tentativa de adicionar ou
  remover itens nulos ou quantidades menores/iguais a zero.
- Controle de Capacidade: Lança InventoryFullException quando o jogador tenta adicionar itens e a quantidade
  total não cabe nos slots disponíveis (canAdd permite verificar isso antes, sem lançar exceção).
- Remoção Inválida: Lança IllegalStateException quando se tenta remover mais itens do que o jogador possui.

### Exemplo

Inventário com 2 slots e pilha máxima de 10 (como a Poção de Vida), contendo
6 poções:

- addItem(poção, 8) funciona: 4 poções completam o primeiro slot (10/10) e
  as 4 restantes ocupam um novo slot. Ficam 14 poções em 2 slots.
- addItem(poção, 20) lança InventoryFullException: 4 completariam o primeiro
  slot e sobrariam 16, que exigiriam mais 2 slots (o inventário só tem 1 livre).
  As 6 poções continuam intactas, e o primeiro slot não chega a ser completado.

### Como usar a classe Inventory

Diretamente, sem o jogador:

```java
Inventory mochila = new Inventory(2);          // 2 slots
HealthPotion pocao = new HealthPotion(30);     // pilha de 10

mochila.addItem(pocao, 6);
mochila.canAdd(pocao, 14);    // true: 4 completam o slot e 10 ocupam o segundo
mochila.canAdd(pocao, 15);    // false: não cabe
mochila.addItem(pocao, 20);   // lança InventoryFullException; continua com 6
mochila.removeItem(pocao, 2); // agora são 4
mochila.countOf(pocao);       // 4
```

Ao usar a classe direto, confira com `canAdd` antes de adicionar ou trate a
`InventoryFullException`.

### Como usar o inventário pelo Player

O `Player` guarda o `Inventory` como um campo privado, então ninguém de fora
chama `addItem` ou `removeItem` diretamente. Tudo passa pelos métodos do
jogador, que devolvem `true` ou `false` em vez de lançar exceções para
situações normais do jogo, como a mochila cheia.

| Método                            | O que faz                                    | Devolve `false` quando                          |
|-----------------------------------|----------------------------------------------|-------------------------------------------------|
| `addItemToInventory(item)`        | Pega 1 unidade do item                       | Não cabe (nada é alterado)                      |
| `addItemToInventory(item, qtd)`   | Pega a quantidade pedida                     | Não cabe tudo (nada é alterado)                 |
| `dropItem(item, qtd)`             | Larga o item                                 | É `NON_DROPPABLE` ou o jogador não tem a qtd    |
| `useItem(item)`                   | Usa o item; se `CONSUMABLE`, gasta 1 unidade | O jogador não tem o item                        |
| `hasItem(item, qtd)`              | Confere se o jogador possui a quantidade     | (é a própria resposta)                          |
| `getInventorySlots()`             | Visão somente leitura dos slots              | —                                               |

Exemplo:

```java
Player player = new Player(Classes.HUMAN);
HealthPotion pocao = new HealthPotion(30);

player.addItemToInventory(pocao, 3);  // true
player.hasItem(pocao, 3);             // true
player.useItem(pocao);                // true: cura 30 e consome 1 (restam 2)
player.dropItem(pocao, 1);            // true (resta 1)
player.dropItem(pocao, 5);            // false: o jogador só tem 1
```

Quando a mochila está cheia, nada é adicionado, e quem chamou decide o que
mostrar ao jogador:

```java
if (!player.addItemToInventory(pocao, 5)) {
    // mochila cheia: mostre a mensagem que quiser
}
```

Para o menu escolher um item pelo número da tela (1 é o primeiro slot):

```java
List<Inventory.InventorySlot> slots = player.getInventorySlots();

if (numero >= 1 && numero <= slots.size()) {
    Item item = slots.get(numero - 1).getItem();
    player.useItem(item);
}
```

Uma observação: itens nulos e quantidades menores ou iguais a zero continuam
lançando `IllegalArgumentException`, mesmo pelo `Player`, porque são erros de
programação e não situações do jogo.

### Onde o inventário aparece no jogo

- **Início do jogo (`GameLoop`):** o jogador recebe 3 Poções de Vida.
- **Combate (`Controller`):** a opção *Usar item* chama o
  `InventoryController`, que mostra a mochila (`MenuInterface.inventoryScreen`),
  lê o número escolhido e chama `Player.useItem`. Usar um item gasta o turno, e
  digitar 0 volta sem perdê-lo.