package br.uffs.cc.jarena;

// TODO: substituir pelos nomes da dupla antes da entrega.
// Integrantes: NOME_1 e NOME_2

public class AgenteAgressivo extends AgenteBaseEstrategico {

    public AgenteAgressivo(Integer x, Integer y, Integer energia) {
        super(x, y, energia);
        energiaParaDividir = 760;
        energiaParaFugir = 140;
        vantagemMinimaCombate = 70;
        janelaBroadcast = 9;
        janelaPersistenciaPonto = 45;
    }

    @Override
    public void pensa() {
        prepararTurno();

        if (recebeuEnergiaNoTurno) {
            broadcastPontoSePrecisar();

            if (valeDividir() && (turnosNoPonto >= 2 || ganhouCombateNoUltimoTurno)) {
                divide();
            } else if (getEnergia() > 500 && turnosNoPonto < 2) {
                explorarPadrao();
            } else {
                para();
            }

            finalizarTurno();
            return;
        }

        if (tomouDanoNoTurno) {
            broadcastPerigoSePrecisar();

            if (podeAtacar() || getEnergia() > 700) {
                para();
            } else {
                recuar();
            }

            finalizarTurno();
            return;
        }

        if (ganhouCombateNoUltimoTurno && valeDividir()) {
            divide();
            finalizarTurno();
            return;
        }

        if (conhecePonto && turnosSemEnergia <= 10) {
            tentarIrParaPontoConhecido();
        } else {
            explorarPadrao();
        }

        avisarPontoSecoSeNecessario();
        finalizarTurno();
    }

    @Override
    public String getEquipe() {
        return "EquipeAgressivaTeste";
    }
}
