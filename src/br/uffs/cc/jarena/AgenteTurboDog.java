package br.uffs.cc.jarena;

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
    private boolean chegouNoCentro; // Trava para mudar o comportamento do Grupo 0

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

        // Se coletou, fica parado para economizar (conforme sua regra anterior)
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

        // LÓGICA DE MOVIMENTAÇÃO (FOCO NO DESLOCAMENTO)
        if (papel == 0 && !chegouNoCentro) {
            int centroX = Constants.LARGURA_TELA / 2;
            int centroY = Constants.ALTURA_TELA / 2;

            // Se ainda está longe do meio, continua indo pra lá
            if (Math.abs(getX() - centroX) > 60 || Math.abs(getY() - centroY) > 60) {
                apontaParaOCentro();
            } else {
                // Chegou no meio! Libera para explorar o resto do mapa
                chegouNoCentro = true; 
                setDirecao(geraDirecaoAleatoria());
            }
        } else {
            // Se bater em algo ou for Grupo 1/2/Grupo 0 que já passou pelo centro
            if (!podeMoverPara(getDirecao())) {
                if (papel == 1 && !chegouNoCentro) {
                    setDirecao(getDirecao() == BAIXO ? CIMA : BAIXO);
                } else if (papel == 2 && !chegouNoCentro) {
                    setDirecao(getDirecao() == DIREITA ? ESQUERDA : DIREITA);
                } else {
                    // Espalhamento aleatório após cumprir o objetivo ou bater
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