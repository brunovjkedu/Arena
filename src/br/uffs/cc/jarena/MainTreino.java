package br.uffs.cc.jarena;

/**
 * Executa a arena em modo headless e imprime um resumo compacto para scripts de treino.
 */
public class MainTreino {
	public static void main(String[] args) {
		int maxTurnos = leParametroInteiro("jarena.maxTurnos", 5000, 1, 200000);
		Arena arena = new Arena(true);

		arena.runHeadless(maxTurnos);
		System.out.print(arena.getEstatistico().montaResumoTreino());
	}

	private static int leParametroInteiro(String nome, int valorPadrao, int minimo, int maximo) {
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
