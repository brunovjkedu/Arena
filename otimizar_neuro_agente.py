#!/usr/bin/env python3
import glob
import json
import random
import subprocess
from pathlib import Path

CLASSE_EQUIPE = "br.uffs.cc.jarena.AgenteNeuroArena"
CLASSE_ADVERSARIO = "br.uffs.cc.jarena.AgenteInimigo"

ENTRADAS = 12
OCULTOS = 8
SAIDAS = 6
TOTAL_PESOS = ENTRADAS * OCULTOS + OCULTOS + OCULTOS * SAIDAS + SAIDAS

POPULACAO = 20
GERACOES = 12
ELITE = 5
REPETICOES_POR_INDIVIDUO = 3
MAX_TURNOS = 5000
MUTACAO_PROB = 0.25
MUTACAO_SIGMA = 0.35
ARQUIVO_MELHOR = Path("neuro_melhor.json")


def compila_java():
    arquivos = []
    arquivos.extend(glob.glob("./src/br/uffs/cc/jarena/*.java"))
    arquivos.extend(glob.glob("./src/br/uffs/cc/jarena/renders/simple2d/*.java"))
    subprocess.run(["javac", *arquivos, "-d", "bin/"], check=True)


def individuo_aleatorio():
    return [random.uniform(-1.0, 1.0) for _ in range(TOTAL_PESOS)]


def cruza(pai, mae):
    filho = []
    for wp, wm in zip(pai, mae):
        if random.random() < 0.5:
            filho.append(wp)
        else:
            filho.append(wm)
    return filho


def muta(individuo):
    novo = []
    for peso in individuo:
        if random.random() < MUTACAO_PROB:
            peso += random.gauss(0.0, MUTACAO_SIGMA)
        novo.append(max(-3.0, min(3.0, peso)))
    return novo


def serializa_pesos(individuo):
    return ",".join(f"{peso:.6f}" for peso in individuo)


def parse_saida(texto):
    metricas = {}
    for linha in texto.splitlines():
        if not linha.startswith("TEAM="):
            continue

        valores = {}
        for item in linha.split(";"):
            chave, valor = item.split("=", 1)
            valores[chave] = valor

        metricas[valores["TEAM"]] = {
            "divisoes": int(valores["DIVISOES"]),
            "pop_max": int(valores["POP_MAX"]),
            "energia_equipe_max": int(valores["ENERGIA_EQUIPE_MAX"]),
            "energia_agente_max": int(valores["ENERGIA_AGENTE_MAX"]),
            "tempo_vida": int(valores["TEMPO_VIDA_MS"]),
        }
    return metricas


def simula(individuo):
    comando = [
        "java",
        f"-Djarena.maxTurnos={MAX_TURNOS}",
        f"-Djarena.classeEquipe={CLASSE_EQUIPE}",
        f"-Djarena.classeAdversario={CLASSE_ADVERSARIO}",
        f"-Dneuro.pesos={serializa_pesos(individuo)}",
        "-cp",
        "bin",
        "br.uffs.cc.jarena.MainTreino",
    ]

    saida = subprocess.run(
        comando,
        check=True,
        capture_output=True,
        text=True,
    ).stdout

    return parse_saida(saida)


def fitness(individuo):
    pontuacoes = []

    for _ in range(REPETICOES_POR_INDIVIDUO):
        metricas = simula(individuo)
        nossa = metricas.get("DuplaArena", {})
        inimigo = metricas.get("Inimigo", {})

        tempo_nosso = nossa.get("tempo_vida", 0)
        tempo_inimigo = inimigo.get("tempo_vida", 0)
        energia_nossa = nossa.get("energia_equipe_max", 0)
        pop_nossa = nossa.get("pop_max", 0)

        score = tempo_nosso * 10
        score += max(0, tempo_nosso - tempo_inimigo) * 20
        score += energia_nossa // 10
        score += pop_nossa * 15
        pontuacoes.append(score)

    return sum(pontuacoes) / len(pontuacoes)


def avalia_populacao(populacao):
    avaliados = []

    for i, individuo in enumerate(populacao, start=1):
        score = fitness(individuo)
        avaliados.append((score, individuo))
        print(f"  individuo {i:02d}: fitness={score:.2f}")

    avaliados.sort(key=lambda item: item[0], reverse=True)
    return avaliados


def salva_melhor(score, individuo):
    ARQUIVO_MELHOR.write_text(json.dumps({
        "fitness": score,
        "pesos": individuo,
        "classeEquipe": CLASSE_EQUIPE,
        "classeAdversario": CLASSE_ADVERSARIO,
        "totalPesos": TOTAL_PESOS,
    }, indent=2))


def proxima_geracao(avaliados):
    elite = [individuo for _, individuo in avaliados[:ELITE]]
    nova = list(elite)

    while len(nova) < POPULACAO:
        pai = random.choice(elite)
        mae = random.choice(elite)
        nova.append(muta(cruza(pai, mae)))

    return nova


def main():
    random.seed(42)
    print("Compilando Java...")
    compila_java()

    populacao = [individuo_aleatorio() for _ in range(POPULACAO)]
    melhor_global_score = None
    melhor_global_individuo = None

    for geracao in range(1, GERACOES + 1):
        print(f"\n=== Geracao {geracao} ===")
        avaliados = avalia_populacao(populacao)
        melhor_score, melhor_individuo = avaliados[0]
        print(f"Melhor da geracao {geracao}: fitness={melhor_score:.2f}")

        if melhor_global_score is None or melhor_score > melhor_global_score:
            melhor_global_score = melhor_score
            melhor_global_individuo = list(melhor_individuo)
            salva_melhor(melhor_global_score, melhor_global_individuo)
            print(f"Novo melhor global salvo em {ARQUIVO_MELHOR}")

        populacao = proxima_geracao(avaliados)

    print("\n=== Melhor final ===")
    print(f"fitness={melhor_global_score:.2f}")
    print(f"arquivo={ARQUIVO_MELHOR}")


if __name__ == "__main__":
    main()
