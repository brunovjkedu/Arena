package br.uffs.cc.jarena;

/**
 * Agente neural da equipe DuplaArena.
 */
public class AgenteNeuroArena extends AgenteNeuroBase {
	public AgenteNeuroArena(Integer x, Integer y, Integer energia) {
		super(x, y, energia, "DuplaArena", "neuro.equipePesos", "neuro.pesos");
	}
}
