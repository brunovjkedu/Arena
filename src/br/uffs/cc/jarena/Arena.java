/**
 * Descreve uma arena para combate entre agentes. A arena contem uma lista com agentes
 * vivos e uma lista com os pontos de energia. Em cada iteracao, todos os agentes vivos
 * sao processados.
 *
 * Fernando Bevilacqua <fernando.bevilacqua@uffs.edu.br>
 */

package br.uffs.cc.jarena;

import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;
import java.util.Vector;

import br.uffs.cc.jarena.renders.simple2d.*;

public class Arena implements Runnable {
	private Vector<Entidade> entidades;
	private Vector<Entidade> nascendo;
	private Vector<Entidade> morrendo;
	private Desenhista desenhista;
	private Estatistico estatistico;
	private Teclado teclado;
	private long ultimoUpdate;
	private long horaInicio;
	private long turnoAtual;
	private boolean ativa;
	private long intervaloUpdate;
	private boolean debug;
	private boolean modoHeadless;
	private boolean modoSilencioso;
	private String classeEquipe;
	private String classeAdversario;

	public Arena() {
		this(false);
	}

	public Arena(boolean headless) {
		modoHeadless = headless;
		modoSilencioso = headless;
		classeEquipe = System.getProperty("jarena.classeEquipe", "br.uffs.cc.jarena.AgenteDuplaArena");
		classeAdversario = System.getProperty("jarena.classeAdversario", "br.uffs.cc.jarena.AgenteInimigo");

		criaAmbiente();
		adicionaPontosEnergia();
		adicionaAgentes();
		initTela();
	}

	private void initTela() {
		if (modoHeadless) {
			this.desenhista = new DesenhistaNulo();
		} else {
			this.desenhista = new DesenhistaSimples2D();
		}

		this.desenhista.init(this, teclado);
	}

	private void criaAmbiente() {
		ativa = true;
		ultimoUpdate = 0;
		turnoAtual = 0;
		intervaloUpdate = Constants.INTERVALO_UPDATE;
		entidades = new Vector<Entidade>();
		nascendo = new Vector<Entidade>();
		morrendo = new Vector<Entidade>();
		estatistico = new Estatistico(this);
		teclado = new Teclado();
	}

	private void adicionaAgentes() {
		int i;

		for (i = 0; i < 15; i++) {
			adicionaEntidade(criaEntidade(classeEquipe, geraXInicialEquipe(), geraYInicialAgente(), Constants.ENTIDADE_ENERGIA_INICIAL));
			adicionaEntidade(criaEntidade(classeAdversario, geraXInicialAdversario(), geraYInicialAgente(), Constants.ENTIDADE_ENERGIA_INICIAL));
		}
	}

	private int geraXInicialEquipe() {
		return sorteiaCoordenada(0, (int)(Constants.LARGURA_MAPA * 0.25));
	}

	private int geraXInicialAdversario() {
		return sorteiaCoordenada((int)(Constants.LARGURA_MAPA * 0.75), Constants.LARGURA_MAPA);
	}

	private int geraYInicialAgente() {
		return sorteiaCoordenada(0, Constants.ALTURA_MAPA);
	}

	private int sorteiaCoordenada(int minimo, int maximo) {
		return minimo + (int)(Math.random() * Math.max(1, maximo - minimo));
	}

	private Entidade criaEntidade(String nomeClasse, Integer x, Integer y, Integer energia) {
		try {
			Class[] argsConstrutor = new Class[] { Integer.class, Integer.class, Integer.class };
			Class<? extends Entidade> classe = Class.forName(nomeClasse).asSubclass(Entidade.class);
			Constructor<? extends Entidade> construtor = classe.getConstructor(argsConstrutor);

			return construtor.newInstance(x, y, energia);
		} catch (Exception e) {
			if (modoSilencioso == false) {
				System.out.println("Erro ao criar entidade " + nomeClasse + ": " + e.getMessage());
			}

			return new AgenteDummy(x, y, energia);
		}
	}

	private void adicionaPontosEnergia() {
		double rand;
		int i, j;

		j = Constants.ALTURA_TELA / Constants.PONTO_ENERGIA_QUANTIDADE;

		for (i = 0; i < Constants.PONTO_ENERGIA_QUANTIDADE; i++) {
			rand = Math.random();
			adicionaEntidade(new PontoEnergia((int) (Constants.LARGURA_TELA * 0.95 * rand), j * i, Constants.PONTO_ENERGIA_SUPRIMENTO_INICIAL));
		}
	}

	public Vector<Entidade> getEntidades() {
		return this.entidades;
	}

	public Desenhista getDesenhista() {
		return this.desenhista;
	}

	public Estatistico getEstatistico() {
		return this.estatistico;
	}

	public long getTurnoAtual() {
		return this.turnoAtual;
	}

	public boolean isModoHeadless() {
		return this.modoHeadless;
	}

	public boolean isModoSilencioso() {
		return this.modoSilencioso;
	}

	public void agendaNascimento(Entidade e) {
		this.nascendo.add(e);
	}

	public void agendaMorte(Entidade e) {
		if (modoSilencioso == false) {
			System.out.println("[MORTE] " + e);
		}

		this.morrendo.add(e);
	}

	public void run() {
		long agora;

		horaInicio = System.currentTimeMillis();

		while (ativa) {
			agora = System.currentTimeMillis();

			if ((agora - ultimoUpdate) >= intervaloUpdate) {
				executaTurno();
				ultimoUpdate = agora;
			}

			processaTeclado();
			estatistico.colheEstatisticas();
			desenhista.render();
		}

		estatistico.imprimeEstatisticas();
		desenhista.terminate();
	}

	public void runHeadless(int maxTurnos) {
		int turno;

		horaInicio = System.currentTimeMillis();

		for (turno = 0; turno < maxTurnos && ativa; turno++) {
			executaTurno();
			estatistico.colheEstatisticas();
		}
	}

	public void termina() {
		ativa = false;
	}

	private void processaTeclado() {
		if (modoHeadless) {
			return;
		}

		if (teclado.isKeyDown(KeyEvent.VK_UP)) {
			intervaloUpdate -= Constants.INTERVALO_UPDATE_INCREMENTO;
		} else if (teclado.isKeyDown(KeyEvent.VK_DOWN)) {
			intervaloUpdate += Constants.INTERVALO_UPDATE_INCREMENTO;
		} else if (teclado.isKeyDown(KeyEvent.VK_RIGHT) || teclado.isKeyDown(KeyEvent.VK_LEFT)) {
			intervaloUpdate = Constants.INTERVALO_UPDATE;
		}

		if (teclado.isKeyDown(KeyEvent.VK_Q) || teclado.isKeyDown(KeyEvent.VK_ESCAPE)) {
			termina();
		}

		if (teclado.isKeyDown(KeyEvent.VK_D)) {
			debug = true;
		} else {
			debug = false;
		}
	}

	private void executaTurno() {
		turnoAtual++;

		for (Entidade e : entidades) {
			if (!e.isMorta()) {
				try {
					e.update();
				} catch (Exception exp) {
					if (modoSilencioso == false) {
						System.out.println("*** EXCECAO em entidade *** Quem = " + e);
						exp.printStackTrace();
					}
				}
			}
		}

		if (nascendo.size() > 0) {
			for (Entidade j : nascendo) {
				adicionaEntidade(j);
			}
			nascendo.removeAllElements();
		}

		if (morrendo.size() > 0) {
			entidades.removeAll(morrendo);
			morrendo.removeAllElements();
		}

		if (isFimCombate()) {
			termina();
		}
	}

	private boolean isFimCombate() {
		boolean temAlguemNascendo = nascendo.size() > 0;
		boolean temAgentes = false;

		for (Entidade a : entidades) {
			if (a instanceof Agente && !a.isMorta()) {
				temAgentes = true;
				break;
			}
		}

		return !temAgentes && !temAlguemNascendo;
	}

	public void adicionaEntidade(Entidade e) {
		e.setArena(this);
		entidades.add(e);

		if (modoSilencioso == false) {
			System.out.println("[NASCEU] " + e);
		}
	}

	public void removeEntidade(Entidade entidade) {
		agendaMorte(entidade);
	}

	public void divideEntidade(Entidade entidade) {
		try {
			Class[] argsConstrutor = new Class[] { Integer.class, Integer.class, Integer.class };

			Class<? extends Entidade> classe = entidade.getClass();
			Constructor<? extends Entidade> construtor = classe.getConstructor(argsConstrutor);

			Entidade nova = construtor.newInstance(entidade.getX(), entidade.getY(), entidade.getEnergia());
			agendaNascimento(nova);

			if(entidade instanceof Agente) {
				estatistico.contabilizaDivisao((Agente)entidade);
				desenhista.agenteClonou((Agente)entidade, (Agente)nova);
			}
		} catch (Exception e) {
			if (modoSilencioso == false) {
				System.out.println("Erro na hora de dividir a entidade!" + e.getMessage());
			}
		}
	}

	public long getTimestampInicio() {
		return horaInicio;
	}

	public boolean isDebug() {
		return debug;
	}
}
