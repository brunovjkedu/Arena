package br.uffs.cc.jarena;

/**
 * Agente TurboDog.
 *
 * Estrategia didatica e facil de observar:
 * - explora o mapa com 3 grupos: grupo 0 vai ao centro e depois se espalha; grupo 1 faz varredura vertical; grupo 2 faz varredura horizontal;
 * - troca de direcao quando encontra uma borda;
 * - se recebeu energia neste turno, fica parado e avisa aliados proximos com sua posicao;
 * - se um aliado avisar "ENERGIA:x:y", tenta se mover ate essa posicao por poucos turnos;
 * 
 * INTEGRANTES: Artur Malheiros(artur.quadros2006@gmail.com) e Bruno Rocha(brunovjk@gmail.com)
 */

public class AgenteTurboDog extends Agente {
    // Nome fixo da equipe; é assim que a arena distingue aliados de inimigos.
    private final String equipe;
    // Quantos turnos seguimos um aviso de energia antes de desistir.
    private final int turnosParaSeguirAviso;
    // Margem usada para considerar que já chegamos perto o suficiente do alvo.
    private final int distanciaMinimaDoAlvo;

    // Flags de estado usadas para a estratégia reagir a eventos do turno.
    private boolean recebeuEnergiaNesteTurno;
    private int turnosSeguindoAvisoEnergia;
    private int alvoEnergiaX;
    private int alvoEnergiaY;
    private boolean temAlvoEnergia;
    
    // "papel" define a função inicial do agente dentro do time.
    private int papel; 
    // Marca que o agente acabou de coletar energia e deve esperar um pouco.
    private boolean coletouRecentemente; 
    // O grupo 0 vai para o centro antes de se espalhar; esta flag registra isso.
    private boolean chegouNoCentro;

    public AgenteTurboDog(Integer x, Integer y, Integer energia) {
        super(x, y, energia);
        // Configuração fixa da equipe e dos limites da estratégia.
        this.equipe = "TurboDog";
        this.turnosParaSeguirAviso = 12;
        this.distanciaMinimaDoAlvo = Constants.ENTIDADE_VELOCIDADE;
        
        // Estado inicial: ainda não recebeu energia nem tem alvo compartilhado.
        this.recebeuEnergiaNesteTurno = false;
        this.turnosSeguindoAvisoEnergia = 0;
        this.temAlvoEnergia = false;
        this.coletouRecentemente = false;
        this.chegouNoCentro = false; 

        // Distribuímos os agentes em três grupos reaproveitando o id único da entidade.
        this.papel = getId() % 3;
        configuraDirecaoInicial();
    }

    private void configuraDirecaoInicial() {
        // Cada grupo nasce com uma missão de abertura diferente para cobrir melhor o mapa.
        if (papel == 1) {
            setDirecao(getY() < Constants.ALTURA_TELA / 2 ? BAIXO : CIMA);
        } else if (papel == 2) {
            setDirecao(getX() < Constants.LARGURA_TELA / 2 ? DIREITA : ESQUERDA);
        } else {
            apontaParaOCentro();
        }
    }

    public void pensa() {
        // Se recebeu energia neste turno, transforma isso em um "evento":
        // avisa aliados, para de andar e poupa energia.
        if (recebeuEnergiaNesteTurno) {
            recebeuEnergiaNesteTurno = false;
            coletouRecentemente = true;
            enviaMensagem("ENERGIA:" + getX() + ":" + getY());
            para();
            return;
        }

        // Depois de encontrar energia, o agente prefere ficar parado por segurança.
        // Isso mantém o agente dentro da área de recarga e evita gasto desnecessário.
        if (coletouRecentemente) {
            para();
            return;
        }

        // Se algum aliado avisou onde há energia, priorizamos seguir esse alvo.
        if (temAlvoEnergia && turnosSeguindoAvisoEnergia > 0) {
            turnosSeguindoAvisoEnergia--;
            moveParaAlvoEnergia();
            return;
        }

        // Se o aviso venceu, voltamos ao modo normal de exploração.
        temAlvoEnergia = false;

        // A exploração base é dividida em três grupos com padrões diferentes.
        if (papel == 0 && !chegouNoCentro) {
            int centroX = Constants.LARGURA_TELA / 2;
            int centroY = Constants.ALTURA_TELA / 2;

            // Grupo 0 avança para o centro para ocupar uma região estratégica do mapa.
            if (Math.abs(getX() - centroX) > 60 || Math.abs(getY() - centroY) > 60) {
                apontaParaOCentro();
            } else {
                // Quando o centro é alcançado, o agente passa a explorar de forma livre.
                chegouNoCentro = true; 
                setDirecao(geraDirecaoAleatoria());
            }
        } else {

            if (!podeMoverPara(getDirecao())) {
                if (papel == 1 && !chegouNoCentro) {
                    // Grupo 1 faz uma varredura vertical, invertendo na borda.
                    setDirecao(getDirecao() == BAIXO ? CIMA : BAIXO);
                } else if (papel == 2 && !chegouNoCentro) {
                    // Grupo 2 faz uma varredura horizontal, também invertendo na borda.
                    setDirecao(getDirecao() == DIREITA ? ESQUERDA : DIREITA);
                } else {
                    // Depois da fase inicial, usamos espalhamento aleatório para cobrir lacunas.
                    setDirecao(geraDirecaoAleatoria());
                }
            }
        }
    }

    private void apontaParaOCentro() {
        int centroX = Constants.LARGURA_TELA / 2;
        int centroY = Constants.ALTURA_TELA / 2;
        int difX = centroX - getX();
        int difY = centroY - getY();

        // Escolhemos o eixo com maior distância para aproximar do centro mais rápido.
        if (Math.abs(difX) > Math.abs(difY)) {
            setDirecao(difX > 0 ? DIREITA : ESQUERDA);
        } else {
            setDirecao(difY > 0 ? BAIXO : CIMA);
        }
    }

    private void moveParaAlvoEnergia() {
        int difX = alvoEnergiaX - getX();
        int difY = alvoEnergiaY - getY();

        // Se já estamos suficientemente perto do ponto avisado, paramos e tentamos coletar.
        if (Math.abs(difX) <= distanciaMinimaDoAlvo && Math.abs(difY) <= distanciaMinimaDoAlvo) {
            para();
            return;
        }

        // Assim como na ida ao centro, priorizamos o eixo com maior diferença.
        if (Math.abs(difX) >= Math.abs(difY)) {
            setDirecao(difX > 0 ? DIREITA : ESQUERDA);
        } else {
            setDirecao(difY > 0 ? BAIXO : CIMA);
        }
    }

    public void recebeuEnergia() {
        // O callback não faz tudo na hora; ele só marca o evento para o pensa() do turno.
        recebeuEnergiaNesteTurno = true;
    }

    public void tomouDano(int energiaRestanteInimigo) {
        // Se o inimigo ainda está mais forte, abandonamos a postura passiva
        // e tentamos escapar mudando de direção.
        if (energiaRestanteInimigo > getEnergia()) {
            coletouRecentemente = false;
            temAlvoEnergia = false;
            setDirecao(geraDirecaoAleatoria());
        }
    }

    public void ganhouCombate() {
        // Ao vencer, o agente volta a se movimentar normalmente.
        coletouRecentemente = false;
    }

    public void recebeuMensagem(String msg) {
        // O protocolo de comunicação do time é simples: "ENERGIA:x:y".
        if (msg != null && msg.startsWith("ENERGIA:")) {
            String[] partes = msg.split(":");
            if (partes.length == 3) {
                try {
                    // Se a mensagem estiver válida, guardamos o alvo e seguimos até expirar.
                    alvoEnergiaX = Integer.parseInt(partes[1]);
                    alvoEnergiaY = Integer.parseInt(partes[2]);
                    temAlvoEnergia = true;
                    turnosSeguindoAvisoEnergia = turnosParaSeguirAviso;
                } catch (Exception e) {}
            }
        }
    }

    public String getEquipe() {
        return equipe;
    }
}
