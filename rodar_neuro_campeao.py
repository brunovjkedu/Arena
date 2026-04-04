#!/usr/bin/env python3
import glob
import json
import subprocess
from pathlib import Path

ARQUIVO_MELHOR = Path("neuro_melhor.json")


def compila_java():
    arquivos = []
    arquivos.extend(glob.glob("./src/br/uffs/cc/jarena/*.java"))
    arquivos.extend(glob.glob("./src/br/uffs/cc/jarena/renders/simple2d/*.java"))
    subprocess.run(["javac", *arquivos, "-d", "bin/"], check=True)


def carrega_campeao():
    if ARQUIVO_MELHOR.exists() == False:
        raise FileNotFoundError("Arquivo neuro_melhor.json nao encontrado. Rode ./otimizar_neuro_agente.py antes.")

    dados = json.loads(ARQUIVO_MELHOR.read_text())
    pesos = dados.get("pesos", [])
    classe_equipe = dados.get("classeEquipe", "br.uffs.cc.jarena.AgenteNeuroArena")
    classe_adversario = dados.get("classeAdversario", "br.uffs.cc.jarena.AgenteInimigo")

    if len(pesos) == 0:
        raise ValueError("neuro_melhor.json nao tem pesos salvos.")

    return classe_equipe, classe_adversario, ",".join(str(peso) for peso in pesos)


def main():
    print("Compilando Java...")
    compila_java()

    classe_equipe, classe_adversario, pesos = carrega_campeao()

    comando = [
        "java",
        f"-Djarena.classeEquipe={classe_equipe}",
        f"-Djarena.classeAdversario={classe_adversario}",
        f"-Dneuro.pesos={pesos}",
        "br.uffs.cc.jarena.Main",
    ]

    print("Abrindo Arena com o agente neural campeao...")
    subprocess.run(comando, check=True, cwd="bin")


if __name__ == "__main__":
    main()
