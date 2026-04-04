package br.uffs.cc.jarena;

/**
 * Agente simples da equipe DuplaArena.
 *
 * Estrategia didatica e facil de observar:
 * - explora o mapa em linha reta;
 * - troca de direcao quando encontra uma borda;
 * - se recebeu energia neste turno, fica parado e avisa aliados proximos com sua posicao;
 * - se um aliado avisar "ENERGIA:x:y", tenta se mover ate essa posicao por alguns turnos;
 * - quando perde a fonte ou o aviso expira, volta a explorar explicitamente.
 *
 * Parametros ajustaveis no treino offline:
 * -Dduplaarena.turnosAviso=12
 * -Dduplaarena.distanciaAlvo=5
 * -Dduplaarena.tentativasDirecao=8
 */
public class AgenteDuplaArena extends Agente {
	private final String equipe;
	private final int turnosParaSeguirAviso;
	private final int distanciaMinimaDoAlvo;
	private final int maxTentativasDirecao;

	private boolean recebeuEnergiaNesteTurno;
	private int turnosSeguindoAvisoEnergia;
	private int alvoEnergiaX;
	private int alvoEnergiaY;
	private boolean temAlvoEnergia;

	public AgenteDuplaArena(Integer x, Integer y, Integer energia) {
		super(x, y, energia);
		equipe = "DuplaArena";
		turnosParaSeguirAviso = leParametroInteiro("duplaarena.turnosAviso", 12, 0, 200);
		distanciaMinimaDoAlvo = leParametroInteiro("duplaarena.distanciaAlvo", Constants.ENTIDADE_VELOCIDADE, 0, 100);
		maxTentativasDirecao = leParametroInteiro("duplaarena.tentativasDirecao", 8, 1, 50);
		recebeuEnergiaNesteTurno = false;
		turnosSeguindoAvisoEnergia = 0;
		temAlvoEnergia = false;
		setDirecao(geraDirecaoAleatoria());
	}

	public void pensa() {
		if (recebeuEnergiaNesteTurno) {
			recebeuEnergiaNesteTurno = false;
			temAlvoEnergia = false;
			turnosSeguindoAvisoEnergia = 0;
			enviaMensagem("ENERGIA:" + getX() + ":" + getY());
			para();
			return;
		}

		if (temAlvoEnergia && turnosSeguindoAvisoEnergia > 0) {
			turnosSeguindoAvisoEnergia--;
			moveParaAlvoEnergia();
			return;
		}

		temAlvoEnergia = false;
		retomaExploracao();

		if (podeMoverPara(getDirecao()) == false) {
			trocaDirecaoAoBaterNaBorda();
		}
	}

	public void recebeuEnergia() {
		recebeuEnergiaNesteTurno = true;
	}

	public void tomouDano(int energiaRestanteInimigo) {
		if (energiaRestanteInimigo > getEnergia()) {
			temAlvoEnergia = false;
			turnosSeguindoAvisoEnergia = 0;
			trocaDirecaoAoBaterNaBorda();
		}
	}

	public void ganhouCombate() {
		retomaExploracao();

		if (podeMoverPara(getDirecao()) == false) {
			trocaDirecaoAoBaterNaBorda();
		}
	}

	public void recebeuMensagem(String msg) {
		if (msg != null && msg.startsWith("ENERGIA:")) {
			String[] partes = msg.split(":");

			if (partes.length == 3) {
				try {
					alvoEnergiaX = Integer.parseInt(partes[1]);
					alvoEnergiaY = Integer.parseInt(partes[2]);
					temAlvoEnergia = true;
					turnosSeguindoAvisoEnergia = turnosParaSeguirAviso;
				} catch (Exception e) {
					temAlvoEnergia = false;
					turnosSeguindoAvisoEnergia = 0;
				}
			}
		}
	}

	public String getEquipe() {
		return equipe;
	}

	private void moveParaAlvoEnergia() {
		int distanciaX = alvoEnergiaX - getX();
		int distanciaY = alvoEnergiaY - getY();

		if (Math.abs(distanciaX) <= distanciaMinimaDoAlvo && Math.abs(distanciaY) <= distanciaMinimaDoAlvo) {
			temAlvoEnergia = false;
			turnosSeguindoAvisoEnergia = 0;
			retomaExploracao();
			return;
		}

		if (Math.abs(distanciaX) >= Math.abs(distanciaY)) {
			tentaDirecaoComFallback(distanciaX > 0 ? DIREITA : ESQUERDA);
		} else {
			tentaDirecaoComFallback(distanciaY > 0 ? BAIXO : CIMA);
		}
	}

	private void retomaExploracao() {
		// para() deixa o agente parado ate alguem chamar setDirecao() de novo.
		if (isParado()) {
			if (podeMoverPara(getDirecao())) {
				setDirecao(getDirecao());
			} else {
				trocaDirecaoAoBaterNaBorda();
			}
		}
	}

	private void tentaDirecaoComFallback(int direcaoPreferida) {
		if (podeMoverPara(direcaoPreferida)) {
			setDirecao(direcaoPreferida);
		} else {
			trocaDirecaoAoBaterNaBorda();
		}
	}

	private void trocaDirecaoAoBaterNaBorda() {
		int novaDirecao = geraDirecaoAleatoria();
		int tentativas = 0;

		while (podeMoverPara(novaDirecao) == false && tentativas < maxTentativasDirecao) {
			novaDirecao = geraDirecaoAleatoria();
			tentativas++;
		}

		if (podeMoverPara(novaDirecao)) {
			setDirecao(novaDirecao);
		} else {
			para();
		}
	}

	private int leParametroInteiro(String nome, int valorPadrao, int minimo, int maximo) {
		String valorTexto = System.getProperty(nome);
		int valor;

		if (valorTexto == null) {
			return valorPadrao;
		}

		try {
			valor = Integer.parseInt(valorTexto);
		} catch (Exception e) {
			return valorPadrao;
		}

		if (valor < minimo) {
			return minimo;
		}

		if (valor > maximo) {
			return maximo;
		}

		return valor;
	}
}
