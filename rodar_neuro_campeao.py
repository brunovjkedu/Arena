#!/usr/bin/env python3
import glob
import json
import subprocess
from pathlib import Path

ARQUIVO_MELHOR_EQUIPE = Path("neuro_melhor.json")
ARQUIVO_MELHOR_INIMIGO = Path("neuro_inimigo_melhor.json")
CLASSE_EQUIPE_PADRAO = "br.uffs.cc.jarena.AgenteNeuroArena"
CLASSE_INIMIGO_PADRAO = "br.uffs.cc.jarena.AgenteInimigo"


def compila_java():
    arquivos = []
    arquivos.extend(glob.glob("./src/br/uffs/cc/jarena/*.java"))
    arquivos.extend(glob.glob("./src/br/uffs/cc/jarena/renders/simple2d/*.java"))
    subprocess.run(["javac", *arquivos, "-d", "bin/"], check=True)


def le_json(caminho):
    if caminho.exists() == False:
        return None
    return json.loads(caminho.read_text())


def serializa_pesos(dados, nome_arquivo):
    if dados is None:
        return None

    pesos = dados.get("pesos", [])
    if len(pesos) == 0:
        raise ValueError(f"{nome_arquivo} nao tem pesos salvos.")

    return ",".join(str(peso) for peso in pesos)


def carrega_campeoes():
    equipe = le_json(ARQUIVO_MELHOR_EQUIPE)
    inimigo = le_json(ARQUIVO_MELHOR_INIMIGO)

    if equipe is None:
        raise FileNotFoundError("Arquivo neuro_melhor.json nao encontrado. Rode ./otimizar_neuro_agente.py antes.")

    classe_equipe = equipe.get("classeEquipe", CLASSE_EQUIPE_PADRAO)
    classe_adversario = CLASSE_INIMIGO_PADRAO

    if inimigo is not None:
        classe_adversario = inimigo.get("classeAdversario", "br.uffs.cc.jarena.AgenteNeuroInimigo")
    else:
        classe_adversario = CLASSE_INIMIGO_PADRAO

    pesos_equipe = serializa_pesos(equipe, "neuro_melhor.json")
    pesos_inimigo = serializa_pesos(inimigo, "neuro_inimigo_melhor.json")

    return classe_equipe, classe_adversario, pesos_equipe, pesos_inimigo


def main():
    print("Compilando Java...")
    compila_java()

    classe_equipe, classe_adversario, pesos_equipe, pesos_inimigo = carrega_campeoes()

    comando = [
        "java",
        f"-Djarena.classeEquipe={classe_equipe}",
        f"-Djarena.classeAdversario={classe_adversario}",
        f"-Dneuro.equipePesos={pesos_equipe}",
        "br.uffs.cc.jarena.Main",
    ]

    if pesos_inimigo is not None:
        comando.insert(-1, f"-Dneuro.adversarioPesos={pesos_inimigo}")

    print("Abrindo Arena com os campeoes neurais...")
    subprocess.run(comando, check=True, cwd="bin")


if __name__ == "__main__":
    main()
