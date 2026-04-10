package br.uffs.cc.jarena;

public class MeuAgenteCentroAgressivo extends Agente {

    private int energiaBaixa;
    private int energiaMinimaParaDividir;
    private int turnosMinimosNoCogumeloParaDividir;
    private int energiaMinimaParaLutar;
    private int vantagemEnergiaParaLutar;
    private int energiaParaPressionarCentro;
    private int intervaloBroadcastCogumelo;
    private int intervaloBroadcastPerigo;
    private int alcanceMemoriaCogumelo;
    private int alcanceMemoriaPerigo;
    private int janelaBuscaLocal;
    private int periodoMudancaPressao;
    private int turnosPersistindoNoCentro;
    private int turnosMaximosPerseguindoCogumelo;
    private int energiaMinimaParaSegurarCogumelo;

    private boolean recebeuEnergiaNoTurno;
    private boolean tomouDanoNoTurno;
    private boolean ganhouCombateNoTurno;

    private boolean conheceCogumelo;
    private boolean conhecePerigo;

    private int energiaInimigo;
    private int turnosVivo;
    private int turnosNoCogumelo;
    private int turnosSemEnergia;
    private int turnosDesdeMensagemCogumelo;
    private int turnosDesdeMensagemPerigo;
    private int idadeInfoCogumelo;
    private int idadeInfoPerigo;
    private int ciclosBuscaLocal;

    private int cogumeloX;
    private int cogumeloY;
    private int perigoX;
    private int perigoY;
    private int spawnX;
    private int spawnY;
    private int papel;

    public MeuAgenteCentroAgressivo(Integer x, Integer y, Integer energia) {
        super(x, y, energia);

        spawnX = x;
        spawnY = y;
        papel = getId() % 5;

        configurarParametros();
        resetarEstado();

        setDirecao(direcaoInicial());
    }

    private void configurarParametros() {
        energiaBaixa = 150;
        energiaMinimaParaDividir = 790;
        turnosMinimosNoCogumeloParaDividir = 2;
        energiaMinimaParaLutar = 260;
        vantagemEnergiaParaLutar = 70;
        energiaParaPressionarCentro = 520;
        intervaloBroadcastCogumelo = 7;
        intervaloBroadcastPerigo = 2;
        alcanceMemoriaCogumelo = 34;
        alcanceMemoriaPerigo = 6;
        janelaBuscaLocal = 12;
        periodoMudancaPressao = 10;
        turnosPersistindoNoCentro = 32;
        turnosMaximosPerseguindoCogumelo = 14;
        energiaMinimaParaSegurarCogumelo = 250;
    }

    private void resetarEstado() {
        recebeuEnergiaNoTurno = false;
        tomouDanoNoTurno = false;
        ganhouCombateNoTurno = false;
        conheceCogumelo = false;
        conhecePerigo = false;
        energiaInimigo = -1;
        turnosVivo = 0;
        turnosNoCogumelo = 0;
        turnosSemEnergia = 0;
        turnosDesdeMensagemCogumelo = 9999;
        turnosDesdeMensagemPerigo = 9999;
        idadeInfoCogumelo = 9999;
        idadeInfoPerigo = 9999;
        ciclosBuscaLocal = 0;
        cogumeloX = -1;
        cogumeloY = -1;
        perigoX = -1;
        perigoY = -1;
    }

    @Override
    public void pensa() {
        prepararTurno();

        if (tomouDanoNoTurno) {
            avisarPerigoSeNecessario();
        }

        if (recebeuEnergiaNoTurno) {
            avisarCogumeloSeNecessario();

            if (tomouDanoNoTurno) {
                if (deveLutarAgora() || getEnergia() >= energiaMinimaParaSegurarCogumelo) {
                    para();
                } else {
                    recuar();
                }

                finalizarTurno();
                return;
            }

            if (deveDividirAgora()) {
                divide();
            } else if (getEnergia() >= energiaMinimaParaSegurarCogumelo) {
                para();
            } else {
                buscarLocalmenteAoRedorDoCogumelo();
            }

            finalizarTurno();
            return;
        }

        if (tomouDanoNoTurno) {
            if (deveLutarAgora()) {
                para();
            } else {
                recuar();
            }

            finalizarTurno();
            return;
        }

        if (ganhouCombateNoTurno && podeDividir() && getEnergia() >= energiaMinimaParaDividir) {
            divide();
            finalizarTurno();
            return;
        }

        if (getEnergia() <= energiaBaixa) {
            if (cogumeloAindaVale()) {
                moverNaDirecaoDoAlvo(cogumeloX, cogumeloY);
            } else {
                recuar();
            }

            finalizarTurno();
            return;
        }

        if (cogumeloAindaVale() && turnosSemEnergia <= turnosMaximosPerseguindoCogumelo) {
            moverNaDirecaoDoAlvo(cogumeloX, cogumeloY);
            finalizarTurno();
            return;
        }

        pressionarMapa();
        finalizarTurno();
    }

    private void prepararTurno() {
        turnosVivo++;
        turnosDesdeMensagemCogumelo++;
        turnosDesdeMensagemPerigo++;
        idadeInfoCogumelo++;
        idadeInfoPerigo++;

        if (recebeuEnergiaNoTurno) {
            turnosSemEnergia = 0;
            turnosNoCogumelo++;
            ciclosBuscaLocal = 0;
        } else {
            turnosSemEnergia++;
            turnosNoCogumelo = 0;
        }

        if (idadeInfoCogumelo > alcanceMemoriaCogumelo) {
            conheceCogumelo = false;
        }
        if (idadeInfoPerigo > alcanceMemoriaPerigo) {
            conhecePerigo = false;
        }
    }

    private void finalizarTurno() {
        recebeuEnergiaNoTurno = false;
        tomouDanoNoTurno = false;
        ganhouCombateNoTurno = false;
        energiaInimigo = -1;
    }

    private boolean deveDividirAgora() {
        return podeDividir()
                && !tomouDanoNoTurno
                && getEnergia() >= energiaMinimaParaDividir
                && (turnosNoCogumelo >= turnosMinimosNoCogumeloParaDividir || ganhouCombateNoTurno);
    }

    private boolean deveLutarAgora() {
        if (energiaInimigo < 0) {
            return false;
        }

        return getEnergia() >= energiaMinimaParaLutar
                && (getEnergia() - energiaInimigo) >= vantagemEnergiaParaLutar;
    }

    private boolean cogumeloAindaVale() {
        return conheceCogumelo && idadeInfoCogumelo <= alcanceMemoriaCogumelo;
    }

    private void registrarCogumeloAtual() {
        cogumeloX = getX();
        cogumeloY = getY();
        conheceCogumelo = true;
        idadeInfoCogumelo = 0;
        ciclosBuscaLocal = 0;
    }

    private void registrarPerigoAtual() {
        perigoX = getX();
        perigoY = getY();
        conhecePerigo = true;
        idadeInfoPerigo = 0;
    }

    private void avisarCogumeloSeNecessario() {
        if (turnosDesdeMensagemCogumelo >= intervaloBroadcastCogumelo || turnosNoCogumelo <= 1) {
            enviaMensagem("M;" + cogumeloX + ";" + cogumeloY + ";" + turnosVivo);
            turnosDesdeMensagemCogumelo = 0;
        }
    }

    private void avisarPerigoSeNecessario() {
        if (turnosDesdeMensagemPerigo >= intervaloBroadcastPerigo) {
            enviaMensagem("E;" + perigoX + ";" + perigoY + ";" + energiaInimigo + ";" + turnosVivo);
            turnosDesdeMensagemPerigo = 0;
        }
    }

    private int direcaoInicial() {
        boolean nasceuNoCentro = Math.abs(spawnX - (Constants.LARGURA_MAPA / 2)) < 120;

        if (nasceuNoCentro) {
            if (papel == 0) {
                return ESQUERDA;
            }
            if (papel == 1) {
                return DIREITA;
            }
            if (papel == 2) {
                return CIMA;
            }
            return BAIXO;
        }

        return spawnX < Constants.LARGURA_MAPA / 2 ? DIREITA : ESQUERDA;
    }

    private void pressionarMapa() {
        int centroX = Constants.LARGURA_MAPA / 2;
        int centroY = Constants.ALTURA_MAPA / 2;
        boolean nasceuNoCentro = Math.abs(spawnX - centroX) < 120;

        if (!nasceuNoCentro && getEnergia() >= energiaParaPressionarCentro && turnosVivo <= turnosPersistindoNoCentro) {
            moverNaDirecaoDoAlvo(centroX, centroY);
            return;
        }

        if (conhecePerigo && getEnergia() >= energiaMinimaParaLutar) {
            moverNaDirecaoDoAlvo(perigoX, perigoY);
            return;
        }

        if (papel == 0) {
            rondaCentroHorizontal();
        } else if (papel == 1) {
            rondaCentroVertical();
        } else if (papel == 2) {
            rondaDiagonal();
        } else if (papel == 3) {
            avancarEmArcos();
        } else {
            patrulhaAdaptativa();
        }
    }

    private void rondaCentroHorizontal() {
        int centroY = Constants.ALTURA_MAPA / 2;

        if (Math.abs(getY() - centroY) > 50) {
            tentarMover(getY() < centroY ? BAIXO : CIMA);
            return;
        }

        if (!tentarMover(spawnX < Constants.LARGURA_MAPA / 2 ? DIREITA : ESQUERDA)) {
            tentarMover(geraDirecaoAleatoria());
        }
    }

    private void rondaCentroVertical() {
        int centroX = Constants.LARGURA_MAPA / 2;

        if (Math.abs(getX() - centroX) > 70) {
            tentarMover(getX() < centroX ? DIREITA : ESQUERDA);
            return;
        }

        if (!tentarMover(spawnY < Constants.ALTURA_MAPA / 2 ? BAIXO : CIMA)) {
            tentarMover(geraDirecaoAleatoria());
        }
    }

    private void rondaDiagonal() {
        int fase = (turnosVivo / Math.max(1, periodoMudancaPressao / 2)) % 2;
        int horizontal = spawnX < Constants.LARGURA_MAPA / 2 ? DIREITA : ESQUERDA;
        int vertical = spawnY < Constants.ALTURA_MAPA / 2 ? BAIXO : CIMA;

        if (fase == 0) {
            if (!tentarMover(horizontal)) {
                tentarMover(vertical);
            }
        } else if (!tentarMover(vertical)) {
            tentarMover(horizontal);
        }
    }

    private void avancarEmArcos() {
        int fase = (turnosVivo / periodoMudancaPressao + papel) % 4;

        if (fase == 0) {
            tentarMover(DIREITA);
        } else if (fase == 1) {
            tentarMover(BAIXO);
        } else if (fase == 2) {
            tentarMover(ESQUERDA);
        } else {
            tentarMover(CIMA);
        }
    }

    private void patrulhaAdaptativa() {
        if (turnosVivo % periodoMudancaPressao == 0) {
            tentarMover(geraDirecaoAleatoria());
        } else if (!tentarMover(getDirecao())) {
            tentarMover(geraDirecaoAleatoria());
        }
    }

    private void buscarLocalmenteAoRedorDoCogumelo() {
        if (!cogumeloAindaVale()) {
            patrulhaAdaptativa();
            return;
        }

        if (Math.abs(cogumeloX - getX()) > Constants.ENTIDADE_VELOCIDADE
                || Math.abs(cogumeloY - getY()) > Constants.ENTIDADE_VELOCIDADE) {
            moverNaDirecaoDoAlvo(cogumeloX, cogumeloY);
            return;
        }

        int fase = (ciclosBuscaLocal / 2 + papel) % 4;
        ciclosBuscaLocal++;

        if (fase == 0) {
            tentarMover(DIREITA);
        } else if (fase == 1) {
            tentarMover(BAIXO);
        } else if (fase == 2) {
            tentarMover(ESQUERDA);
        } else {
            tentarMover(CIMA);
        }

        if (ciclosBuscaLocal > janelaBuscaLocal) {
            patrulhaAdaptativa();
        }
    }

    private void moverNaDirecaoDoAlvo(int alvoX, int alvoY) {
        int dx = alvoX - getX();
        int dy = alvoY - getY();

        if (Math.abs(dx) >= Math.abs(dy)) {
            if (!tentarMover(dx >= 0 ? DIREITA : ESQUERDA)) {
                tentarMover(dy >= 0 ? BAIXO : CIMA);
            }
        } else if (!tentarMover(dy >= 0 ? BAIXO : CIMA)) {
            tentarMover(dx >= 0 ? DIREITA : ESQUERDA);
        }
    }

    private void recuar() {
        if (conhecePerigo) {
            int dx = getX() - perigoX;
            int dy = getY() - perigoY;

            if (Math.abs(dx) >= Math.abs(dy)) {
                if (!tentarMover(dx >= 0 ? DIREITA : ESQUERDA)) {
                    tentarMover(dy >= 0 ? BAIXO : CIMA);
                }
            } else if (!tentarMover(dy >= 0 ? BAIXO : CIMA)) {
                tentarMover(dx >= 0 ? DIREITA : ESQUERDA);
            }
            return;
        }

        if (!tentarMover(spawnX < Constants.LARGURA_MAPA / 2 ? ESQUERDA : DIREITA)) {
            tentarMover(spawnY < Constants.ALTURA_MAPA / 2 ? CIMA : BAIXO);
        }
    }

    private boolean tentarMover(int dir) {
        if (podeMoverPara(dir)) {
            setDirecao(dir);
            return true;
        }

        if (dir == DIREITA || dir == ESQUERDA) {
            if (podeMoverPara(CIMA)) {
                setDirecao(CIMA);
                return true;
            }
            if (podeMoverPara(BAIXO)) {
                setDirecao(BAIXO);
                return true;
            }
        } else {
            if (podeMoverPara(DIREITA)) {
                setDirecao(DIREITA);
                return true;
            }
            if (podeMoverPara(ESQUERDA)) {
                setDirecao(ESQUERDA);
                return true;
            }
        }

        para();
        return false;
    }

    @Override
    public void recebeuEnergia() {
        recebeuEnergiaNoTurno = true;
        registrarCogumeloAtual();
    }

    @Override
    public void tomouDano(int energiaRestanteInimigo) {
        tomouDanoNoTurno = true;
        energiaInimigo = energiaRestanteInimigo;
        registrarPerigoAtual();
    }

    @Override
    public void ganhouCombate() {
        ganhouCombateNoTurno = true;
        conhecePerigo = false;
        idadeInfoPerigo = 9999;
    }

    @Override
    public void recebeuMensagem(String msg) {
        if (msg == null) {
            return;
        }

        String[] partes = msg.split(";");
        if (partes.length < 4) {
            return;
        }

        if ("M".equals(partes[0])) {
            int x = inteiroSeguro(partes[1], -1);
            int y = inteiroSeguro(partes[2], -1);
            int idade = inteiroSeguro(partes[3], turnosVivo);

            if (x >= 0 && y >= 0 && (turnosVivo - idade) <= alcanceMemoriaCogumelo) {
                cogumeloX = x;
                cogumeloY = y;
                conheceCogumelo = true;
                idadeInfoCogumelo = turnosVivo - idade;
            }
        } else if ("E".equals(partes[0]) && partes.length >= 5) {
            int x = inteiroSeguro(partes[1], -1);
            int y = inteiroSeguro(partes[2], -1);
            int energia = inteiroSeguro(partes[3], -1);
            int idade = inteiroSeguro(partes[4], turnosVivo);

            if (x >= 0 && y >= 0 && (turnosVivo - idade) <= alcanceMemoriaPerigo) {
                perigoX = x;
                perigoY = y;
                energiaInimigo = energia;
                conhecePerigo = true;
                idadeInfoPerigo = turnosVivo - idade;
            }
        }
    }

    private int inteiroSeguro(String valor, int padrao) {
        try {
            return Integer.parseInt(valor);
        } catch (Exception e) {
            return padrao;
        }
    }

    @Override
    public String getEquipe() {
        return "EquipeEmboscada";
    }
}
