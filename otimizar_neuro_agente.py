#!/usr/bin/env python3
import glob
import json
import random
import subprocess
from pathlib import Path

CLASSE_EQUIPE_NEURAL = "br.uffs.cc.jarena.AgenteNeuroArena"
CLASSE_EQUIPE_BASELINE = "br.uffs.cc.jarena.AgenteDuplaArena"
CLASSE_INIMIGO_NEURAL = "br.uffs.cc.jarena.AgenteNeuroInimigo"
CLASSE_INIMIGO_BASELINE = "br.uffs.cc.jarena.AgenteInimigo"

ENTRADAS = 12
OCULTOS = 8
SAIDAS = 6
TOTAL_PESOS = ENTRADAS * OCULTOS + OCULTOS + OCULTOS * SAIDAS + SAIDAS

POPULACAO = 16
GERACOES = 10
ELITE = 4
REPETICOES_POR_DUELO = 2
MAX_TURNOS = 5000
POOL_MAX = 4
MUTACAO_PROB = 0.25
MUTACAO_SIGMA = 0.35

ARQUIVO_MELHOR_EQUIPE = Path("neuro_melhor.json")
ARQUIVO_MELHOR_INIMIGO = Path("neuro_inimigo_melhor.json")


def compila_java():
    arquivos = []
    arquivos.extend(glob.glob("./src/br/uffs/cc/jarena/*.java"))
    arquivos.extend(glob.glob("./src/br/uffs/cc/jarena/renders/simple2d/*.java"))
    subprocess.run(["javac", *arquivos, "-d", "bin/"], check=True)


def individuo_aleatorio():
    return [random.uniform(-1.0, 1.0) for _ in range(TOTAL_PESOS)]


def cruza(pai, mae):
    return [wp if random.random() < 0.5 else wm for wp, wm in zip(pai, mae)]


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
            "energia_coletada": int(valores.get("ENERGIA_COLETADA", 0)),
            "vitorias": int(valores.get("VITORIAS", 0)),
            "passos": int(valores.get("PASSOS", 0)),
            "celulas_visitadas": int(valores.get("CELULAS_VISITADAS", 0)),
        }
    return metricas


def simula(classe_equipe, pesos_equipe, classe_inimigo, pesos_inimigo):
    comando = [
        "java",
        f"-Djarena.maxTurnos={MAX_TURNOS}",
        f"-Djarena.classeEquipe={classe_equipe}",
        f"-Djarena.classeAdversario={classe_inimigo}",
    ]

    if pesos_equipe is not None:
        comando.append(f"-Dneuro.equipePesos={serializa_pesos(pesos_equipe)}")

    if pesos_inimigo is not None:
        comando.append(f"-Dneuro.adversarioPesos={serializa_pesos(pesos_inimigo)}")

    comando.extend(["-cp", "bin", "br.uffs.cc.jarena.MainTreino"])

    saida = subprocess.run(
        comando,
        check=True,
        capture_output=True,
        text=True,
    ).stdout

    return parse_saida(saida)


def pontua(metricas, equipe, rival):
    nossa = metricas.get(equipe, {})
    adversario = metricas.get(rival, {})

    tempo_nosso = nossa.get("tempo_vida", 0)
    tempo_rival = adversario.get("tempo_vida", 0)
    energia_coletada = nossa.get("energia_coletada", 0)
    vitorias = nossa.get("vitorias", 0)
    passos = nossa.get("passos", 0)
    celulas = nossa.get("celulas_visitadas", 0)
    energia_equipe = nossa.get("energia_equipe_max", 0)
    pop_max = nossa.get("pop_max", 0)

    score = tempo_nosso * 4
    score += max(0, tempo_nosso - tempo_rival) * 10
    score += energia_coletada * 3
    score += vitorias * 450
    score += passos * 1.2
    score += celulas * 120
    score += energia_equipe / 25
    score += pop_max * 8

    if tempo_nosso > tempo_rival:
        score += 3000

    if passos < 200:
        score -= (200 - passos) * 15

    if celulas < 10:
        score -= (10 - celulas) * 250

    return score


def fitness_equipe(individuo, pool_inimigos):
    scores = []
    for adversario in pool_inimigos:
        for _ in range(REPETICOES_POR_DUELO):
            metricas = simula(CLASSE_EQUIPE_NEURAL, individuo, adversario["classe"], adversario["pesos"])
            scores.append(pontua(metricas, "DuplaArena", "Inimigo"))
    return sum(scores) / len(scores)


def fitness_inimigo(individuo, pool_equipes):
    scores = []
    for adversario in pool_equipes:
        for _ in range(REPETICOES_POR_DUELO):
            metricas = simula(adversario["classe"], adversario["pesos"], CLASSE_INIMIGO_NEURAL, individuo)
            scores.append(pontua(metricas, "Inimigo", "DuplaArena"))
    return sum(scores) / len(scores)


def avalia_populacao(populacao, funcao_fitness, pool_adversarios, rotulo):
    avaliados = []

    for i, individuo in enumerate(populacao, start=1):
        score = funcao_fitness(individuo, pool_adversarios)
        avaliados.append((score, individuo))
        print(f"  {rotulo} {i:02d}: fitness={score:.2f}")

    avaliados.sort(key=lambda item: item[0], reverse=True)
    return avaliados


def proxima_geracao(avaliados):
    elite = [individuo for _, individuo in avaliados[:ELITE]]
    nova = [list(individuo) for individuo in elite]

    while len(nova) < POPULACAO:
        pai = random.choice(elite)
        mae = random.choice(elite)
        nova.append(muta(cruza(pai, mae)))

    return nova


def salva_json(caminho, fitness, individuo, classe_equipe, classe_adversario):
    caminho.write_text(json.dumps({
        "fitness": fitness,
        "pesos": individuo,
        "classeEquipe": classe_equipe,
        "classeAdversario": classe_adversario,
        "totalPesos": TOTAL_PESOS,
    }, indent=2))


def atualiza_pool(pool, classe, individuo):
    pool.insert(1, {"classe": classe, "pesos": list(individuo)})
    del pool[POOL_MAX:]


def main():
    random.seed(42)
    print("Compilando Java...")
    compila_java()

    populacao_equipe = [individuo_aleatorio() for _ in range(POPULACAO)]
    populacao_inimigo = [individuo_aleatorio() for _ in range(POPULACAO)]

    pool_inimigos = [{"classe": CLASSE_INIMIGO_BASELINE, "pesos": None}]
    pool_equipes = [{"classe": CLASSE_EQUIPE_BASELINE, "pesos": None}]

    melhor_equipe_score = None
    melhor_equipe_individuo = None
    melhor_inimigo_score = None
    melhor_inimigo_individuo = None

    for geracao in range(1, GERACOES + 1):
        print(f"\n=== Geracao {geracao} / Evoluindo DuplaArena ===")
        avaliados_equipe = avalia_populacao(populacao_equipe, fitness_equipe, pool_inimigos, "equipe")
        score_equipe, individuo_equipe = avaliados_equipe[0]
        print(f"Melhor DuplaArena da geracao {geracao}: fitness={score_equipe:.2f}")

        if melhor_equipe_score is None or score_equipe > melhor_equipe_score:
            melhor_equipe_score = score_equipe
            melhor_equipe_individuo = list(individuo_equipe)
            salva_json(ARQUIVO_MELHOR_EQUIPE, melhor_equipe_score, melhor_equipe_individuo, CLASSE_EQUIPE_NEURAL, CLASSE_INIMIGO_NEURAL)
            print(f"Novo melhor DuplaArena salvo em {ARQUIVO_MELHOR_EQUIPE}")

        atualiza_pool(pool_equipes, CLASSE_EQUIPE_NEURAL, individuo_equipe)
        populacao_equipe = proxima_geracao(avaliados_equipe)

        print(f"\n=== Geracao {geracao} / Evoluindo Inimigo ===")
        avaliados_inimigo = avalia_populacao(populacao_inimigo, fitness_inimigo, pool_equipes, "inimigo")
        score_inimigo, individuo_inimigo = avaliados_inimigo[0]
        print(f"Melhor Inimigo da geracao {geracao}: fitness={score_inimigo:.2f}")

        if melhor_inimigo_score is None or score_inimigo > melhor_inimigo_score:
            melhor_inimigo_score = score_inimigo
            melhor_inimigo_individuo = list(individuo_inimigo)
            salva_json(ARQUIVO_MELHOR_INIMIGO, melhor_inimigo_score, melhor_inimigo_individuo, CLASSE_EQUIPE_NEURAL, CLASSE_INIMIGO_NEURAL)
            print(f"Novo melhor Inimigo salvo em {ARQUIVO_MELHOR_INIMIGO}")

        atualiza_pool(pool_inimigos, CLASSE_INIMIGO_NEURAL, individuo_inimigo)
        populacao_inimigo = proxima_geracao(avaliados_inimigo)

    print("\n=== Melhores finais ===")
    print(f"DuplaArena fitness={melhor_equipe_score:.2f} arquivo={ARQUIVO_MELHOR_EQUIPE}")
    print(f"Inimigo fitness={melhor_inimigo_score:.2f} arquivo={ARQUIVO_MELHOR_INIMIGO}")


if __name__ == "__main__":
    main()
