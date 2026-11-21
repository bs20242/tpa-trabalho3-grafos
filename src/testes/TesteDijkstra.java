package testes;

import grafos.Dijkstra;
import grafos.Grafo;
import grafos.Rota;

public class TesteDijkstra {
    public static void main(String[] args) {
        boolean habilitadas = false;
        assert habilitadas = true;
        if (!habilitadas) throw new IllegalStateException("Execute com -ea.");
        testarMenorCaminhoExemplo();
        testarRelaxamentoEmEtapas();
        testarMesmaOrigemEDesconectado();
        testarPesosZeroEFracionarios();
        testarMaisDeDezLocais();
        testarEntradasInvalidas();
        System.out.println("TesteDijkstra: todos os testes passaram.");
    }

    private static void testarMenorCaminhoExemplo() {
        Grafo<String> grafo = new Grafo<>(String.CASE_INSENSITIVE_ORDER);
        for (String local : new String[] {"Loja", "A", "B", "C"}) grafo.adicionarVertice(local);
        grafo.adicionarAresta("Loja", "A", 10);
        grafo.adicionarAresta("Loja", "B", 15);
        grafo.adicionarAresta("Loja", "C", 20);
        grafo.adicionarAresta("A", "B", 35);
        grafo.adicionarAresta("A", "C", 25);
        grafo.adicionarAresta("B", "C", 30);

        // O caminho passando pela Loja (10 + 15 = 25) custa menos que a ligacao direta (35).
        Rota<String> rota = Dijkstra.calcular(grafo, "a", "B");
        assert rota != null;
        assert rota.getDistanciaTotal() == 25;
        assert rota.toString().equals("[A, Loja, B]");

        Rota<String> direto = Dijkstra.calcular(grafo, "Loja", "C");
        assert direto != null;
        assert direto.getDistanciaTotal() == 20;
        assert direto.toString().equals("[Loja, C]");
    }

    private static void testarRelaxamentoEmEtapas() {
        Grafo<Integer> grafo = new Grafo<>(Integer::compare);
        for (int i = 1; i <= 5; i++) grafo.adicionarVertice(i);
        grafo.adicionarAresta(1, 5, 20);
        grafo.adicionarAresta(1, 2, 2);
        grafo.adicionarAresta(2, 5, 10);
        grafo.adicionarAresta(2, 3, 3);
        grafo.adicionarAresta(3, 4, 1);
        grafo.adicionarAresta(4, 5, 2);

        // Estimativa para 5 cai de 20 para 12 (via 2) e depois para 8 (via 2, 3, 4).
        Rota<Integer> rota = Dijkstra.calcular(grafo, 1, 5);
        assert rota != null;
        assert rota.getDistanciaTotal() == 8;
        assert rota.toString().equals("[1, 2, 3, 4, 5]");
    }

    private static void testarMesmaOrigemEDesconectado() {
        Grafo<String> grafo = new Grafo<>(String.CASE_INSENSITIVE_ORDER);
        grafo.adicionarVertice("Loja");
        grafo.adicionarVertice("A");
        grafo.adicionarVertice("Ilha");
        grafo.adicionarAresta("Loja", "A", 5);

        Rota<String> mesma = Dijkstra.calcular(grafo, "Loja", "loja");
        assert mesma != null;
        assert mesma.getDistanciaTotal() == 0;
        assert mesma.toString().equals("[Loja]");

        assert Dijkstra.calcular(grafo, "Loja", "Ilha") == null;
    }

    private static void testarPesosZeroEFracionarios() {
        Grafo<Integer> grafo = new Grafo<>(Integer::compare);
        for (int i = 1; i <= 4; i++) grafo.adicionarVertice(i);
        grafo.adicionarAresta(1, 2, 0.5f);
        grafo.adicionarAresta(2, 3, 0);
        grafo.adicionarAresta(3, 4, 1.25f);
        grafo.adicionarAresta(1, 4, 2);

        Rota<Integer> rota = Dijkstra.calcular(grafo, 1, 4);
        assert rota != null;
        assert rota.getDistanciaTotal() == 1.75;
        assert rota.toString().equals("[1, 2, 3, 4]");
    }

    private static void testarMaisDeDezLocais() {
        Grafo<Integer> grafo = new Grafo<>(Integer::compare);
        for (int i = 0; i < 15; i++) grafo.adicionarVertice(i);
        for (int i = 0; i < 14; i++) grafo.adicionarAresta(i, i + 1, 2);
        grafo.adicionarAresta(0, 14, 100);

        Rota<Integer> rota = Dijkstra.calcular(grafo, 0, 14);
        assert rota != null;
        assert rota.getDistanciaTotal() == 28;
    }

    private static void testarEntradasInvalidas() {
        Grafo<Integer> grafo = new Grafo<>(Integer::compare);
        grafo.adicionarVertice(1);
        grafo.adicionarVertice(2);

        assert lancarExcecao(() -> Dijkstra.calcular(null, 1, 2));
        assert lancarExcecao(() -> Dijkstra.calcular(grafo, 99, 2));
        assert lancarExcecao(() -> Dijkstra.calcular(grafo, 1, 99));
        assert lancarExcecao(() -> Dijkstra.calcular(grafo, null, 2));
        assert lancarExcecao(() -> Dijkstra.calcular(grafo, 1, null));
    }

    private static boolean lancarExcecao(Runnable acao) {
        try {
            acao.run();
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}
