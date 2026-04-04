package br.uffs.cc.jarena;

/**
 * Agente simples da equipe DuplaArena.
 *
 * Estrategia didatica e facil de observar:
 * - explora o mapa em linha reta;
 * - troca de direcao quando encontra uma borda;
 * - se recebeu energia neste turno, fica parado para economizar e aproveitar a fonte;
 * - se a fonte sair de perto, volta a explorar no turno seguinte;
 * - por enquanto nao divide, para manter a comparacao mais facil.
 */
public class AgenteDuplaArena extends Agente {
	// Nao usamos static no agente para evitar estado compartilhado entre instancias.
	private final String equipe;

	private boolean recebeuEnergiaNesteTurno;

	public AgenteDuplaArena(Integer x, Integer y, Integer energia) {
		super(x, y, energia);
		equipe = "DuplaArena";
		setDirecao(geraDirecaoAleatoria());
		recebeuEnergiaNesteTurno = false;
	}

	public void pensa() {
		// PontoEnergia chama recebeuEnergia() antes do agente pensar neste turno.
		// Entao esse booleano funciona como um sensor simples de "ainda estou na fonte agora?".
		if (recebeuEnergiaNesteTurno) {
			recebeuEnergiaNesteTurno = false;
			para();
			return;
		}

		if (podeMoverPara(getDirecao()) == false) {
			trocaDirecaoAoBaterNaBorda();
		}

		// Estrategia atual: nao dividir automaticamente.
	}

	public void recebeuEnergia() {
		recebeuEnergiaNesteTurno = true;
	}

	public void tomouDano(int energiaRestanteInimigo) {
		// Reacao simples e conservadora: se o inimigo aparenta ter mais energia,
		// troca direcao para tentar sair dessa colisao ruim.
		if (energiaRestanteInimigo > getEnergia()) {
			trocaDirecaoAoBaterNaBorda();
		}
	}

	public void ganhouCombate() {
		if (podeMoverPara(getDirecao()) == false) {
			trocaDirecaoAoBaterNaBorda();
		}
	}

	public void recebeuMensagem(String msg) {
		// Ainda nao estamos usando comunicacao na estrategia simples.
	}

	public String getEquipe() {
		return equipe;
	}

	private void trocaDirecaoAoBaterNaBorda() {
		int novaDirecao = geraDirecaoAleatoria();
		int tentativas = 0;

		while (podeMoverPara(novaDirecao) == false && tentativas < 8) {
			novaDirecao = geraDirecaoAleatoria();
			tentativas++;
		}

		if (podeMoverPara(novaDirecao)) {
			setDirecao(novaDirecao);
		} else {
			para();
		}
	}
}
