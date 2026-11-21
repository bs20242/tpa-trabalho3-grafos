package grafos;

import colecao.ListaEncadeada;

/** Menor caminho entre dois locais usando pesos nao negativos (Dijkstra). */
public class Dijkstra {
    /** Retorna null quando nao existe caminho ligando origem e destino. */
    public static <T> Rota<T> calcular(Grafo<T> grafo, T origem, T destino) {
        if (grafo == null) throw new IllegalArgumentException("Informe um grafo.");
        int indiceOrigem = grafo.indiceDe(origem);
        if (indiceOrigem < 0) throw new IllegalArgumentException("A origem nao esta no grafo.");
        int indiceDestino = grafo.indiceDe(destino);
        if (indiceDestino < 0) throw new IllegalArgumentException("O destino nao esta no grafo.");

        float[][] pesos = grafo.matrizPesos();
        int n = pesos.length;
        double[] distancias = new double[n];
        int[] predecessores = new int[n];
        boolean[] rotulados = new boolean[n];

        for (int i = 0; i < n; i++) {
            distancias[i] = Double.POSITIVE_INFINITY;
            predecessores[i] = -1;
        }
        distancias[indiceOrigem] = 0;

        for (int passo = 0; passo < n; passo++) {
            int atual = proximoMaisProximo(distancias, rotulados);
            if (atual == -1) break;
            rotulados[atual] = true;
            if (atual == indiceDestino) break;

            for (int vizinho = 0; vizinho < n; vizinho++) {
                if (!rotulados[vizinho] && Float.isFinite(pesos[atual][vizinho])) {
                    double estimativa = distancias[atual] + pesos[atual][vizinho];
                    if (estimativa < distancias[vizinho]) {
                        distancias[vizinho] = estimativa;
                        predecessores[vizinho] = atual;
                    }
                }
            }
        }

        if (!Double.isFinite(distancias[indiceDestino])) return null;

        int[] invertido = new int[n];
        int quantidade = 0;
        for (int cursor = indiceDestino; cursor != -1; cursor = predecessores[cursor]) {
            invertido[quantidade++] = cursor;
        }
        int[] caminho = new int[quantidade];
        for (int i = 0; i < quantidade; i++) {
            caminho[i] = invertido[quantidade - 1 - i];
        }

        ListaEncadeada<T> percurso = grafo.montarSequencia(caminho, quantidade);
        return new Rota<>(percurso, distancias[indiceDestino]);
    }

    private static int proximoMaisProximo(double[] distancias, boolean[] rotulados) {
        int escolhido = -1;
        double menor = Double.POSITIVE_INFINITY;
        for (int i = 0; i < distancias.length; i++) {
            if (!rotulados[i] && distancias[i] < menor) {
                menor = distancias[i];
                escolhido = i;
            }
        }
        return escolhido;
    }
}
