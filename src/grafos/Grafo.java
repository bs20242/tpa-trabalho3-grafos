package grafos;

import colecao.ListaEncadeada;
import colecao.No;
import java.util.Comparator;

/** Grafo nao direcionado: cada ligacao pode ser percorrida nos dois sentidos. */
public class Grafo<T> {
    private static class Vertice<T> {
        private final T valor;
        private final int indice;
        private final ListaEncadeada<Aresta<T>> vizinhos;

        private Vertice(T valor, int indice, Comparator<T> comparador) {
            this.valor = valor;
            this.indice = indice;
            this.vizinhos = new ListaEncadeada<>(
                (a, b) -> comparador.compare(a.destino.valor, b.destino.valor), false);
        }
    }

    private static class Aresta<T> {
        private final Vertice<T> destino;
        private final float peso;

        private Aresta(Vertice<T> destino, float peso) {
            this.destino = destino;
            this.peso = peso;
        }
    }

    private final Comparator<T> comparador;
    private final ListaEncadeada<Vertice<T>> vertices;
    private int totalArestas;

    public Grafo(Comparator<T> comparador) {
        if (comparador == null) throw new IllegalArgumentException("Informe um comparador.");
        this.comparador = comparador;
        this.vertices = new ListaEncadeada<>(
            (a, b) -> comparador.compare(a.valor, b.valor), false);
    }

    // O comparador define quando dois valores representam o mesmo local.
    public boolean adicionarVertice(T valor) {
        if (valor == null || localizarVertice(valor) != null) return false;
        vertices.adicionar(new Vertice<>(valor, vertices.quantidadeNos(), comparador));
        return true;
    }

    /** Retorna false para extremos ausentes, lacos ou ligacoes repetidas. */
    public boolean adicionarAresta(T origem, T destino, float peso) {
        if (!Float.isFinite(peso) || peso < 0) {
            throw new IllegalArgumentException("O peso deve ser finito e nao negativo.");
        }
        Vertice<T> inicio = localizarVertice(origem);
        Vertice<T> fim = localizarVertice(destino);
        if (inicio == null || fim == null || inicio == fim) return false;
        if (localizarAresta(inicio, fim) != null) return false;

        // A mesma ligacao aparece nas duas listas, mas conta como uma unica aresta.
        inicio.vizinhos.adicionar(new Aresta<>(fim, peso));
        fim.vizinhos.adicionar(new Aresta<>(inicio, peso));
        totalArestas++;
        return true;
    }

    public int quantidadeVertices() {
        return vertices.quantidadeNos();
    }

    public int quantidadeArestas() {
        return totalArestas;
    }

    Comparator<T> getComparador() {
        return comparador;
    }

    int indiceDe(T valor) {
        Vertice<T> vertice = localizarVertice(valor);
        return vertice == null ? -1 : vertice.indice;
    }

    float[][] matrizPesos() {
        int n = quantidadeVertices();
        float[][] matriz = new float[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matriz[i][j] = Float.POSITIVE_INFINITY;
            }
        }
        for (Vertice<T> vertice : vertices) {
            for (Aresta<T> aresta : vertice.vizinhos) {
                matriz[vertice.indice][aresta.destino.indice] = aresta.peso;
            }
        }
        return matriz;
    }

    ListaEncadeada<T> montarSequencia(int[] indices, int tamanho) {
        ListaEncadeada<T> sequencia = new ListaEncadeada<>(comparador, false);
        // A lista insere no inicio: copiar de tras para frente preserva a ordem.
        for (int i = tamanho - 1; i >= 0; i--) {
            sequencia.adicionar(valorDoIndice(indices[i]));
        }
        return sequencia;
    }

    private T valorDoIndice(int indice) {
        for (Vertice<T> vertice : vertices) {
            if (vertice.indice == indice) return vertice.valor;
        }
        throw new IllegalArgumentException("Indice invalido: " + indice);
    }

    /** Retorna infinito positivo quando nao existe ligacao direta entre os valores. */
    public float pesoEntre(T origem, T destino) {
        Vertice<T> inicio = localizarVertice(origem);
        Vertice<T> fim = localizarVertice(destino);
        if (inicio == null || fim == null) return Float.POSITIVE_INFINITY;
        Aresta<T> aresta = localizarAresta(inicio, fim);
        return aresta == null ? Float.POSITIVE_INFINITY : aresta.peso;
    }

    /** Visita apenas os vertices alcancaveis a partir da origem; nao calcula a rota de entregas. */
    public String caminharEmLargura(T origem) {
        Vertice<T> inicio = localizarVertice(origem);
        if (inicio == null) return "[]";

        boolean[] visitados = new boolean[quantidadeVertices()];
        No<Vertice<T>> primeiro = new No<>(inicio);
        No<Vertice<T>> ultimo = primeiro;
        visitados[inicio.indice] = true;
        StringBuilder resultado = new StringBuilder("[");

        while (primeiro != null) {
            Vertice<T> atual = primeiro.getValor();
            primeiro = primeiro.getProximo();
            if (primeiro == null) ultimo = null;
            if (resultado.length() > 1) resultado.append(", ");
            resultado.append(atual.valor);

            for (Aresta<T> aresta : atual.vizinhos) {
                Vertice<T> vizinho = aresta.destino;
                if (!visitados[vizinho.indice]) {
                    // Marca ao entrar na fila para nao repetir vertices em ciclos.
                    visitados[vizinho.indice] = true;
                    No<Vertice<T>> novo = new No<>(vizinho);
                    if (ultimo == null) primeiro = novo;
                    else ultimo.setProximo(novo);
                    ultimo = novo;
                }
            }
        }
        return resultado.append("]").toString();
    }

    /** Copia a lista de valores, sem expor os nos internos. A ordem nao e uma rota. */
    public ListaEncadeada<T> obterVertices() {
        ListaEncadeada<T> copia = new ListaEncadeada<>(comparador, false);
        for (Vertice<T> vertice : vertices) copia.adicionar(vertice.valor);
        return copia;
    }

    private Vertice<T> localizarVertice(T valor) {
        if (valor == null) return null;
        for (Vertice<T> vertice : vertices) {
            if (comparador.compare(vertice.valor, valor) == 0) return vertice;
        }
        return null;
    }

    private Aresta<T> localizarAresta(Vertice<T> origem, Vertice<T> destino) {
        for (Aresta<T> aresta : origem.vizinhos) {
            if (aresta.destino == destino) return aresta;
        }
        return null;
    }
}
