#!/usr/bin/env python3
import glob
import random
import subprocess

PARAMETROS = {
    "duplaarena.turnosAviso": (0, 40),
    "duplaarena.distanciaAlvo": (0, 30),
    "duplaarena.tentativasDirecao": (1, 20),
}

POPULACAO = 16
GERACOES = 8
ELITE = 4
MUTACAO_PROB = 0.35
REPETICOES_POR_INDIVIDUO = 3
MAX_TURNOS = 6000


def individuo_aleatorio():
    return {
        nome: random.randint(intervalo[0], intervalo[1])
        for nome, intervalo in PARAMETROS.items()
    }


def muta(individuo):
    novo = dict(individuo)
    for nome, (minimo, maximo) in PARAMETROS.items():
        if random.random() < MUTACAO_PROB:
            passo = random.randint(-4, 4)
            novo[nome] = max(minimo, min(maximo, novo[nome] + passo))
    return novo


def cruza(pai, mae):
    return {
        nome: pai[nome] if random.random() < 0.5 else mae[nome]
        for nome in PARAMETROS
    }


def parse_saida(texto):
    metricas = {}
    for linha in texto.splitlines():
        if not linha.startswith("TEAM="):
            continue
        partes = {}
        for item in linha.split(";"):
            chave, valor = item.split("=", 1)
            partes[chave] = valor
        metricas[partes["TEAM"]] = {
            "divisoes": int(partes["DIVISOES"]),
            "pop_max": int(partes["POP_MAX"]),
            "energia_equipe_max": int(partes["ENERGIA_EQUIPE_MAX"]),
            "energia_agente_max": int(partes["ENERGIA_AGENTE_MAX"]),
            "tempo_vida_ms": int(partes["TEMPO_VIDA_MS"]),
        }
    return metricas


def simula(individuo):
    comando = ["java", f"-Djarena.maxTurnos={MAX_TURNOS}"]

    for nome, valor in individuo.items():
        comando.append(f"-D{nome}={valor}")

    comando.extend(["-cp", "bin", "br.uffs.cc.jarena.MainTreino"])

    saida = subprocess.run(
        comando,
        check=True,
        capture_output=True,
        text=True,
    ).stdout

    return parse_saida(saida)


def fitness(individuo):
    pontos = []

    for _ in range(REPETICOES_POR_INDIVIDUO):
        metricas = simula(individuo)
        nossa = metricas.get("DuplaArena", {})
        inimigo = metricas.get("Inimigo", {})

        tempo_nosso = nossa.get("tempo_vida_ms", 0)
        tempo_inimigo = inimigo.get("tempo_vida_ms", 0)
        energia_nossa = nossa.get("energia_equipe_max", 0)
        pop_nossa = nossa.get("pop_max", 0)

        score = tempo_nosso
        score += max(0, tempo_nosso - tempo_inimigo) * 2
        score += energia_nossa // 20
        score += pop_nossa * 10
        pontos.append(score)

    return sum(pontos) / len(pontos)


def avalia_populacao(populacao):
    avaliados = []

    for i, individuo in enumerate(populacao, start=1):
        score = fitness(individuo)
        avaliados.append((score, individuo))
        print(f"  individuo {i:02d}: fitness={score:.2f} params={individuo}")

    avaliados.sort(key=lambda item: item[0], reverse=True)
    return avaliados


def proxima_geracao(avaliados):
    elite = [individuo for _, individuo in avaliados[:ELITE]]
    nova = list(elite)

    while len(nova) < POPULACAO:
        pai = random.choice(elite)
        mae = random.choice(elite)
        filho = muta(cruza(pai, mae))
        nova.append(filho)

    return nova


def compila_java():
    arquivos = []
    arquivos.extend(glob.glob("./src/br/uffs/cc/jarena/*.java"))
    arquivos.extend(glob.glob("./src/br/uffs/cc/jarena/renders/simple2d/*.java"))

    subprocess.run(["javac", *arquivos, "-d", "bin/"], check=True)


def main():
    random.seed(42)

    print("Compilando Java...")
    compila_java()

    populacao = [individuo_aleatorio() for _ in range(POPULACAO)]

    for geracao in range(1, GERACOES + 1):
        print(f"\n=== Geracao {geracao} ===")
        avaliados = avalia_populacao(populacao)
        melhor_score, melhor = avaliados[0]
        print(f"Melhor da geracao {geracao}: fitness={melhor_score:.2f} params={melhor}")
        populacao = proxima_geracao(avaliados)

    print("\n=== Melhor final ===")
    avaliados = avalia_populacao(populacao)
    melhor_score, melhor = avaliados[0]
    print(f"fitness={melhor_score:.2f}")
    print(f"params={melhor}")


if __name__ == "__main__":
    main()
