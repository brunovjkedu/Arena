package br.uffs.cc.jarena;

// TODO: substituir pelos nomes da dupla antes da entrega.
// Integrantes: NOME_1 e NOME_2

public class AgenteEconomico extends AgenteBaseEstrategico {

    public AgenteEconomico(Integer x, Integer y, Integer energia) {
        super(x, y, energia);
        energiaParaDividir = 930;
        energiaParaFugir = 220;
        vantagemMinimaCombate = 180;
        janelaBroadcast = 12;
        janelaPersistenciaPonto = 55;
    }

    @Override
    public void pensa() {
        prepararTurno();

        if (recebeuEnergiaNoTurno) {
            broadcastPontoSePrecisar();

            if (valeDividir() && turnosNoPonto >= 4) {
                divide();
            } else {
                para();
            }

            finalizarTurno();
            return;
        }

        if (tomouDanoNoTurno) {
            broadcastPerigoSePrecisar();

            if (podeAtacar() && !estaFraco()) {
                para();
            } else {
                recuar();
            }

            finalizarTurno();
            return;
        }

        if (estaFraco()) {
            if (conhecePonto) {
                tentarIrParaPontoConhecido();
            } else {
                recuar();
            }

            finalizarTurno();
            return;
        }

        if (conhecePonto && turnosSemEnergia <= 20) {
            tentarIrParaPontoConhecido();
        } else {
            explorarPadrao();
        }

        avisarPontoSecoSeNecessario();
        finalizarTurno();
    }

    @Override
    public String getEquipe() {
        return "EquipeEconomicaTeste";
    }
}
