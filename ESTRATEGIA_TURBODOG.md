# Estrategia TurboDog

## Integrantes

- Artur Malheiros
- Bruno Rocha

## Objetivo

O objetivo principal da estrategia do agente `TurboDog` foi maximizar o tempo de sobrevivencia da equipe dentro da arena. Para isso, priorizamos tres ideias centrais:

- cobrir o mapa de forma simples e organizada em busca de cogumelos;
- reduzir gasto desnecessario de energia;
- evitar combates que, na maioria dos testes, se mostraram pouco vantajosos.

Nosso foco nao foi construir um agente excessivamente complexo, e sim uma solucao clara, funcional e facil de observar durante a execucao.

## Ideia Inicial

A ideia inicial da equipe foi fazer uma varredura pelo mapa para localizar fontes de energia o mais cedo possivel. Como o mapa possui grande area e os agentes nascem em grupo, decidimos dividir os agentes em papeis diferentes para evitar que todos seguissem exatamente o mesmo caminho.

Assim, o agente foi organizado em tres grupos:

- grupo 0: vai inicialmente para a regiao central e, depois de chegar ao centro, se espalha de forma aleatoria;
- grupo 1: explora o mapa com movimento predominantemente vertical;
- grupo 2: explora o mapa com movimento predominantemente horizontal.

Essa divisao foi escolhida para aumentar a cobertura do mapa e diminuir a chance de concentracao excessiva de agentes em uma unica regiao logo no inicio da partida.

## Como a Estrategia Funciona

### 1. Exploracao do mapa

Cada agente recebe um papel com base em `getId() % 3`. Isso faz com que, desde o inicio da simulacao, a equipe se distribua entre comportamentos diferentes.

- O grupo que vai ao centro tenta rapidamente ocupar a regiao central do mapa, pois essa area oferece boas possibilidades de redistribuicao posterior.
- Os grupos vertical e horizontal fazem exploracao mais linear, ajudando a cobrir diferentes faixas da arena.
- Quando o agente encontra uma borda, ele troca de direcao para continuar a exploracao.

Essa abordagem foi escolhida por ser simples e robusta. Durante nossos testes, solucoes muito complexas de movimentacao nem sempre trouxeram melhora real no tempo de vida.

### 2. Coleta de energia

Quando um agente recebe energia em um turno, ele interpreta isso como sinal de que encontrou um cogumelo vantajoso. Nesse caso, ele:

- avisa aliados proximos com sua posicao;
- para de se mover;
- permanece no local para continuar aproveitando a fonte de energia.

Nos testes que realizamos, continuar se movendo logo depois de encontrar uma boa fonte geralmente nao foi uma boa escolha. Em muitos casos, o agente gastava energia procurando outra oportunidade sem necessidade, enquanto poderia permanecer parado absorvendo mais energia com menor custo.

Por isso, decidimos que, ao encontrar um cogumelo util, a melhor acao na maior parte do tempo era simplesmente parar.

### 3. Comunicacao entre agentes

A coordenacao da equipe foi feita por mensagens locais, respeitando a restricao do desafio de nao usar variaveis `static`.

Quando um agente encontra energia, ele envia uma mensagem no formato:

`ENERGIA:x:y`

Os agentes aliados que recebem essa mensagem passam a tentar seguir aquele alvo por alguns turnos. Essa decisao permite que mais de um agente aproveite a mesma regiao energetica, aumentando a eficiencia da equipe sem precisar de uma memoria global compartilhada.

Escolhemos uma perseguicao curta para esses avisos, pois seguir um alvo por tempo demais tambem pode gerar desperdicio de energia caso a oportunidade ja tenha deixado de existir.

### 4. Combate

Durante os testes, observamos que lutar quase nunca foi uma boa ideia para nosso objetivo principal, que era sobreviver o maximo possivel.

Por esse motivo, nossa estrategia trata combate como situacao secundaria:

- se o inimigo aparenta estar em vantagem energetica, o agente tenta sair da situacao;
- o foco principal nao e eliminar adversarios, e sim preservar energia e continuar vivo.

Essa escolha veio diretamente da pratica. Em varias simulacoes, entrar em combate fazia os agentes perderem energia rapidamente e diminuia o tempo total de vida da equipe.

## Decisoes Tomadas a Partir dos Testes

Ao longo do desenvolvimento, varias ideias foram observadas, comparadas e refinadas. As conclusoes mais importantes foram as seguintes:

- explorar o mapa com papeis diferentes foi melhor do que fazer todos os agentes se moverem do mesmo jeito;
- ficar parado ao encontrar uma boa fonte de energia foi, na maior parte dos casos, mais eficiente do que continuar procurando imediatamente;
- perseguir energia por alguns turnos foi melhor do que perseguir indefinidamente;
- combate agressivo nao trouxe bons resultados para o criterio de sobrevivencia;
- solucoes mais simples e mais estaveis performaram melhor do que estrategias excessivamente sofisticadas.

Em resumo, o comportamento final da equipe nao surgiu apenas por intuicao, mas sim pela observacao de quais escolhas aumentavam o tempo de vida nas execucoes feitas.

## Vantagens da Solucao

Consideramos que a estrategia escolhida possui algumas vantagens importantes:

- simplicidade de implementacao e de observacao;
- boa distribuicao inicial dos agentes;
- uso de comunicacao local para compartilhar oportunidades de energia;
- economia de energia ao permanecer parado em fontes vantajosas;
- prioridade clara para sobrevivencia em vez de confronto.

## Limitacoes

Apesar dos pontos positivos, a estrategia possui limitacoes:

- os agentes nao possuem memoria global do mapa;
- a comunicacao depende apenas do alcance local das mensagens;
- a logica de combate e simples e focada mais em evitar perdas do que em tomar decisoes sofisticadas;
- o espalhamento aleatorio do grupo central pode gerar trajetorias menos previsiveis.

Mesmo com essas limitacoes, a estrategia foi considerada adequada para o objetivo proposto, especialmente por equilibrar cobertura de mapa, simplicidade e economia de energia.

## Mapeamento da Estrategia no Codigo

Para deixar claro como cada etapa da estrategia aparece na implementacao, abaixo esta um resumo relacionando comportamento e trecho do agente:

- Divisao em tres grupos: acontece no construtor, quando o papel do agente e definido por `getId() % 3`.
- Configuracao inicial do movimento: acontece em `configuraDirecaoInicial()`, que decide se o agente vai para o centro, anda na vertical ou anda na horizontal.
- Ida ao centro: e implementada por `apontaParaOCentro()`, usada pelos agentes do grupo 0.
- Exploracao do mapa: fica concentrada em `pensa()`, que organiza o fluxo principal de decisao do agente a cada turno.
- Mudanca de direcao nas bordas: tambem aparece em `pensa()`, no trecho em que o agente verifica `podeMoverPara(getDirecao())`.
- Parada ao encontrar energia: acontece quando `recebeuEnergiaNesteTurno` e verdadeiro, fazendo o agente avisar a equipe e chamar `para()`.
- Comunicacao entre aliados: e feita por `enviaMensagem("ENERGIA:x:y")` e tratada em `recebeuMensagem(String msg)`.
- Perseguicao de energia avisada: e implementada por `moveParaAlvoEnergia()`, que move o agente por alguns turnos ate a posicao recebida.
- Reacao basica a combate: aparece em `tomouDano(int energiaRestanteInimigo)`, onde o agente abandona o estado de coleta e tenta mudar de direcao.

## Conclusao

A estrategia `TurboDog` foi desenvolvida com foco em sobrevivencia, simplicidade e aproveitamento eficiente dos cogumelos encontrados. A equipe optou por uma abordagem em que os agentes exploram a arena de maneira distribuida, compartilham informacoes por mensagens e evitam combates sempre que possivel.

Nosso entendimento final, baseado nos testes realizados, foi que sobreviver por mais tempo dependia mais de explorar bem, economizar energia e aproveitar boas oportunidades de coleta do que de procurar confronto direto com outros agentes.
