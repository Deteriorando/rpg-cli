# Sistema de inventário

O inventário ele é um sistema onde o jogador poderá guarda ‘items’
para ser usado. Cada ‘item’ será especifíco em classes do java
e isso é importante para manter desacoplamento.

O Sistema de inventário será somente para o jogador, ou seja,
somente na classe jogador, o inimigo não vai precisa ter um sistema
de inventário próprio(e nem é necessário).

A classe ```Inventory``` ela guarda os ‘items’ num Array do tipo
‘Item’ que vai ser a base de transporte que o jogador vai ter durante
o jogo. O Inventário ele tem um limite de 8 slots para o jogador, esse
é o limite para o equilibrio o jogo.
