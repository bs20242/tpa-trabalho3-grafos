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
        ListaEncadeada<T> valores = grafo.obterVertices();
        T inicio = valores.pesquisar(origem);
        if (inicio == null) throw new IllegalArgumentException("A origem nao esta no grafo.");

        int n = valores.quantidadeNos();
        float[][] pesos = new float[n][n];
        int indiceOrigem = 0;
        int i = 0;
        // Consulta as listas uma vez e guarda os pesos para a busca recursiva.
        for (T de : valores) {
            if (de == inicio) indiceOrigem = i;
            int j = 0;
            for (T para : valores) pesos[i][j++] = grafo.pesoEntre(de, para);
            i++;
        }
        Busca busca = new Busca(pesos, indiceOrigem);
        busca.visitar(indiceOrigem, 1, 0);
        if (busca.melhorCaminho == null) return null;

        ListaEncadeada<T> percurso = new ListaEncadeada<>(grafo.getComparador(), false);
        // A lista insere no inicio: copiar de tras para frente preserva a rota.
        for (int posicao = n; posicao >= 0; posicao--) {
            int indice = 0;
            for (T valor : valores) {
                if (indice++ == busca.melhorCaminho[posicao]) {
                    percurso.adicionar(valor);
                    break;
                }
            }
        }
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
                    visitados[proximo] = true;
                    caminho[quantidade] = proximo;
                    visitar(proximo, quantidade + 1, distancia + pesos[atual][proximo]);
                    // Desfaz a escolha para tentar outra ordem de visita.
                    visitados[proximo] = false;
                }
            }
        }
    }
}
