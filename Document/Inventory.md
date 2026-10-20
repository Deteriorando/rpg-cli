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

- Encapsulamento e Segurança: A lista interna de slots é protegida contra
modificações diretas externas através de visualizações imutáveis (Collections.unmodifiableList).

### Regras de Negócio e Funcionalidades Principais

- Armazenamento de Itens: Permite a inserção controlada de instâncias da classe abstrata Item.
- Empilhamento Inteligente (Stacking): Itens marcados com a propriedade STACKABLE e dotados de
uma variável de controle de limite por pilha (maxStackSize) são agrupados automaticamente no mesmo
slot até atingirem o seu limite máximo.
- Transbordo Automático (Spillover): Caso um slot atinja o seu limite de pilha
(maxStackSize), o excedente de itens transborda automaticamente para abrir um novo slot,
desde que o limite físico total de slots do inventário não seja ultrapassado.

### Validações e Tratamento de Exceções

O sistema substituiu mensagens impressas no console por exceções customizadas e nativas para garantir
um fluxo robusto de tratamento de erros:

- Validação de Nulos e Quantidades: Lança IllegalArgumentException caso haja tentativa de inserir itens
nulos ou quantidades menores/iguais a zero.
- Controle de Capacidade: Lança InventoryFullException quando o jogador tenta adicionar itens e o inventário
já atingiu o limite máximo de slots físicos.
- Segurança contra estouros numéricos: Contém verificações matemáticas para prevenir estouros de capacidade
(Integer Overflow) em pilhas massivas.


