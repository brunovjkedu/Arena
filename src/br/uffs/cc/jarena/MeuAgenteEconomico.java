package br.uffs.cc.jarena;

// Integrantes: PREENCHER_ANTES_DA_ENTREGA

public class MeuAgenteEconomico extends Agente {

    // Parametros tunaveis para busca/ML.
    private int energiaBaixa;
    private int energiaCritica;
    private int energiaMinimaParaDividir;
    private int turnosMinimosNoCogumeloParaDividir;
    private int vantagemEnergiaParaLutar;
    private int energiaMinimaParaLutar;
    private int energiaMinimaParaSegurarCogumelo;
    private int alcanceMemoriaCogumelo;
    private int alcanceMemoriaPerigo;
    private int intervaloBroadcastCogumelo;
    private int intervaloBroadcastPerigo;
    private int janelaBuscaLocal;
    private int periodoTrocaExploracao;
    private int turnosMaximosPerseguindoCogumelo;
    private int energiaMinimaParaPararNoCogumelo;
    private int turnosSemEnergiaParaEsquecerCogumelo;

    private boolean recebeuEnergiaNoTurno;
    private boolean tomouDanoNoTurno;
    private boolean ganhouCombateNoTurno;

    private boolean conheceCogumelo;
    private boolean conhecePerigo;

    private int energiaInimigo;
    private int idadeInfoCogumelo;
    private int idadeInfoPerigo;
    private int turnosVivo;
    private int turnosNoCogumelo;
    private int turnosSemEnergia;
    private int turnosDesdeMensagemCogumelo;
    private int turnosDesdeMensagemPerigo;
    private int ciclosBuscaLocal;

    private int cogumeloX;
    private int cogumeloY;
    private int perigoX;
    private int perigoY;

    private int spawnX;
    private int spawnY;
    private int papel;

    public MeuAgenteEconomico(Integer x, Integer y, Integer energia) {
        super(x, y, energia);

        spawnX = x;
        spawnY = y;
        papel = getId() % 6;

        configurarParametros();
        resetarMemorias();

        setDirecao(direcaoInicial());
    }

    private void configurarParametros() {
        energiaBaixa = 200;
        energiaCritica = 110;
        energiaMinimaParaDividir = 920;
        turnosMinimosNoCogumeloParaDividir = 4;
        vantagemEnergiaParaLutar = 150;
        energiaMinimaParaLutar = 340;
        energiaMinimaParaSegurarCogumelo = 300;
        alcanceMemoriaCogumelo = 45;
        alcanceMemoriaPerigo = 5;
        intervaloBroadcastCogumelo = 8;
        intervaloBroadcastPerigo = 3;
        janelaBuscaLocal = 18;
        periodoTrocaExploracao = 14;
        turnosMaximosPerseguindoCogumelo = 18;
        energiaMinimaParaPararNoCogumelo = 540;
        turnosSemEnergiaParaEsquecerCogumelo = 32;
    }

    private void resetarMemorias() {
        recebeuEnergiaNoTurno = false;
        tomouDanoNoTurno = false;
        ganhouCombateNoTurno = false;

        conheceCogumelo = false;
        conhecePerigo = false;

        energiaInimigo = -1;
        idadeInfoCogumelo = 9999;
        idadeInfoPerigo = 9999;
        turnosVivo = 0;
        turnosNoCogumelo = 0;
        turnosSemEnergia = 0;
        turnosDesdeMensagemCogumelo = 9999;
        turnosDesdeMensagemPerigo = 9999;
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
                if (deveLutarAgora()) {
                    para();
                } else {
                    recuar();
                }

                finalizarTurno();
                return;
            }

            if (deveDividirAgora()) {
                divide();
            } else if (deveSegurarCogumelo()) {
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

        if (estaCritico()) {
            if (cogumeloConhecidoAindaVale()) {
                moverNaDirecaoDoAlvo(cogumeloX, cogumeloY);
            } else {
                recuar();
            }

            finalizarTurno();
            return;
        }

        if (cogumeloConhecidoAindaVale()) {
            if (turnosSemEnergia <= turnosMaximosPerseguindoCogumelo) {
                moverNaDirecaoDoAlvo(cogumeloX, cogumeloY);
            } else {
                conheceCogumelo = false;
                explorar();
            }
        } else {
            explorar();
        }

        finalizarTurno();
    }

    private void prepararTurno() {
        turnosVivo++;
        idadeInfoCogumelo++;
        idadeInfoPerigo++;
        turnosDesdeMensagemCogumelo++;
        turnosDesdeMensagemPerigo++;

        if (recebeuEnergiaNoTurno) {
            turnosSemEnergia = 0;
            turnosNoCogumelo++;
            ciclosBuscaLocal = 0;
        } else {
            turnosSemEnergia++;
            turnosNoCogumelo = 0;
        }

        if (turnosSemEnergia > turnosSemEnergiaParaEsquecerCogumelo) {
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

    private boolean estaCritico() {
        return getEnergia() <= energiaCritica;
    }

    private boolean deveDividirAgora() {
        return podeDividir()
                && !tomouDanoNoTurno
                && getEnergia() >= energiaMinimaParaDividir
                && turnosNoCogumelo >= turnosMinimosNoCogumeloParaDividir;
    }

    private boolean deveSegurarCogumelo() {
        return getEnergia() >= energiaMinimaParaPararNoCogumelo || turnosNoCogumelo >= 2;
    }

    private boolean deveLutarAgora() {
        if (energiaInimigo < 0) {
            return false;
        }

        if (recebeuEnergiaNoTurno && getEnergia() >= energiaMinimaParaSegurarCogumelo) {
            return getEnergia() + vantagemEnergiaParaLutar >= energiaInimigo;
        }

        return getEnergia() >= energiaMinimaParaLutar
                && (getEnergia() - energiaInimigo) >= vantagemEnergiaParaLutar;
    }

    private boolean cogumeloConhecidoAindaVale() {
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
                return CIMA;
            }
            if (papel == 1) {
                return BAIXO;
            }
            if (papel == 2) {
                return ESQUERDA;
            }
            return DIREITA;
        }

        if (spawnX < Constants.LARGURA_MAPA / 2) {
            if (papel == 1) {
                return BAIXO;
            }
            if (papel == 2) {
                return CIMA;
            }
            return DIREITA;
        }

        if (papel == 1) {
            return BAIXO;
        }
        if (papel == 2) {
            return CIMA;
        }
        return ESQUERDA;
    }

    private void explorar() {
        if (papel == 0) {
            explorarFaixaHorizontal();
        } else if (papel == 1) {
            explorarFaixaVertical();
        } else if (papel == 2) {
            explorarZigueZague();
        } else if (papel == 3) {
            explorarEmDirecaoAoCentro();
        } else if (papel == 4) {
            explorarFugindoDoCentroSeNecessario();
        } else {
            explorarMisturado();
        }
    }

    private void explorarFaixaHorizontal() {
        int principal = spawnX < Constants.LARGURA_MAPA / 2 ? DIREITA : ESQUERDA;
        int secundaria = getY() < Constants.ALTURA_MAPA / 2 ? BAIXO : CIMA;

        if (!tentarMover(principal) || turnosVivo % periodoTrocaExploracao == 0) {
            tentarMover(secundaria);
        }
    }

    private void explorarFaixaVertical() {
        int principal = spawnY < Constants.ALTURA_MAPA / 2 ? BAIXO : CIMA;
        int secundaria = spawnX < Constants.LARGURA_MAPA / 2 ? DIREITA : ESQUERDA;

        if (!tentarMover(principal) || turnosVivo % periodoTrocaExploracao == 0) {
            tentarMover(secundaria);
        }
    }

    private void explorarZigueZague() {
        int fase = (turnosVivo / Math.max(1, periodoTrocaExploracao / 2)) % 2;
        int horizontal = spawnX < Constants.LARGURA_MAPA / 2 ? DIREITA : ESQUERDA;
        int vertical = papel % 2 == 0 ? BAIXO : CIMA;

        if (fase == 0) {
            if (!tentarMover(horizontal)) {
                tentarMover(vertical);
            }
        } else if (!tentarMover(vertical)) {
            tentarMover(horizontal);
        }
    }

    private void explorarEmDirecaoAoCentro() {
        int centroX = Constants.LARGURA_MAPA / 2;
        int centroY = Constants.ALTURA_MAPA / 2;

        if (Math.abs(getX() - centroX) > 100 || Math.abs(getY() - centroY) > 100) {
            moverNaDirecaoDoAlvo(centroX, centroY);
        } else {
            explorarZigueZague();
        }
    }

    private void explorarFugindoDoCentroSeNecessario() {
        boolean nasceuNoCentro = Math.abs(spawnX - (Constants.LARGURA_MAPA / 2)) < 120;

        if (nasceuNoCentro && turnosVivo < 25) {
            if (papel % 2 == 0) {
                if (!tentarMover(ESQUERDA)) {
                    tentarMover(CIMA);
                }
            } else if (!tentarMover(DIREITA)) {
                tentarMover(BAIXO);
            }
            return;
        }

        explorarFaixaHorizontal();
    }

    private void explorarMisturado() {
        if (turnosVivo % periodoTrocaExploracao == 0) {
            int fase = (turnosVivo / periodoTrocaExploracao + papel) % 4;
            if (fase == 0) {
                tentarMover(DIREITA);
            } else if (fase == 1) {
                tentarMover(BAIXO);
            } else if (fase == 2) {
                tentarMover(ESQUERDA);
            } else {
                tentarMover(CIMA);
            }
        } else if (!tentarMover(getDirecao())) {
            tentarMover(geraDirecaoAleatoria());
        }
    }

    private void buscarLocalmenteAoRedorDoCogumelo() {
        if (!cogumeloConhecidoAindaVale()) {
            explorar();
            return;
        }

        if (Math.abs(cogumeloX - getX()) > Constants.ENTIDADE_VELOCIDADE
                || Math.abs(cogumeloY - getY()) > Constants.ENTIDADE_VELOCIDADE) {
            moverNaDirecaoDoAlvo(cogumeloX, cogumeloY);
            return;
        }

        int fase = (ciclosBuscaLocal / 3 + papel) % 4;
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
            explorar();
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

        if (spawnX < Constants.LARGURA_MAPA / 2) {
            if (!tentarMover(ESQUERDA)) {
                tentarMover(spawnY < Constants.ALTURA_MAPA / 2 ? CIMA : BAIXO);
            }
        } else if (!tentarMover(DIREITA)) {
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
                if (!conheceCogumelo || idadeInfoCogumelo > (turnosVivo - idade)) {
                    cogumeloX = x;
                    cogumeloY = y;
                    conheceCogumelo = true;
                    idadeInfoCogumelo = turnosVivo - idade;
                }
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
        return "MinhaEquipe";
    }
}
