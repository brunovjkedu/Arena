package br.uffs.cc.jarena;

/**
 * Agente com uma politica neural pequena para treino por neuroevolucao.
 *
 * A rede recebe um estado local do agente e escolhe entre:
 * 0 = parar
 * 1 = seguir direcao atual
 * 2 = explorar em direcao aleatoria
 * 3 = seguir alvo de energia recebido por mensagem
 * 4 = fugir/trocar direcao
 * 5 = dividir
 *
 * Os pesos sao passados por -Dneuro.pesos=w1,w2,w3,...
 */
public class AgenteNeuroArena extends Agente {
	private final String equipe;
	private final int entradas;
	private final int ocultos;
	private final int saidas;
	private final int totalPesos;
	private final int turnosMemoriaDano;
	private final int turnosMemoriaMensagem;
	private final double[] pesos;
	private final double[] vetorEntrada;
	private final double[] vetorOculto;
	private final double[] vetorSaida;

	private boolean recebeuEnergiaNesteTurno;
	private int turnosDesdeDano;
	private int turnosAlvoEnergia;
	private int alvoEnergiaX;
	private int alvoEnergiaY;
	private boolean inimigoPareciaMaisForte;

	public AgenteNeuroArena(Integer x, Integer y, Integer energia) {
		super(x, y, energia);
		equipe = "DuplaArena";
		entradas = 12;
		ocultos = 8;
		saidas = 6;
		totalPesos = entradas * ocultos + ocultos + ocultos * saidas + saidas;
		turnosMemoriaDano = 8;
		turnosMemoriaMensagem = 20;
		pesos = carregaPesos();
		vetorEntrada = new double[entradas];
		vetorOculto = new double[ocultos];
		vetorSaida = new double[saidas];
		recebeuEnergiaNesteTurno = false;
		turnosDesdeDano = turnosMemoriaDano;
		turnosAlvoEnergia = 0;
		alvoEnergiaX = getX();
		alvoEnergiaY = getY();
		inimigoPareciaMaisForte = false;
		setDirecao(geraDirecaoAleatoria());
	}

	public void pensa() {
		atualizaMemorias();
		preencheEntrada();
		calculaRede();
		executaAcao(escolheAcao());
		recebeuEnergiaNesteTurno = false;
	}

	public void recebeuEnergia() {
		recebeuEnergiaNesteTurno = true;
		turnosAlvoEnergia = 0;
		enviaMensagem("ENERGIA:" + getX() + ":" + getY());
	}

	public void tomouDano(int energiaRestanteInimigo) {
		turnosDesdeDano = 0;
		inimigoPareciaMaisForte = energiaRestanteInimigo > getEnergia();
	}

	public void ganhouCombate() {
		turnosDesdeDano = turnosMemoriaDano;
		inimigoPareciaMaisForte = false;
	}

	public void recebeuMensagem(String msg) {
		if (msg == null || msg.startsWith("ENERGIA:") == false) {
			return;
		}

		String[] partes = msg.split(":");
		if (partes.length != 3) {
			return;
		}

		try {
			alvoEnergiaX = Integer.parseInt(partes[1]);
			alvoEnergiaY = Integer.parseInt(partes[2]);
			turnosAlvoEnergia = turnosMemoriaMensagem;
		} catch (Exception e) {
			turnosAlvoEnergia = 0;
		}
	}

	public String getEquipe() {
		return equipe;
	}

	private void atualizaMemorias() {
		if (turnosDesdeDano < turnosMemoriaDano) {
			turnosDesdeDano++;
		}

		if (turnosAlvoEnergia > 0) {
			turnosAlvoEnergia--;
		}
	}

	private void preencheEntrada() {
		double dxAlvo = 0.0;
		double dyAlvo = 0.0;

		if (turnosAlvoEnergia > 0) {
			dxAlvo = normaliza(alvoEnergiaX - getX(), Constants.LARGURA_MAPA);
			dyAlvo = normaliza(alvoEnergiaY - getY(), Constants.ALTURA_MAPA);
		}

		vetorEntrada[0] = normaliza(getEnergia(), 2000.0);
		vetorEntrada[1] = recebeuEnergiaNesteTurno ? 1.0 : 0.0;
		vetorEntrada[2] = turnosAlvoEnergia > 0 ? 1.0 : 0.0;
		vetorEntrada[3] = dxAlvo;
		vetorEntrada[4] = dyAlvo;
		vetorEntrada[5] = podeMoverPara(getDirecao()) ? 1.0 : 0.0;
		vetorEntrada[6] = podeDividir() ? 1.0 : 0.0;
		vetorEntrada[7] = normaliza(turnosMemoriaDano - turnosDesdeDano, turnosMemoriaDano);
		vetorEntrada[8] = inimigoPareciaMaisForte ? 1.0 : 0.0;
		vetorEntrada[9] = normaliza(getX(), Constants.LARGURA_MAPA);
		vetorEntrada[10] = normaliza(getY(), Constants.ALTURA_MAPA);
		vetorEntrada[11] = 1.0;
	}

	private void calculaRede() {
		int indice = 0;
		int i;
		int j;

		for (j = 0; j < ocultos; j++) {
			double soma = 0.0;
			for (i = 0; i < entradas; i++) {
				soma += vetorEntrada[i] * pesos[indice++];
			}
			soma += pesos[indice++];
			vetorOculto[j] = Math.tanh(soma);
		}

		for (j = 0; j < saidas; j++) {
			double soma = 0.0;
			for (i = 0; i < ocultos; i++) {
				soma += vetorOculto[i] * pesos[indice++];
			}
			soma += pesos[indice++];
			vetorSaida[j] = soma;
		}
	}

	private int escolheAcao() {
		int melhorIndice = 0;
		int i;

		for (i = 1; i < saidas; i++) {
			if (vetorSaida[i] > vetorSaida[melhorIndice]) {
				melhorIndice = i;
			}
		}

		return melhorIndice;
	}

	private void executaAcao(int acao) {
		switch (acao) {
			case 0:
				para();
				break;
			case 1:
				continuaOuCorrigeDirecao();
				break;
			case 2:
				setDirecao(geraDirecaoAleatoria());
				break;
			case 3:
				segueAlvoEnergia();
				break;
			case 4:
				setDirecao(geraDirecaoAleatoria());
				break;
			case 5:
				if (podeDividir()) {
					divide();
				} else {
					continuaOuCorrigeDirecao();
				}
				break;
			default:
				continuaOuCorrigeDirecao();
		}
	}

	private void continuaOuCorrigeDirecao() {
		if (isParado()) {
			setDirecao(getDirecao());
		}

		if (podeMoverPara(getDirecao()) == false) {
			setDirecao(geraDirecaoAleatoria());
		}
	}

	private void segueAlvoEnergia() {
		int dx;
		int dy;

		if (turnosAlvoEnergia <= 0) {
			continuaOuCorrigeDirecao();
			return;
		}

		dx = alvoEnergiaX - getX();
		dy = alvoEnergiaY - getY();

		if (Math.abs(dx) + Math.abs(dy) <= Constants.ENTIDADE_VELOCIDADE) {
			para();
			return;
		}

		if (Math.abs(dx) >= Math.abs(dy)) {
			setDirecao(dx >= 0 ? DIREITA : ESQUERDA);
		} else {
			setDirecao(dy >= 0 ? BAIXO : CIMA);
		}

		if (podeMoverPara(getDirecao()) == false) {
			setDirecao(geraDirecaoAleatoria());
		}
	}

	private double[] carregaPesos() {
		double[] carregados = new double[totalPesos];
		String textoPesos = System.getProperty("neuro.pesos");

		if (textoPesos == null || textoPesos.trim().length() == 0) {
			return carregados;
		}

		String[] partes = textoPesos.split(",");
		int limite = Math.min(totalPesos, partes.length);
		int i;

		for (i = 0; i < limite; i++) {
			try {
				carregados[i] = Double.parseDouble(partes[i]);
			} catch (Exception e) {
				carregados[i] = 0.0;
			}
		}

		return carregados;
	}

	private double normaliza(double valor, double escala) {
		if (escala == 0.0) {
			return 0.0;
		}

		if (valor > escala) {
			return 1.0;
		}

		if (valor < -escala) {
			return -1.0;
		}

		return valor / escala;
	}
}
