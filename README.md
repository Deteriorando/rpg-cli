# RPG CLI

Um RPG de terminal feito em Java, com combate por turnos, classes e
um sistema de inventário.

### Introdução

Olá! Seja bem-vindo ao RPG CLI, um RPG feito para terminal, sendo um jogo
bem simples para se explorar as mecânicas.

Fiz esse jogo para ser simples de fazer, e não é nada tão sério. Porém
decidi fazê-lo em Java, por ser uma linguagem que eu domino e quero
aprimorar as habilidades nela.

O jogo é feito para ser simples, mas divertido, para as pessoas jogarem em
um terminal comum. Talvez em algum momento futuro eu faça o jogo crescer com
uma interface gráfica e mais mecânicas. Mas por enquanto é só isso.

**ATENÇÃO: o jogo continua incompleto!**

### Como jogar

No menu principal você pode começar um combate, ver o seu perfil, escolher
uma classe ou sair.

Durante o combate, a cada rodada você escolhe uma ação:

1. **Atacar:** causa dano ao inimigo. Existe uma chance de acerto crítico,
   em que o dano é multiplicado.
2. **Defender:** reduz em 15% o dano do próximo ataque que você receber.
3. **Usar item:** abre a sua mochila para usar um item (veja abaixo).

O inimigo é novo a cada combate, com classe e nome sorteados. A luta termina
quando a vida de um dos dois chega a zero.

### Classes

Human, Wizard, Witch, Warrior e Berserk. Cada classe tem seus próprios
valores de vida, dano e multiplicador, e você pode trocar a sua pelo menu
principal. Mais detalhes em [Sistema de entidade](Document/EntitySystem.md).

### Sistema de inventário

O jogador carrega uma mochila com **8 slots**. Os inimigos não têm
inventário, só o jogador.

- **Empilhamento:** itens empilháveis (como a poção) dividem o mesmo slot até
  o limite da pilha. Quando a pilha enche, o que sobrou abre um novo slot.
- **Mochila cheia:** se não couber tudo, nada é adicionado. A mochila nunca
  fica preenchida pela metade.
- **Usar itens:** no combate, a opção *Usar item* mostra a mochila numerada,
  com a quantidade de cada pilha. Você escolhe o número do item, ou digita 0 para
  voltar sem perder o turno. Usar um item gasta a sua ação da rodada, e itens
  consumíveis perdem uma unidade a cada uso.
- **Poção de Vida:** recupera vida de quem a usa. Você começa o jogo com 3.
- **Propriedades:** cada item tem propriedades (empilhável, consumível,
  equipável, item-chave, não largável e negociável) e algumas combinações são
  proibidas para evitar itens contraditórios.

Para entender as regras em detalhe, veja [Sistema de inventário](Document/Inventory.md)
e [Sistema de itens](Document/Items.md).

**O que ainda falta:** os itens só aparecem no início do jogo (ainda não há
itens deixados pelos inimigos), e as propriedades *equipável*, *item-chave* e
*negociável* existem, mas ainda não têm efeito no jogo.

### Documentação

- [Sistema de entidade](Document/EntitySystem.md): atributos, classes e como as entidades são criadas
- [Sistema de inventário](Document/Inventory.md): slots, empilhamento e regras
- [Sistema de itens](Document/Items.md): itens e propriedades

### Como rodar

Requisitos: **JDK 25** e [Maven](https://maven.apache.org/).

```bash
mvn compile
java -cp target/classes dete.org.Main
```
