package testes;

import colecao.ListaEncadeada;
import grafos.Grafo;

public class TesteGrafo {
    public static void main(String[] args) {
        boolean habilitadas = false;
        assert habilitadas = true;
        if (!habilitadas) throw new IllegalStateException("Execute com -ea.");
        testarVertices();
        testarArestas();
        testarLargura();
        testarPesosInvalidos();
        System.out.println("TesteGrafo: todos os testes passaram.");
    }

    private static void testarVertices() {
        Grafo<String> grafo = new Grafo<>(String.CASE_INSENSITIVE_ORDER);
        assert grafo.caminharEmLargura("Loja").equals("[]");
        assert !grafo.adicionarVertice(null);
        assert grafo.adicionarVertice("Loja") : "O primeiro vertice deve ser inserido.";
        assert !grafo.adicionarVertice("LOJA");
        assert grafo.quantidadeVertices() == 1;
        assert grafo.caminharEmLargura("loja").equals("[Loja]");
        ListaEncadeada<String> copia = grafo.obterVertices();
        assert copia.pesquisar("LOJA").equals("Loja");
        copia.remover("Loja");
        assert grafo.quantidadeVertices() == 1 : "A copia nao deve alterar o grafo.";
    }

    private static void testarArestas() {
        Grafo<Integer> grafo = new Grafo<>(Integer::compare);
        grafo.adicionarVertice(1);
        grafo.adicionarVertice(2);
        assert !grafo.adicionarAresta(1, 3, 2);
        assert !grafo.adicionarAresta(null, 2, 2);
        assert !grafo.adicionarAresta(1, 1, 2);
        assert grafo.adicionarAresta(1, 2, 2.5f);
        assert grafo.pesoEntre(1, 2) == 2.5f;
        assert grafo.pesoEntre(2, 1) == 2.5f : "A ligacao deve valer nos dois sentidos.";
        assert !grafo.adicionarAresta(2, 1, 8);
        assert grafo.quantidadeArestas() == 1;
        assert grafo.pesoEntre(1, 2) == 2.5f;
        grafo.adicionarVertice(3);
        assert grafo.adicionarAresta(2, 3, 0);
        assert grafo.pesoEntre(2, 3) == 0;
        assert grafo.pesoEntre(1, 3) == Float.POSITIVE_INFINITY;
        assert grafo.pesoEntre(1, 99) == Float.POSITIVE_INFINITY;
        assert grafo.quantidadeArestas() == 2;
    }

    private static void testarLargura() {
        Grafo<Integer> grafo = new Grafo<>(Integer::compare);
        for (int i = 1; i <= 6; i++) grafo.adicionarVertice(i);
        grafo.adicionarAresta(1, 2, 1);
        grafo.adicionarAresta(1, 3, 1);
        grafo.adicionarAresta(2, 4, 1);
        grafo.adicionarAresta(3, 5, 1);
        grafo.adicionarAresta(4, 5, 1);
        // Os vizinhos sao visitados na ordem em que aparecem na lista.
        assert grafo.caminharEmLargura(1).equals("[1, 3, 2, 5, 4]");
        assert grafo.caminharEmLargura(1).equals("[1, 3, 2, 5, 4]");
        assert grafo.caminharEmLargura(6).equals("[6]");
        assert grafo.caminharEmLargura(99).equals("[]");
        grafo.adicionarAresta(5, 6, 1);
        assert grafo.caminharEmLargura(1).equals("[1, 3, 2, 5, 4, 6]");
        assert grafo.caminharEmLargura(6).equals("[6, 5, 4, 3, 2, 1]");
    }

    private static void testarPesosInvalidos() {
        Grafo<Integer> grafo = new Grafo<>(Integer::compare);
        grafo.adicionarVertice(1);
        grafo.adicionarVertice(2);
        for (float peso : new float[] {-1, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
            boolean rejeitado = false;
            try {
                grafo.adicionarAresta(1, 2, peso);
            } catch (IllegalArgumentException e) {
                rejeitado = true;
            }
            assert rejeitado : "Peso invalido aceito.";
            assert grafo.quantidadeArestas() == 0;
        }
        boolean rejeitado = false;
        try {
            new Grafo<Integer>(null);
        } catch (IllegalArgumentException e) {
            rejeitado = true;
        }
        assert rejeitado : "O comparador e necessario.";
    }
}
