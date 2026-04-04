package br.uffs.cc.jarena;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Recolhe estatisticas sobre a arena, como populacao existente, numero de
 * batalhas vencidas, etc.
 */
public class Estatistico {
	public static final int TOTAL_DIVISOES = 0;
	public static final int MAX_POPULACAO = 1;
	public static final int MAX_ENERGIA_TOTAL = 2;
	public static final int MAX_ENERGIA_AGENTE = 3;
	public static final int MAX_TEMPO_VIDA = 4;

	private static final int MAX_ESTATISTICAS = 5;

	private Arena arena;
	private HashMap<String, long[]> infos;
	private long ultimoUpdate;

	public Estatistico(Arena a) {
		arena = a;
		infos = new HashMap<String, long[]>();
		ultimoUpdate = 0;
	}

	public void colheEstatisticas() {
		Agente a;
		HashMap<String, long[]> tempInfos = new HashMap<String, long[]>();
		long[] nums;
		long[] numsTemp;

		for(Entidade e : arena.getEntidades()) {
			if(e instanceof Agente) {
				a = (Agente) e;
				numsTemp = getInfoEquipe(a.getEquipe(), tempInfos);

				numsTemp[MAX_POPULACAO]++;
				numsTemp[MAX_ENERGIA_TOTAL] += a.getEnergia();

				if(a.getEnergia() > numsTemp[MAX_ENERGIA_AGENTE]) {
					numsTemp[MAX_ENERGIA_AGENTE] = a.getEnergia();
				}

				if (arena.isModoHeadless()) {
					numsTemp[MAX_TEMPO_VIDA] = arena.getTurnoAtual();
				} else {
					numsTemp[MAX_TEMPO_VIDA] = Calendar.getInstance().getTimeInMillis();
				}
			}
		}

		Set<String> chaves = tempInfos.keySet();

		for (String chave : chaves) {
			nums = getInfoEquipe(chave, infos);
			numsTemp = getInfoEquipe(chave, tempInfos);

			for(int i = 0; i < nums.length; i++) {
				if(numsTemp[i] > nums[i]) {
					nums[i] = numsTemp[i];
				}
			}
		}
	}

	public void contabilizaDivisao(Agente a) {
		long[] nums = getInfoEquipe(a.getEquipe(), infos);
		nums[TOTAL_DIVISOES]++;
	}

	private long[] getInfoEquipe(String nome, HashMap<String, long[]> infos) {
		long[] nums = infos.get(nome);

		if(nums == null) {
			nums = new long[MAX_ESTATISTICAS];
			infos.put(nome, nums);
		}

		return nums;
	}

	public String getNomeFromIdEstatistica(int id) {
		String nome;

		switch(id) {
			case TOTAL_DIVISOES:
				nome = "Divisoes";
				break;
			case MAX_POPULACAO:
				nome = "Populacao max.";
				break;
			case MAX_ENERGIA_TOTAL:
				nome = "Energia max. (equipe)";
				break;
			case MAX_ENERGIA_AGENTE:
				nome = "Energia max. (agente)";
				break;
			case MAX_TEMPO_VIDA:
				if (arena.isModoHeadless()) {
					nome = "Tempo de vida (turnos)";
				} else {
					nome = "Tempo de vida";
				}
				break;
			default:
				nome = "(desconhecido)";
				break;
		}

		return nome;
	}

	public long getValorEquipe(String equipe, int idEstatistica) {
		long[] nums = infos.get(equipe);

		if (nums == null || idEstatistica < 0 || idEstatistica >= MAX_ESTATISTICAS) {
			return 0;
		}

		if (idEstatistica == MAX_TEMPO_VIDA && arena.isModoHeadless() == false) {
			return Math.max(0, nums[idEstatistica] - arena.getTimestampInicio());
		}

		return nums[idEstatistica];
	}

	public Set<String> getEquipes() {
		return infos.keySet();
	}

	public String montaResumoTreino() {
		StringBuilder resumo = new StringBuilder();

		for (String equipe : infos.keySet()) {
			resumo.append("TEAM=").append(equipe)
				.append(";DIVISOES=").append(getValorEquipe(equipe, TOTAL_DIVISOES))
				.append(";POP_MAX=").append(getValorEquipe(equipe, MAX_POPULACAO))
				.append(";ENERGIA_EQUIPE_MAX=").append(getValorEquipe(equipe, MAX_ENERGIA_TOTAL))
				.append(";ENERGIA_AGENTE_MAX=").append(getValorEquipe(equipe, MAX_ENERGIA_AGENTE))
				.append(";TEMPO_VIDA_MS=").append(getValorEquipe(equipe, MAX_TEMPO_VIDA))
				.append("\n");
		}

		return resumo.toString();
	}

	public void imprimeEstatisticas() {
		Set<String> chaves = infos.keySet();
		long[] nums;
		long tempo;
		String infoTempo;

		System.out.println("\n\nEstatisticas");
		System.out.println("---------------------------------------\n");

		for (String chave : chaves) {
			nums = infos.get(chave);

			System.out.println("Equipe " + chave);

			for(int i = 0; i < nums.length; i++) {
				if(i == MAX_TEMPO_VIDA && arena.isModoHeadless() == false) {
					tempo = nums[i] - arena.getTimestampInicio();
					infoTempo = String.format("%d min, %d seg",
							TimeUnit.MILLISECONDS.toMinutes(tempo),
							TimeUnit.MILLISECONDS.toSeconds(tempo) -
							TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(tempo))
					);
					System.out.println("\t" + getNomeFromIdEstatistica(i) + ": " + infoTempo);
				} else {
					System.out.println("\t" + getNomeFromIdEstatistica(i) + ": " + getValorEquipe(chave, i));
				}
			}
			System.out.println();
		}
	}
}
