package br.uffs.cc.jarena;

/**
 * Agente simples da equipe DuplaArena.
 *
 * Estrategia didatica e facil de observar:
 * - explora o mapa em linha reta;
 * - troca de direcao quando encontra uma borda;
 * - quando recebe energia, fica parado por alguns turnos para economizar;
 * - por enquanto NAO divide, porque o teste anterior mostrou que clones
 *   demais reduziram muito a energia individual e o time morreu mais cedo.
 */
public class AgenteDuplaArena extends Agente {
	private static final String EQUIPE = "DuplaArena";
	private static final int TURNOS_FARMANDO_APOS_ENERGIA = 8;

	private int turnosFarmando;

	public AgenteDuplaArena(Integer x, Integer y, Integer energia) {
		super(x, y, energia);
		setDirecao(geraDirecaoAleatoria());
		turnosFarmando = 0;
	}

	public void pensa() {
		// Se acabamos de receber energia, vale parar um pouco: parado custa menos
		// energia por turno e isso facilita medir o efeito do farm.
		if (turnosFarmando > 0) {
			turnosFarmando--;
			para();
			return;
		}

		if (!podeMoverPara(getDirecao())) {
			trocaDirecaoAoBaterNaBorda();
		}

		// Estrategia atual: nao dividir automaticamente.
		// Isso deve manter agentes mais fortes individualmente e ajudar a comparar
		// com o resultado anterior, onde 15 divisoes pareceram custar caro demais.
	}

	public void recebeuEnergia() {
		turnosFarmando = TURNOS_FARMANDO_APOS_ENERGIA;
	}

	public void tomouDano(int energiaRestanteInimigo) {
		// Reacao simples e conservadora: se o inimigo aparenta ter mais energia,
		// troca direcao para tentar sair dessa colisao ruim.
		if (energiaRestanteInimigo > getEnergia()) {
			trocaDirecaoAoBaterNaBorda();
		}
	}

	public void ganhouCombate() {
		// Mantemos o comportamento simples: so garante uma direcao valida.
		if (!podeMoverPara(getDirecao())) {
			trocaDirecaoAoBaterNaBorda();
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
