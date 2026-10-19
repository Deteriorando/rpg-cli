# Sistema de entidade

O sistema de entidade ele foi feito para ter entidade
no mundo normal. Cada entidade ela tem características 
específicas, o que exige que tenha uma classe génerica
para cada entidade que herda dessa classe génerica.

Para uma entidade ser instanciada, ela precisa que tenha requisitos
mínimos para ser uma entidade válida, que são:

- Ter no mínimo 820 de vida
- O Dano mínimo é 10 de vida
- O Multiplicador precisa ter valor 1

Caso ao contrário, a entidade que estiver a ser instanciada
com diferentes valores ela vai dar `IllegalArgumentException`
