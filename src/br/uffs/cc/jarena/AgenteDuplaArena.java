package br.uffs.cc.jarena;

/**
 * Agente simples da equipe DuplaArena.
 *
 * Estrategia inicial, de proposito bem facil de ler:
 * - explora o mapa em linha reta;
 * - troca de direcao quando encontra uma borda;
 * - quando recebe energia, fica parado por alguns turnos para economizar;
 * - se tiver energia sobrando, divide de forma conservadora.
 */
public class AgenteDuplaArena extends Agente {
	private static final String EQUIPE = "DuplaArena";
	private static final int ENERGIA_MINIMA_PARA_DIVIDIR = 900;
	private static final int TURNOS_FARMANDO_APOS_ENERGIA = 6;

	private int turnosFarmando;

	public AgenteDuplaArena(Integer x, Integer y, Integer energia) {
		super(x, y, energia);
		setDirecao(geraDirecaoAleatoria());
		turnosFarmando = 0;
	}

	public void pensa() {
		if (turnosFarmando > 0) {
			turnosFarmando--;
			para();
			return;
		}

		if (!podeMoverPara(getDirecao())) {
			trocaDirecaoAoBaterNaBorda();
		}

		if (podeDividir() && getEnergia() >= ENERGIA_MINIMA_PARA_DIVIDIR) {
			divide();
		}
	}

	public void recebeuEnergia() {
		turnosFarmando = TURNOS_FARMANDO_APOS_ENERGIA;
	}

	public void tomouDano(int energiaRestanteInimigo) {
		if (energiaRestanteInimigo > getEnergia()) {
			setDirecao(geraDirecaoAleatoria());
		}
	}

	public void ganhouCombate() {
		if (!podeMoverPara(getDirecao())) {
			setDirecao(geraDirecaoAleatoria());
		}
	}

	public void recebeuMensagem(String msg) {
		if ("ENERGIA".equals(msg)) {
			turnosFarmando = Math.max(turnosFarmando, 2);
		}
	}

	public String getEquipe() {
		return EQUIPE;
	}

	private void trocaDirecaoAoBaterNaBorda() {
		int novaDirecao = geraDirecaoAleatoria();
		int tentativas = 0;

		while (!podeMoverPara(novaDirecao) && tentativas < 8) {
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
