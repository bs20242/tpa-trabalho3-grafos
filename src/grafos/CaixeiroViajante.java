package grafos;

import colecao.ListaEncadeada;

/** Busca exata por ordens de visita. Indicada apenas para conjuntos pequenos. */
public class CaixeiroViajante {
    public static final int LIMITE_LOCAIS = 10;

    /** Retorna null se nao houver uma rota que visite todos e volte a origem. */
    public static <T> Rota<T> calcular(Grafo<T> grafo, T origem) {
        if (grafo == null) throw new IllegalArgumentException("Informe um grafo.");
        if (grafo.quantidadeVertices() > LIMITE_LOCAIS) {
            throw new IllegalArgumentException("A busca exata aceita ate 10 locais, incluindo a loja.");
        }
        int indiceOrigem = grafo.indiceDe(origem);
        if (indiceOrigem < 0) throw new IllegalArgumentException("A origem nao esta no grafo.");

        float[][] pesos = grafo.matrizPesos();
        Busca busca = new Busca(pesos, indiceOrigem);
        busca.visitar(indiceOrigem, 1, 0);
        if (busca.melhorCaminho == null) return null;

        ListaEncadeada<T> percurso = grafo.montarSequencia(busca.melhorCaminho, pesos.length + 1);
        return new Rota<>(percurso, busca.menorDistancia);
    }

    private static class Busca {
        private final float[][] pesos;
        private final int origem;
        private final boolean[] visitados;
        private final int[] caminho;
        private int[] melhorCaminho;
        private double menorDistancia = Double.POSITIVE_INFINITY;

        private Busca(float[][] pesos, int origem) {
            this.pesos = pesos;
            this.origem = origem;
            this.visitados = new boolean[pesos.length];
            this.caminho = new int[pesos.length + 1];
            caminho[0] = origem;
            visitados[origem] = true;
        }

        private void visitar(int atual, int quantidade, double distancia) {
            if (quantidade == pesos.length) {
                // Com apenas a loja, nao existe deslocamento a fazer.
                double volta = pesos.length == 1 ? 0 : pesos[atual][origem];
                double total = distancia + volta;
                if (total < menorDistancia) {
                    menorDistancia = total;
                    caminho[quantidade] = origem;
                    melhorCaminho = caminho.clone();
                }
                return;
            }

            for (int proximo = 0; proximo < pesos.length; proximo++) {
                if (!visitados[proximo] && Float.isFinite(pesos[atual][proximo])) {
                    double novaDistancia = distancia + pesos[atual][proximo];
                    // Como os pesos sao nao negativos, um caminho parcial ja maior ou igual nao melhora a resposta.
                    if (novaDistancia >= menorDistancia) continue;
                    visitados[proximo] = true;
                    caminho[quantidade] = proximo;
                    visitar(proximo, quantidade + 1, novaDistancia);
                    // Desfaz a escolha para tentar outra ordem de visita.
                    visitados[proximo] = false;
                }
            }
        }
    }
}
