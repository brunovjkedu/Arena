package br.uffs.cc.jarena;

/**
 * Agente TurboDog.
 *
 * Estrategia didatica e facil de observar:
 * - explora o mapa com 3 grupos grupo 0 para o centro e depis se separa por posições aleatórias, grupo 1 anda verticalmente e 2 horizantalmente;
 * - troca de direcao quando encontra uma borda;
 * - se recebeu energia neste turno, fica parado e avisa aliados proximos com sua posicao;
 * - se um aliado avisar "ENERGIA:x:y", tenta se mover ate essa posicao por poucos turnos;
 * 
 * INTEGRANTES: Artur Malheiros(artur.quadros2006@gmail.com) e Bruno Rocha(brunovjk@gmail.com)
 */

public class AgenteTurboDog extends Agente {
    private final String equipe;
    private final int turnosParaSeguirAviso;
    private final int distanciaMinimaDoAlvo;

    private boolean recebeuEnergiaNesteTurno;
    private int turnosSeguindoAvisoEnergia;
    private int alvoEnergiaX;
    private int alvoEnergiaY;
    private boolean temAlvoEnergia;
    
    private int papel; 
    private boolean coletouRecentemente; 
    private boolean chegouNoCentro; //para repartir grupo 0 quando chegar no centro

    public AgenteTurboDog(Integer x, Integer y, Integer energia) {
        super(x, y, energia);
        this.equipe = "TurboDog";
        this.turnosParaSeguirAviso = 12;
        this.distanciaMinimaDoAlvo = Constants.ENTIDADE_VELOCIDADE;
        
        this.recebeuEnergiaNesteTurno = false;
        this.turnosSeguindoAvisoEnergia = 0;
        this.temAlvoEnergia = false;
        this.coletouRecentemente = false;
        this.chegouNoCentro = false; 

        this.papel = getId() % 3;
        configuraDirecaoInicial();
    }

    private void configuraDirecaoInicial() {
        if (papel == 1) {
            setDirecao(getY() < Constants.ALTURA_TELA / 2 ? BAIXO : CIMA);
        } else if (papel == 2) {
            setDirecao(getX() < Constants.LARGURA_TELA / 2 ? DIREITA : ESQUERDA);
        } else {
            apontaParaOCentro();
        }
    }

    public void pensa() {
        if (recebeuEnergiaNesteTurno) {
            recebeuEnergiaNesteTurno = false;
            coletouRecentemente = true;
            enviaMensagem("ENERGIA:" + getX() + ":" + getY());
            para();
            return;
        }

        //fica parado depois que coletar um cogumelo, assim pega energia e economiza ela
        if (coletouRecentemente) {
            para();
            return;
        }

        if (temAlvoEnergia && turnosSeguindoAvisoEnergia > 0) {
            turnosSeguindoAvisoEnergia--;
            moveParaAlvoEnergia();
            return;
        }

        temAlvoEnergia = false;

        //lógica de movimentação dividida em 3 grupos com movimentações diferentes
        if (papel == 0 && !chegouNoCentro) {
            int centroX = Constants.LARGURA_TELA / 2;
            int centroY = Constants.ALTURA_TELA / 2;

            //se ainda está longe do meio, continua indo pra lá
            if (Math.abs(getX() - centroX) > 60 || Math.abs(getY() - centroY) > 60) {
                apontaParaOCentro();
            } else {
                //quando chega no meio se espalha para explorar
                chegouNoCentro = true; 
                setDirecao(geraDirecaoAleatoria());
            }
        } else {

            if (!podeMoverPara(getDirecao())) {
                if (papel == 1 && !chegouNoCentro) {
                    setDirecao(getDirecao() == BAIXO ? CIMA : BAIXO);
                } else if (papel == 2 && !chegouNoCentro) {
                    setDirecao(getDirecao() == DIREITA ? ESQUERDA : DIREITA);
                } else {
                    //espalhamento aleatório
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

        if (Math.abs(difX) > Math.abs(difY)) {
            setDirecao(difX > 0 ? DIREITA : ESQUERDA);
        } else {
            setDirecao(difY > 0 ? BAIXO : CIMA);
        }
    }

    private void moveParaAlvoEnergia() {
        int difX = alvoEnergiaX - getX();
        int difY = alvoEnergiaY - getY();

        if (Math.abs(difX) <= distanciaMinimaDoAlvo && Math.abs(difY) <= distanciaMinimaDoAlvo) {
            para();
            return;
        }

        if (Math.abs(difX) >= Math.abs(difY)) {
            setDirecao(difX > 0 ? DIREITA : ESQUERDA);
        } else {
            setDirecao(difY > 0 ? BAIXO : CIMA);
        }
    }

    public void recebeuEnergia() {
        recebeuEnergiaNesteTurno = true;
    }

    public void tomouDano(int energiaRestanteInimigo) {
        if (energiaRestanteInimigo > getEnergia()) {
            coletouRecentemente = false;
            temAlvoEnergia = false;
            setDirecao(geraDirecaoAleatoria());
        }
    }

    public void ganhouCombate() {
        coletouRecentemente = false;
    }

    public void recebeuMensagem(String msg) {
        if (msg != null && msg.startsWith("ENERGIA:")) {
            String[] partes = msg.split(":");
            if (partes.length == 3) {
                try {
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