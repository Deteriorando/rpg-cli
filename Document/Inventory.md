# Sistema de inventário

O sistema de inventário é a estrutura responsável por permitir
que o Player armazene, gerencie e utilize itens ao longo de sua
jornada no jogo de terminal. Para garantir um design limpo,
desacoplado e orientado a objetos, o inventário pertence exclusivamente
ao Jogador. Os inimigos (Enemy) concentram-se apenas em lógica de
combate e inteligência artificial, sendo totalmente isentos de
um sistema de inventário.

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
e o tipo do item.

### Validações e Tratamento de Exceções

O sistema substituiu mensagens impressas no console por exceções customizadas e nativas para garantir
um fluxo robusto de tratamento de erros:

- Validação de Nulos e Quantidades: Lança IllegalArgumentException caso haja tentativa de adicionar ou
remover itens nulos ou quantidades menores/iguais a zero.
- Controle de Capacidade: Lança InventoryFullException quando o jogador tenta adicionar itens e a quantidade
total não cabe nos slots disponíveis (canAdd permite verificar isso antes, sem lançar exceção).
- Remoção Inválida: Lança IllegalStateException quando se tenta remover mais itens do que o jogador possui.

### Exemplo

Inventário com 2 slots, pilha máxima de 64, contendo 60 poções:

- addItem(poção, 68) funciona: 4 poções completam o primeiro slot (64/64) e
as 64 restantes ocupam um novo slot.
- addItem(poção, 100) lança InventoryFullException: 4 completariam o primeiro
slot e sobrariam 96, que exigiriam mais 2 slots (o inventário só tem 1 livre).
As 60 poções continuam intactas, e o primeiro slot não chega a ser completado.
