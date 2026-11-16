package testes;

import grafos.CaixeiroViajante;
import grafos.Grafo;
import grafos.Rota;

public class TesteCaixeiroViajante {
    public static void main(String[] args) {
        boolean habilitadas = false;
        assert habilitadas = true;
        if (!habilitadas) throw new IllegalStateException("Execute com -ea.");
        testarMenorRota();
        testarSemRota();
        testarCasosPequenos();
        testarLimites();
        System.out.println("TesteCaixeiroViajante: todos os testes passaram.");
    }

    private static void testarMenorRota() {
        Grafo<String> grafo = new Grafo<>(String.CASE_INSENSITIVE_ORDER);
        for (String local : new String[] {"Loja", "A", "B", "C"}) grafo.adicionarVertice(local);
        grafo.adicionarAresta("Loja", "A", 10);
        grafo.adicionarAresta("Loja", "B", 15);
        grafo.adicionarAresta("Loja", "C", 20);
        grafo.adicionarAresta("A", "B", 35);
        grafo.adicionarAresta("A", "C", 25);
        grafo.adicionarAresta("B", "C", 30);
        // Os tres ciclos distintos custam 95, 80 e 95, contando o retorno.
        Rota<String> rota = CaixeiroViajante.calcular(grafo, "LOJA");
        assert rota != null : "Existe uma rota de entregas.";
        assert rota.getDistanciaTotal() == 80;
        assert rota.toString().equals("[Loja, A, C, B, Loja]")
            || rota.toString().equals("[Loja, B, C, A, Loja]");
        String anterior = null;
        double soma = 0;
        int quantidade = 0;
        for (String local : rota) {
            if (anterior != null) soma += grafo.pesoEntre(anterior, local);
            anterior = local;
            quantidade++;
        }
        assert quantidade == 5 && soma == 80;
        assert CaixeiroViajante.calcular(grafo, "B").getDistanciaTotal() == 80;
        assert grafo.quantidadeVertices() == 4 && grafo.quantidadeArestas() == 6;
    }

    private static void testarSemRota() {
        Grafo<Integer> grafo = new Grafo<>(Integer::compare);
        for (int i = 1; i <= 4; i++) grafo.adicionarVertice(i);
        grafo.adicionarAresta(1, 2, 1);
        grafo.adicionarAresta(2, 3, 1);
        assert CaixeiroViajante.calcular(grafo, 1) == null;
        grafo.adicionarAresta(3, 4, 1);
        // Todos sao alcancaveis, mas nao existe a volta a origem sem repetir vertices.
        assert CaixeiroViajante.calcular(grafo, 1) == null;
        grafo.adicionarAresta(4, 1, 1);
        assert CaixeiroViajante.calcular(grafo, 1).getDistanciaTotal() == 4;
    }

    private static void testarCasosPequenos() {
        Grafo<Integer> grafo = new Grafo<>(Integer::compare);
        grafo.adicionarVertice(1);
        assert CaixeiroViajante.calcular(grafo, 1).getDistanciaTotal() == 0;
        grafo.adicionarVertice(2);
        assert CaixeiroViajante.calcular(grafo, 1) == null;
        grafo.adicionarAresta(1, 2, 0.5f);
        assert CaixeiroViajante.calcular(grafo, 1).getDistanciaTotal() == 1;
        grafo.adicionarVertice(3);
        grafo.adicionarAresta(2, 3, 0);
        grafo.adicionarAresta(3, 1, 1.25f);
        assert CaixeiroViajante.calcular(grafo, 1).getDistanciaTotal() == 1.75;
    }

    private static void testarLimites() {
        Grafo<Integer> grafo = new Grafo<>(Integer::compare);
        boolean rejeitado = false;
        try { CaixeiroViajante.calcular(grafo, 1); }
        catch (IllegalArgumentException e) { rejeitado = true; }
        assert rejeitado;
        for (int i = 0; i < 10; i++) grafo.adicionarVertice(i);
        for (int i = 0; i < 10; i++) grafo.adicionarAresta(i, (i + 1) % 10, 1);
        assert CaixeiroViajante.calcular(grafo, 0).getDistanciaTotal() == 10;
        grafo.adicionarVertice(10);
        rejeitado = false;
        try { CaixeiroViajante.calcular(grafo, 0); }
        catch (IllegalArgumentException e) { rejeitado = true; }
        assert rejeitado : "O limite deve impedir uma busca grande demais.";
    }
}
