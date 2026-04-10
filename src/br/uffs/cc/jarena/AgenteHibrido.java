package br.uffs.cc.jarena;

// TODO: substituir pelos nomes da dupla antes da entrega.
// Integrantes: NOME_1 e NOME_2

public class AgenteHibrido extends AgenteBaseEstrategico {

    private boolean modoDefensivo;

    public AgenteHibrido(Integer x, Integer y, Integer energia) {
        super(x, y, energia);
        energiaParaDividir = 840;
        energiaParaFugir = 170;
        vantagemMinimaCombate = 100;
        janelaBroadcast = 8;
        janelaPersistenciaPonto = 70;
        modoDefensivo = false;
    }

    @Override
    public void pensa() {
        prepararTurno();

        if (getEnergia() < 300 || turnosDesdePerigo <= 2) {
            modoDefensivo = true;
        } else if (getEnergia() > 650 && turnosSemEnergia < 25) {
            modoDefensivo = false;
        }

        if (recebeuEnergiaNoTurno) {
            broadcastPontoSePrecisar();

            if (modoDefensivo) {
                if (valeDividir() && turnosNoPonto >= 5) {
                    divide();
                } else {
                    para();
                }
            } else {
                if (valeDividir() && turnosNoPonto >= 3) {
                    divide();
                } else if (getEnergia() > 550 && turnosVivo % 18 == 0) {
                    explorarPadrao();
                } else {
                    para();
                }
            }

            finalizarTurno();
            return;
        }

        if (tomouDanoNoTurno) {
            broadcastPerigoSePrecisar();

            if (!modoDefensivo && podeAtacar()) {
                para();
            } else if (modoDefensivo && conhecePonto && !estaFraco()) {
                tentarIrParaPontoConhecido();
            } else {
                recuar();
            }

            finalizarTurno();
            return;
        }

        if (estaFraco()) {
            modoDefensivo = true;
            if (conhecePonto) {
                tentarIrParaPontoConhecido();
            } else {
                recuar();
            }
            finalizarTurno();
            return;
        }

        if (conhecePonto) {
            if (modoDefensivo || turnosSemEnergia <= 16) {
                tentarIrParaPontoConhecido();
            } else {
                explorarPadrao();
            }
        } else {
            explorarPadrao();
        }

        if (turnosVivo % 16 == 0) {
            broadcastPontoSePrecisar();
        }

        avisarPontoSecoSeNecessario();
        finalizarTurno();
    }

    @Override
    public String getEquipe() {
        return "EquipeHibridaTeste";
    }
}
