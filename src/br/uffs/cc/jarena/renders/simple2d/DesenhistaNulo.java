package br.uffs.cc.jarena.renders.simple2d;

import java.awt.event.KeyListener;

import br.uffs.cc.jarena.Agente;
import br.uffs.cc.jarena.Arena;
import br.uffs.cc.jarena.Desenhista;
import br.uffs.cc.jarena.Entidade;
import br.uffs.cc.jarena.PontoEnergia;

/**
 * Render sem interface grafica para rodar simulacoes de treino mais rapido.
 */
public class DesenhistaNulo implements Desenhista {
	public void init(Arena a, KeyListener k) {
	}

	public void render() {
	}

	public void terminate() {
	}

	public int getTamanho(Entidade e, int tipo) {
		if (e instanceof Agente) {
			return 32;
		}

		if (e instanceof PontoEnergia) {
			return tipo == LARGURA ? 50 : 60;
		}

		return 0;
	}

	public void agenteRecebeuEnergia(Agente a) {
	}

	public void agenteTomouDano(Agente a) {
	}

	public void agenteBateuAlguem(Agente batendo, Agente apanhando) {
	}

	public void agenteGanhouCombate(Agente a) {
	}

	public void agenteMorreu(Agente a) {
	}

	public void agenteMorreuPorExcecao(Agente a, Exception e) {
	}

	public void agenteClonou(Agente origem, Agente clone) {
	}

	public void agenteEnviouMensagem(Agente a, String msg) {
	}

	public void agenteRecebeuMensagem(Agente destinatario, Agente remetente) {
	}

	public void entidadeAdicionada(Entidade e) {
	}

	public void entidadeRemovida(Entidade e) {
	}
}
