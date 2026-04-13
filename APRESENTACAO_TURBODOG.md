# Apresentacao TurboDog

## Slide 1 - Titulo

**TurboDog: estrategia de sobrevivencia na arena**

- Integrantes: Artur Malheiros e Bruno Rocha
- Objetivo: sobreviver o maior tempo possivel
- Ideia central: explorar bem o mapa, aproveitar energia e evitar gasto desnecessario

**Fala sugerida**

"Nosso agente foi pensado para sobreviver o maximo possivel. Em vez de focar em combate, a gente priorizou cobertura do mapa, coleta de energia e economia de movimento."

---

## Slide 2 - Como chegamos na estrategia

- No inicio pensamos: se todos os agentes fizerem a mesma coisa, eles se acumulam
- Testamos a ideia de distribuir funcoes para cobrir mais area
- Observamos nos testes que lutar demais piorava o tempo de vida
- Tambem vimos que, ao encontrar energia, muitas vezes era melhor ficar parado do que continuar procurando

**Fala sugerida**

"A estrategia final nao veio de uma ideia unica pronta. Ela foi aparecendo a partir dos testes. O que mais ajudou foi espalhar os agentes, reduzir gasto de energia e tratar combate como algo secundario."

---

## Slide 3 - Estrategia geral

- Dividimos o time em 3 grupos
- Grupo 0 vai para o centro e depois se espalha
- Grupo 1 faz exploracao vertical
- Grupo 2 faz exploracao horizontal
- Quando um agente encontra energia, ele avisa aliados proximos
- Quem recebe o aviso tenta seguir o alvo por alguns turnos

**Fala sugerida**

"A estrategia mistura cobertura inicial do mapa com comunicacao local. Assim, os agentes nao ficam todos sobrepostos e conseguem aproveitar melhor as oportunidades de energia."

---

## Slide 4 - Por que dividir em grupos

- Evita que todo mundo siga o mesmo caminho
- Aumenta a cobertura da arena logo no inicio
- O centro vira uma area de redistribuicao
- Os movimentos vertical e horizontal criam uma busca simples e organizada

**Fala sugerida**

"A divisao em grupos foi uma forma simples de organizar o time. O grupo do centro ajuda a ocupar uma regiao importante, enquanto os outros dois abrem busca em faixas diferentes do mapa."

---

## Slide 5 - Como o codigo define o papel do agente

Trechos para mostrar:

- `this.papel = getId() % 3;`
- `configuraDirecaoInicial()`

Explicacao:

- O `id` do agente e usado para separar os agentes em 3 papeis
- Cada papel recebe uma direcao inicial diferente
- Isso ja cria a distribuicao do time nos primeiros turnos

**Fala sugerida**

"Aqui e onde a estrategia comeca a aparecer no codigo. A linha `getId() % 3` separa o time em tres comportamentos. Depois disso, `configuraDirecaoInicial()` decide para onde cada grupo vai sair andando."

---

## Slide 6 - O metodo mais importante: `pensa()`

Fluxo principal do turno:

1. Se recebeu energia, avisa os aliados e para
2. Se acabou de coletar energia, continua parado
3. Se recebeu aviso de energia, tenta ir ate o alvo
4. Se nao houver aviso, segue a exploracao do seu grupo

**Fala sugerida**

"O metodo `pensa()` e o cerebro do agente. A cada turno ele decide qual prioridade vale mais: aproveitar energia, seguir um aviso ou continuar explorando."

---

## Slide 7 - Comportamento ao encontrar energia

Trechos para mostrar:

- `recebeuEnergia()`
- bloco inicial de `pensa()`
- `enviaMensagem("ENERGIA:" + getX() + ":" + getY())`

Explicacao:

- Quando recebe energia, o agente marca esse evento
- No turno seguinte, ele envia a coordenada para aliados proximos
- Depois chama `para()` para economizar energia e continuar coletando

**Fala sugerida**

"Essa foi uma decisao importante dos nossos testes. Em vez de sair andando logo depois de encontrar energia, o agente para e tenta aproveitar aquele ponto o maximo possivel."

---

## Slide 8 - Comunicacao entre aliados

Trechos para mostrar:

- `recebeuMensagem(String msg)`
- `moveParaAlvoEnergia()`

Explicacao:

- O protocolo de mensagem e simples: `ENERGIA:x:y`
- Quando um aliado recebe essa mensagem, ele guarda a coordenada
- Durante alguns turnos, ele tenta caminhar ate esse local

**Fala sugerida**

"Como nao usamos memoria global, a coordenacao foi feita por mensagens locais. Isso deixa o agente simples e dentro das restricoes do trabalho."

---

## Slide 9 - Exploracao do mapa

Trechos para mostrar:

- bloco `if (papel == 0 && !chegouNoCentro)`
- `apontaParaOCentro()`
- tratamento de borda com `podeMoverPara(getDirecao())`

Explicacao:

- Grupo 0 ocupa o centro e depois vira exploracao aleatoria
- Grupo 1 inverte entre cima e baixo
- Grupo 2 inverte entre esquerda e direita
- Quando bate na borda, o agente troca a direcao

**Fala sugerida**

"A exploracao foi feita para ser simples e facil de enxergar na simulacao. Cada grupo tem um padrao claro, o que tambem ajuda bastante na apresentacao."

---

## Slide 10 - Combate

Trecho para mostrar:

- `tomouDano(int energiaRestanteInimigo)`

Explicacao:

- Se o inimigo aparenta estar mais forte, o agente abandona o estado de coleta
- Ele tambem descarta o alvo de energia
- Em seguida troca para uma direcao aleatoria

**Fala sugerida**

"Nosso agente nao foi construido para ser agressivo. A ideia foi sobreviver, entao o combate entra mais como reacao do que como objetivo."

---

## Slide 11 - Ponto forte e limitacao

Pontos fortes:

- Estrategia simples e facil de explicar
- Boa distribuicao inicial do time
- Compartilhamento local de oportunidades de energia
- Economia de energia ao parar em pontos bons

Limitacao importante:

- Depois que `coletouRecentemente` vira `true`, o agente so volta ao comportamento normal se apanhar ou vencer combate
- Isso significa que, se a energia acabar ou sair dali, ele pode ficar parado tempo demais

**Fala sugerida**

"Um ponto forte do TurboDog e a simplicidade. Um ponto fraco e que o agente pode ficar parado por mais tempo do que deveria depois de encontrar energia. Isso e uma limitacao real da versao final."

---

## Slide 12 - Conclusao

- Nosso agente foi guiado por observacao de testes
- A estrategia final prioriza sobrevivencia, nao agressividade
- O codigo segue exatamente essa ideia: distribuir, explorar, compartilhar energia e economizar movimento

**Fala sugerida**

"Em resumo, o TurboDog foi construido para sobreviver bem com uma logica clara. A estrategia aparece direto no codigo: distribuicao em grupos, comunicacao por mensagem, parada em pontos de energia e reacao defensiva em combate."
