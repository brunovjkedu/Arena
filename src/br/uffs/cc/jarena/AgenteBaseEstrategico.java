package br.uffs.cc.jarena;

// TODO: substituir pelos nomes da dupla antes da entrega.
// Integrantes: NOME_1 e NOME_2

public abstract class AgenteBaseEstrategico extends Agente {

    protected boolean recebeuEnergiaNoTurno;
    protected boolean tomouDanoNoTurno;
    protected boolean ganhouCombateNoUltimoTurno;

    protected int energiaInimigo;
    protected int turnosVivo;
    protected int turnosSemEnergia;
    protected int turnosNoPonto;
    protected int turnosDesdeUltimaMensagem;
    protected int turnosDesdePerigo;
    protected int turnosDesdeCombate;

    protected int ultimoPontoX;
    protected int ultimoPontoY;
    protected boolean conhecePonto;

    protected int ultimoPerigoX;
    protected int ultimoPerigoY;
    protected int ultimaEnergiaInimiga;
    protected boolean conhecePerigo;

    protected int ciclosBuscaLocal;
    protected int direcaoExploracaoFavorita;

    protected int energiaParaDividir;
    protected int energiaParaFugir;
    protected int vantagemMinimaCombate;
    protected int janelaBroadcast;
    protected int janelaPersistenciaPonto;

    public AgenteBaseEstrategico(Integer x, Integer y, Integer energia) {
        super(x, y, energia);

        recebeuEnergiaNoTurno = false;
        tomouDanoNoTurno = false;
        ganhouCombateNoUltimoTurno = false;

        energiaInimigo = -1;
        turnosVivo = 0;
        turnosSemEnergia = 0;
        turnosNoPonto = 0;
        turnosDesdeUltimaMensagem = 9999;
        turnosDesdePerigo = 9999;
        turnosDesdeCombate = 9999;

        ultimoPontoX = -1;
        ultimoPontoY = -1;
        conhecePonto = false;

        ultimoPerigoX = -1;
        ultimoPerigoY = -1;
        ultimaEnergiaInimiga = -1;
        conhecePerigo = false;

        ciclosBuscaLocal = 0;
        direcaoExploracaoFavorita = defineDirecaoInicial();

        energiaParaDividir = 850;
        energiaParaFugir = 180;
        vantagemMinimaCombate = 120;
        janelaBroadcast = 10;
        janelaPersistenciaPonto = 60;

        setDirecao(direcaoExploracaoFavorita);
    }

    protected int defineDirecaoInicial() {
        int papel = getId() % 4;

        if (papel == 0 || papel == 2) {
            return DIREITA;
        }

        return BAIXO;
    }

    protected void prepararTurno() {
        turnosVivo++;
        turnosDesdeUltimaMensagem++;
        turnosDesdePerigo++;
        turnosDesdeCombate++;

        if (recebeuEnergiaNoTurno) {
            turnosSemEnergia = 0;
            turnosNoPonto++;
            ciclosBuscaLocal = 0;
        } else {
            turnosSemEnergia++;
            turnosNoPonto = 0;
        }
    }

    protected void finalizarTurno() {
        recebeuEnergiaNoTurno = false;
        tomouDanoNoTurno = false;
        ganhouCombateNoUltimoTurno = false;
        energiaInimigo = -1;
    }

    protected void registrarPontoAtual() {
        ultimoPontoX = getX();
        ultimoPontoY = getY();
        conhecePonto = true;
        ciclosBuscaLocal = 0;
    }

    protected void registrarPerigoAtual(int energiaDoInimigo) {
        ultimoPerigoX = getX();
        ultimoPerigoY = getY();
        ultimaEnergiaInimiga = energiaDoInimigo;
        conhecePerigo = true;
        turnosDesdePerigo = 0;
        turnosDesdeCombate = 0;
    }

    protected boolean estaFraco() {
        return getEnergia() <= energiaParaFugir;
    }

    protected boolean podeAtacar() {
        return energiaInimigo >= 0 && getEnergia() >= (energiaInimigo + vantagemMinimaCombate);
    }

    protected boolean valeDividir() {
        return podeDividir() && getEnergia() >= energiaParaDividir && !tomouDanoNoTurno;
    }

    protected void broadcastPontoSePrecisar() {
        if (conhecePonto && (recebeuEnergiaNoTurno || turnosVivo % janelaBroadcast == 0)) {
            enviaMensagem("MUSH;" + ultimoPontoX + ";" + ultimoPontoY + ";" + turnosVivo);
        }
    }

    protected void broadcastPerigoSePrecisar() {
        if (conhecePerigo && turnosDesdePerigo <= 2) {
            enviaMensagem("ENEMY;" + ultimoPerigoX + ";" + ultimoPerigoY + ";" + ultimaEnergiaInimiga + ";" + turnosVivo);
        }
    }

    protected void tentarIrParaPontoConhecido() {
        if (!conhecePonto) {
            explorarPadrao();
            return;
        }

        int dx = ultimoPontoX - getX();
        int dy = ultimoPontoY - getY();

        if (Math.abs(dx) <= Constants.ENTIDADE_VELOCIDADE && Math.abs(dy) <= Constants.ENTIDADE_VELOCIDADE) {
            buscarLocalmenteAoRedorDoPonto();
            return;
        }

        moverNaDirecaoDoAlvo(ultimoPontoX, ultimoPontoY);
    }

    protected void buscarLocalmenteAoRedorDoPonto() {
        int fase = ((ciclosBuscaLocal / 3) + (getId() % 4)) % 4;
        int dir = DIREITA;

        if (fase == 0) {
            dir = DIREITA;
        } else if (fase == 1) {
            dir = BAIXO;
        } else if (fase == 2) {
            dir = ESQUERDA;
        } else {
            dir = CIMA;
        }

        ciclosBuscaLocal++;
        tentarMoverOuAlternar(dir);
    }

    protected void explorarPadrao() {
        int papel = getId() % 4;

        if (papel == 0) {
            explorarHorizontalPrimeiro();
        } else if (papel == 1) {
            explorarVerticalPrimeiro();
        } else if (papel == 2) {
            explorarDiagonalZigueZague();
        } else {
            explorarComMudancasPeriodicas();
        }
    }

    protected void explorarHorizontalPrimeiro() {
        if (getDirecao() != DIREITA && getDirecao() != ESQUERDA) {
            setDirecao(DIREITA);
        }

        if (!podeMoverPara(getDirecao())) {
            if (getY() < Constants.ALTURA_MAPA / 2) {
                tentarMoverOuAlternar(BAIXO);
            } else {
                tentarMoverOuAlternar(CIMA);
            }
        }
    }

    protected void explorarVerticalPrimeiro() {
        if (getDirecao() != BAIXO && getDirecao() != CIMA) {
            setDirecao(BAIXO);
        }

        if (!podeMoverPara(getDirecao())) {
            if (getX() < Constants.LARGURA_MAPA / 2) {
                tentarMoverOuAlternar(DIREITA);
            } else {
                tentarMoverOuAlternar(ESQUERDA);
            }
        }
    }

    protected void explorarDiagonalZigueZague() {
        if (turnosVivo % 12 == 0) {
            if (getDirecao() == DIREITA || getDirecao() == ESQUERDA) {
                if (getY() < Constants.ALTURA_MAPA / 2) {
                    tentarMoverOuAlternar(BAIXO);
                } else {
                    tentarMoverOuAlternar(CIMA);
                }
            } else {
                if (getX() < Constants.LARGURA_MAPA / 2) {
                    tentarMoverOuAlternar(DIREITA);
                } else {
                    tentarMoverOuAlternar(ESQUERDA);
                }
            }
        }

        if (!podeMoverPara(getDirecao())) {
            if (getDirecao() == DIREITA || getDirecao() == ESQUERDA) {
                if (getY() < Constants.ALTURA_MAPA / 2) {
                    tentarMoverOuAlternar(BAIXO);
                } else {
                    tentarMoverOuAlternar(CIMA);
                }
            } else {
                if (getX() < Constants.LARGURA_MAPA / 2) {
                    tentarMoverOuAlternar(DIREITA);
                } else {
                    tentarMoverOuAlternar(ESQUERDA);
                }
            }
        }
    }

    protected void explorarComMudancasPeriodicas() {
        if (turnosSemEnergia > 0 && turnosSemEnergia % 15 == 0) {
            int fase = (turnosSemEnergia / 15 + getId()) % 4;
            if (fase == 0) {
                tentarMoverOuAlternar(DIREITA);
            } else if (fase == 1) {
                tentarMoverOuAlternar(BAIXO);
            } else if (fase == 2) {
                tentarMoverOuAlternar(ESQUERDA);
            } else {
                tentarMoverOuAlternar(CIMA);
            }
        }

        if (!podeMoverPara(getDirecao())) {
            tentarMoverOuAlternar(geraDirecaoAleatoria());
        }
    }

    protected void moverNaDirecaoDoAlvo(int alvoX, int alvoY) {
        int dx = alvoX - getX();
        int dy = alvoY - getY();
        int principal;
        int secundaria;

        if (Math.abs(dx) >= Math.abs(dy)) {
            principal = dx >= 0 ? DIREITA : ESQUERDA;
            secundaria = dy >= 0 ? BAIXO : CIMA;
        } else {
            principal = dy >= 0 ? BAIXO : CIMA;
            secundaria = dx >= 0 ? DIREITA : ESQUERDA;
        }

        if (!tentarMoverOuAlternar(principal)) {
            if (!tentarMoverOuAlternar(secundaria)) {
                explorarPadrao();
            }
        }
    }

    protected boolean tentarMoverOuAlternar(int dir) {
        if (dir == NENHUMA_DIRECAO) {
            para();
            return true;
        }

        if (podeMoverPara(dir)) {
            setDirecao(dir);
            return true;
        }

        if (dir == DIREITA || dir == ESQUERDA) {
            if (getY() < Constants.ALTURA_MAPA / 2) {
                if (podeMoverPara(BAIXO)) {
                    setDirecao(BAIXO);
                    return true;
                }
            }
            if (podeMoverPara(CIMA)) {
                setDirecao(CIMA);
                return true;
            }
            if (podeMoverPara(BAIXO)) {
                setDirecao(BAIXO);
                return true;
            }
        } else {
            if (getX() < Constants.LARGURA_MAPA / 2) {
                if (podeMoverPara(DIREITA)) {
                    setDirecao(DIREITA);
                    return true;
                }
            }
            if (podeMoverPara(ESQUERDA)) {
                setDirecao(ESQUERDA);
                return true;
            }
            if (podeMoverPara(DIREITA)) {
                setDirecao(DIREITA);
                return true;
            }
        }

        return false;
    }

    protected void recuar() {
        if (conhecePerigo && turnosDesdePerigo <= 3) {
            int dx = getX() - ultimoPerigoX;
            int dy = getY() - ultimoPerigoY;

            if (Math.abs(dx) >= Math.abs(dy)) {
                if (dx >= 0) {
                    if (tentarMoverOuAlternar(DIREITA)) {
                        return;
                    }
                } else {
                    if (tentarMoverOuAlternar(ESQUERDA)) {
                        return;
                    }
                }
            } else {
                if (dy >= 0) {
                    if (tentarMoverOuAlternar(BAIXO)) {
                        return;
                    }
                } else {
                    if (tentarMoverOuAlternar(CIMA)) {
                        return;
                    }
                }
            }
        }

        if (getX() < Constants.LARGURA_MAPA / 2) {
            if (getY() < Constants.ALTURA_MAPA / 2) {
                if (!tentarMoverOuAlternar(ESQUERDA)) {
                    tentarMoverOuAlternar(CIMA);
                }
            } else {
                if (!tentarMoverOuAlternar(ESQUERDA)) {
                    tentarMoverOuAlternar(BAIXO);
                }
            }
        } else {
            if (getY() < Constants.ALTURA_MAPA / 2) {
                if (!tentarMoverOuAlternar(DIREITA)) {
                    tentarMoverOuAlternar(CIMA);
                }
            } else {
                if (!tentarMoverOuAlternar(DIREITA)) {
                    tentarMoverOuAlternar(BAIXO);
                }
            }
        }
    }

    @Override
    public void recebeuEnergia() {
        recebeuEnergiaNoTurno = true;
        registrarPontoAtual();
    }

    @Override
    public void tomouDano(int energiaRestanteInimigo) {
        tomouDanoNoTurno = true;
        energiaInimigo = energiaRestanteInimigo;
        registrarPerigoAtual(energiaRestanteInimigo);
    }

    @Override
    public void ganhouCombate() {
        ganhouCombateNoUltimoTurno = true;
        conhecePerigo = false;
        ultimaEnergiaInimiga = -1;
        turnosDesdeCombate = 0;
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

        if ("MUSH".equals(partes[0])) {
            int x = parseSeguro(partes[1], -1);
            int y = parseSeguro(partes[2], -1);
            int idade = parseSeguro(partes[3], turnosVivo);

            if (x >= 0 && y >= 0 && (idade + janelaPersistenciaPonto) >= turnosVivo) {
                ultimoPontoX = x;
                ultimoPontoY = y;
                conhecePonto = true;
                turnosDesdeUltimaMensagem = 0;
            }
        } else if ("ENEMY".equals(partes[0])) {
            int x = parseSeguro(partes[1], -1);
            int y = parseSeguro(partes[2], -1);
            int energia = parseSeguro(partes[3], -1);

            if (x >= 0 && y >= 0) {
                ultimoPerigoX = x;
                ultimoPerigoY = y;
                ultimaEnergiaInimiga = energia;
                conhecePerigo = true;
                turnosDesdePerigo = 0;
                turnosDesdeUltimaMensagem = 0;
            }
        } else if ("DRY".equals(partes[0])) {
            int x = parseSeguro(partes[1], -1);
            int y = parseSeguro(partes[2], -1);

            if (conhecePonto && ultimoPontoX == x && ultimoPontoY == y) {
                conhecePonto = false;
            }
        }
    }

    protected int parseSeguro(String valor, int padrao) {
        try {
            return Integer.parseInt(valor);
        } catch (Exception e) {
            return padrao;
        }
    }

    protected void avisarPontoSecoSeNecessario() {
        if (conhecePonto && !recebeuEnergiaNoTurno && turnosSemEnergia > janelaPersistenciaPonto) {
            enviaMensagem("DRY;" + ultimoPontoX + ";" + ultimoPontoY + ";" + turnosVivo);
            conhecePonto = false;
        }
    }
}
