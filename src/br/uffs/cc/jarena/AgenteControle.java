package br.uffs.cc.jarena;

// TODO: substituir pelos nomes da dupla antes da entrega.
// Integrantes: NOME_1 e NOME_2

public class AgenteControle extends AgenteBaseEstrategico {

    public AgenteControle(Integer x, Integer y, Integer energia) {
        super(x, y, energia);
        energiaParaDividir = 860;
        energiaParaFugir = 190;
        vantagemMinimaCombate = 120;
        janelaBroadcast = 8;
        janelaPersistenciaPonto = 80;
    }

    @Override
    public void pensa() {
        prepararTurno();

        if (recebeuEnergiaNoTurno) {
            broadcastPontoSePrecisar();

            if (tomouDanoNoTurno && !podeAtacar()) {
                recuar();
                finalizarTurno();
                return;
            }

            if (valeDividir() && turnosNoPonto >= 3) {
                broadcastPontoSePrecisar();
                divide();
            } else {
                para();
            }

            finalizarTurno();
            return;
        }

        if (tomouDanoNoTurno) {
            broadcastPerigoSePrecisar();

            if (podeAtacar()) {
                para();
            } else if (conhecePonto && turnosDesdePerigo <= 1) {
                recuar();
            } else {
                explorarPadrao();
            }

            finalizarTurno();
            return;
        }

        if (conhecePonto) {
            tentarIrParaPontoConhecido();
        } else if (conhecePerigo && turnosDesdePerigo <= 2 && estaFraco()) {
            recuar();
        } else {
            explorarPadrao();
        }

        if (turnosVivo % 14 == 0) {
            broadcastPontoSePrecisar();
        }

        avisarPontoSecoSeNecessario();
        finalizarTurno();
    }

    @Override
    public String getEquipe() {
        return "EquipeControleTeste";
    }
}
