package br.uffs.cc.jarena;

/**
 * Agente neural adversario da equipe Inimigo, usado na co-evolucao.
 */
public class AgenteNeuroInimigo extends AgenteNeuroBase {
	public AgenteNeuroInimigo(Integer x, Integer y, Integer energia) {
		super(x, y, energia, "Inimigo", "neuro.adversarioPesos", "neuro.pesos");
	}
}
