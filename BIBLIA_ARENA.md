# Biblia Competitiva do Arena

Este documento é nosso manual de jogo limpo para dominar o Arena com programacao boa, estrategia forte e sem explorar brechas obscuras.

## 1. Filosofia

Nosso objetivo é ganhar de forma competitiva, mas justa.

Principios:
- Respeitar a API publica de `Agente` e as regras do simulador.
- Nao depender de bug, comparacao estranha, estado interno escondido ou comportamento acidental.
- Construir vantagem com leitura de codigo, matematica, coordenacao, testes e engenharia.
- Se uma tecnica parecer "hack" de motor e nao inteligencia de agente, a gente nao usa.

## 2. Modelo Mental do Jogo

O loop principal esta em `Arena.run()`. Em cada ciclo, a arena atualiza entidades, processa teclado, coleta estatisticas e renderiza.

Cada agente vive dentro de `Agente.update()`, que faz esta sequencia:
1. Chama `pensa()`.
2. Se `divide()` foi solicitado e houver energia suficiente, executa divisao.
3. Se nao estiver parado, move uma unidade de velocidade na direcao atual.
4. Cobra energia de movimento e de sobrevivencia.
5. Processa combate se houver inimigo na mesma posicao.

Os pontos de energia em `PontoEnergia.update()`:
- Regeneram a propria energia conforme constantes.
- Transferem energia para agentes dentro da area de alcance.
- Podem se mover, se `PONTO_ENERGIA_MOVEL` estiver ativo.

A partida acaba quando nao existe mais nenhum `Agente` vivo ou nascendo.

## 3. Regras Praticas de Energia

Constantes relevantes em `Constants.java`:
- Energia inicial do agente: `1000`
- Custo por andar: `2`
- Custo por viver a cada turno: `1`
- Custo para dividir: `20`
- Dano de combate por pancada: `5`
- Recompensa por eliminar inimigo: `200`
- Energia entregue por ponto de energia por turno: `5`

Leituras estrategicas:
- Um agente andando custa `3` por turno no total.
- Um agente parado custa `1` por turno.
- Ficar em alcance de um ponto de energia tende a ser lucro liquido, porque recebe `5`.
- Dividir so faz sentido se a nova unidade gerar vantagem territorial, controle de fonte de energia ou pressao de combate.
- Brigar sem vantagem numerica ou energetica pode ser ruim, porque ambos perdem energia por turno de combate.

Regra de ouro:
- Energia nao é so vida, e tambem capital de expansao. Dividir cedo demais pode criar um enxame fraco; dividir tarde demais pode perder mapa.

## 4. Capacidades do Agente

A classe `Agente` oferece estas decisoes honestas:
- `setDirecao(...)` para andar.
- `para()` para reduzir custo energetico.
- `podeMoverPara(...)` para nao bater na borda.
- `podeDividir()` e `divide()` para expandir populacao.
- `enviaMensagem(...)` para coordenar aliados proximos.
- Callbacks de evento: `recebeuEnergia()`, `tomouDano(...)`, `ganhouCombate()`, `recebeuMensagem(...)`.

O que o bot nao deve tentar fazer:
- Alterar `x`/`y` diretamente.
- Burlar protecao de informacao da arena.
- Usar reflexao ou acesso indevido para enxergar estado global.

## 5. Estrategias Competitivas Limpas

### 5.1 Abertura

Objetivo da abertura:
- Espalhar para descobrir fontes de energia.
- Evitar colisao prematura sem informacao.
- Preservar energia enquanto o mapa ainda e desconhecido.

Politica sugerida:
- Comecar com exploracao controlada.
- Trocar direcao ao tocar borda.
- Parar temporariamente quando estiver farmando energia com seguranca.
- Dividir apenas quando houver energia sobrando e um motivo tatico claro.

### 5.2 Controle de Energia

Fonte de energia e territorio.

Boas ideias:
- Se `recebeuEnergia()` disparou, registrar que aquela regiao e valiosa.
- Reduzir movimento enquanto estiver dentro da area de um ponto de energia, para maximizar saldo.
- Se houver comunicacao de time, avisar aliados que uma zona tem recurso.
- Manter alguns agentes como "guarda/fazenda" em regioes boas e outros como "scouts".

### 5.3 Combate

Combate acontece quando dois agentes inimigos ocupam exatamente a mesma posicao.

Boas ideias:
- Usar `tomouDano(energiaRestanteInimigo)` para estimar se vale insistir ou tentar reposicionar.
- Se ganhou combate, aproveitar bonus de energia para avaliar divisao ou avanco.
- Evitar comportamento suicida de ficar preso em briga ruim sem criterio.

Importante:
- Como nao ha sensor global na API publica, combate bom depende mais de posicionamento, controle de energia e coordenacao local do que de onisciencia.

### 5.4 Coordenacao por Mensagens

Mensagens so chegam a aliados dentro de `AGENTE_ALCANCE_MENSAGEM`.

Usos honestos e fortes:
- Avisar "achei energia".
- Avisar "tem inimigo aqui".
- Coordenar papel local: "vou farmar", "vou patrulhar", "zona ocupada".

Cuidados:
- Mensagem nao deve virar spam cego todo turno.
- Definir protocolo simples, parseavel e robusto.
- Tolerar perda de contexto, porque nem todo aliado estara no alcance.

### 5.5 Divisao

Dividir cria uma nova entidade da mesma classe, no mesmo `x/y`, com energia dividida apos pagar custo.

Boas regras:
- So dividir acima de um limiar de seguranca.
- Preferir dividir perto de fonte de energia ou apos vitoria em combate.
- Atribuir roles diferentes apos divisao usando estado interno e pequenas variacoes de direcao.
- Evitar cascata de divisoes sem controle.

## 6. Arquitetura Recomendada para um Agente Forte

Estrutura sugerida:
- Um pequeno "estado interno" no agente.
- Uma funcao de decisao por prioridade dentro de `pensa()`.
- Memoria local de eventos recentes.
- Protocolo de mensagens compacto.
- Parametros numericos centralizados para facilitar tuning.

Exemplo de prioridades em `pensa()`:
1. Se estou em risco energetico, priorizar sobrevivencia.
2. Se estou farmando energia com saldo positivo, reduzir movimento e avaliar divisao.
3. Se recebi sinal recente de inimigo, ajustar patrulha/combate.
4. Se nao ha informacao util, explorar sem oscilar em borda.

Estados uteis:
- `EXPLORANDO`
- `FARMANDO`
- `PATRULHANDO`
- `EM_ALERTA`
- `REAGRUPANDO`

## 7. O Que Podemos Aprender de Robocode e Battlecode

Referencias:
- Robocode/Tank Royale: https://robocode.dev/articles/intro.html
- Battlecode: https://battlecode.org/

Ideias que fazem sentido trazer:
- Separar movimento, combate e comunicacao em modulos.
- Usar roles de time, em vez de todo mundo fazer a mesma coisa sempre.
- Evitar oscilacao de movimento perto de bordas ou pontos de interesse.
- Fazer scouting e depois converter informacao em pressao territorial.
- Medir desempenho por simulacoes repetidas, nao por intuicao isolada.

Ideias que nao vamos copiar de forma errada:
- Nao usar truques dependentes de detalhe interno do motor.
- Nao tentar explorar bug de comparacao, timing ou render.
- Nao fazer comportamento que so funciona por acidente em vez de estrategia.

## 8. Checklist de Fair Play

Antes de aceitar uma tecnica, perguntar:
- Isso usa apenas a API normal do agente?
- Isso continuaria parecendo justo se outro competidor lesse nosso codigo?
- Isso melhora inteligencia/estrategia ou so explora uma falha do framework?
- Se o professor/organizador corrigisse uma bugfix obvia, nossa estrategia continuaria de pe?

Se a resposta for duvidosa, a gente corta.

## 9. Plano para Construir um Agente Campeao

Fase 1:
- Implementar um agente base com exploracao, anti-borda, memoria simples e divisao conservadora.

Fase 2:
- Adicionar deteccao de farm via `recebeuEnergia()` e transicao de estado para ocupar recurso.

Fase 3:
- Adicionar protocolo de mensagens entre aliados proximos.

Fase 4:
- Ajustar regras de combate e divisao com base em logs e estatisticas.

Fase 5:
- Rodar baterias de simulacao contra `AgenteDummy` e `AgenteInimigo`, medir consistencia e refinar parametros.

## 10. Primeira Hipotese de Bot Forte

Um desenho promissor e um agente "explorador-colonizador":
- Comeca explorando com direcao persistente e troca controlada em borda.
- Quando acha energia, entra em modo `FARMANDO`, para ou reduz deslocamento e avisa aliados proximos.
- Se a energia sobe acima de um limiar seguro, divide e manda o clone sair em outra direcao.
- Se entrar em combate e o inimigo parecer fraco, sustenta a posicao; se nao, tenta reposicionar apos um numero pequeno de eventos de dano.
- Mantem mensagens simples e cooldown para nao spammar.

Isso e limpo, competitivo e ja explora bem o que o motor realmente oferece.
