# Trabalho 3 - Grafos

TPA - IFES Campus Serra. Professor: Victorio Albani de Carvalho.

Tema: calcular uma rota de entregas que saia de uma loja, visite todos os locais cadastrados e volte à loja, ou encontrar o menor caminho entre dois locais (entrega expressa), com a menor distância total.

## O que já funciona

- Grafo genérico, não direcionado e com pesos, usando listas de adjacência.
- Inclusão de vértices sem repetição e ligações entre locais já cadastrados.
- Caminhamento em largura a partir de um local.
- Busca exata da menor rota por enumeração das ordens de visita com poda (*branch and bound*), com até 10 locais contando a loja.
- Cálculo do menor caminho entre dois locais pelo algoritmo de Dijkstra (entrega expressa), sem limite de 10 locais.
- Testes da lista reaproveitada, do grafo, da rota completa e do Dijkstra.
- Texto inicial das seções 1 e 2 em [relatorio/Secoes_1_e_2.md](relatorio/Secoes_1_e_2.md).

O aplicativo com menu e leitura de arquivo ainda será integrado. O tema e a estratégia precisam ser apresentados ao professor, conforme a etapa 1 do enunciado. Os algoritmos da rota e do menor caminho foram implementados para avaliação; o limite de 10 locais na rota completa é uma escolha desta versão, não uma exigência do professor.

## Bibliotecas reaproveitadas

`src/colecao` vem do [Trabalho 2 do grupo](https://github.com/bs20242/tpa-trabalho2-arvores/tree/4299391560e470d88a94b6851ee63462eebf3297/src/colecao), que já reutilizava a lista do Trabalho 1. O contador de elementos foi mantido. Foi acrescentado o percurso com `Iterable` e `Iterator` para usar `for` nas listas de vértices e arestas, sem expor os nós.

A interface `IColecao` conserva os comentários e a autoria do material do professor. `No` também é reutilizado na fila do caminhamento em largura. As árvores não são necessárias nesta implementação. Nenhuma lista ou árvore da biblioteca padrão do Java é usada.

## Compilar e testar no Windows

Use um JDK 11 ou superior para compilar. O script gera classes compatíveis com Java 8 ou superior. Caso `javac` não esteja no PATH, configure `JAVA_HOME` com a pasta do seu JDK.

No CMD, dentro da pasta do projeto, execute cada comando em uma linha:

```bat
powershell -NoProfile -ExecutionPolicy Bypass -File .\compilar.ps1
java -ea -cp bin testes.TesteLista
java -ea -cp bin testes.TesteGrafo
java -ea -cp bin testes.TesteCaixeiroViajante
java -ea -cp bin testes.TesteDijkstra
```

No PowerShell, a compilação também pode ser chamada com `./compilar.ps1`.

## Como usar a biblioteca no aplicativo

```java
Grafo<String> grafo = new Grafo<>(String.CASE_INSENSITIVE_ORDER);
grafo.adicionarVertice("Loja");
grafo.adicionarVertice("Cliente A");
grafo.adicionarVertice("Cliente B");
grafo.adicionarAresta("Loja", "Cliente A", 2.0f);
grafo.adicionarAresta("Cliente A", "Cliente B", 3.0f);
grafo.adicionarAresta("Cliente B", "Loja", 4.0f);

System.out.println(grafo.caminharEmLargura("Loja"));
Rota<String> rota = CaixeiroViajante.calcular(grafo, "Loja");
if (rota == null) {
    System.out.println("Nao existe uma rota que visite todos e volte a loja.");
} else {
    System.out.println(rota);
    System.out.println("Distancia total: " + rota.getDistanciaTotal());
}

Rota<String> expressa = Dijkstra.calcular(grafo, "Loja", "Cliente B");
if (expressa != null) {
    System.out.println("Entrega expressa: " + expressa + " (" + expressa.getDistanciaTotal() + ")");
}
```

Importe `grafos.Grafo`, `grafos.CaixeiroViajante`, `grafos.Dijkstra` e `grafos.Rota`. No exemplo, qualquer uma das duas ordens do ciclo completo tem distância total 9, e a entrega expressa entre `Loja` e `Cliente B` retorna `[Loja, Cliente B]` com distância 4.

| Método | Comportamento |
|---|---|
| `adicionarVertice(valor)` | Retorna `false` para nulo ou valor repetido conforme o comparador. |
| `adicionarAresta(origem, destino, peso)` | Cria a ligação nos dois sentidos. Retorna `false` se faltar um extremo, se houver repetição ou se origem e destino forem o mesmo vértice. |
| `pesoEntre(origem, destino)` | Consulta uma ligação direta. Retorna infinito positivo se ela não existir. |
| `caminharEmLargura(origem)` | Retorna a sequência dos vértices alcançáveis; `[]` se a origem não existir. |
| `obterVertices()` | Retorna uma cópia da lista de valores. Os objetos armazenados continuam sendo os mesmos. |
| `CaixeiroViajante.calcular(grafo, origem)` | Retorna a menor `Rota` passando por todos os vértices e voltando à origem, ou `null` quando não há percurso válido. |
| `Dijkstra.calcular(grafo, origem, destino)` | Retorna a menor `Rota` entre `origem` e `destino` (entrega expressa), ou `null` quando não há caminho entre eles. |

Pesos negativos, infinitos e `NaN` causam `IllegalArgumentException`. Peso zero é permitido. `CaixeiroViajante.calcular` lança essa exceção para origem ausente, grafo nulo ou mais de 10 locais; `Dijkstra.calcular` lança para grafo nulo, origem ausente ou destino ausente (sem limite de 10 locais); o menu deve mostrar o motivo ao usuário.

Os valores usados como chave não devem ser alterados enquanto estiverem no grafo. O comparador precisa representar a identificação dos locais. O caminhamento em largura não é a rota de entregas e não minimiza distâncias.

## Arquivo para a integração

[dados/exemplo_entregas.csv](dados/exemplo_entregas.csv) é uma proposta de entrada para o aplicativo. Cada linha `VERTICE;nome` cadastra um local. Cada linha `ARESTA;origem;destino;peso` cadastra uma ligação nos dois sentidos. Os vértices vêm antes das arestas. Use UTF-8, ponto decimal e nomes sem ponto e vírgula. A loja de partida pode ser escolhida no menu.

O arquivo contém quatro locais e seis ligações. A menor distância saindo de `Loja` é 80: `Loja -> A -> C -> B -> Loja`, ou o caminho inverso. Esse resultado é conferido em `TesteCaixeiroViajante`; o leitor do arquivo ainda não está implementado.

## Próxima integração do grupo

- Matheus: leitura do arquivo, menu com as duas operações (rota completa de entregas e entrega expressa entre dois locais via Dijkstra), inclusão posterior de vértices e arestas, escolha da loja/destino e exibição da rota. Separar apresentação e leitura de dados da biblioteca.
- Levi: combinar uma contribuição de código (por exemplo, leitor/validador do CSV e testes de integração), preparar casos de entrada e consolidar o relatório com os textos dos integrantes.
- Grupo: confirmar a divisão efetiva das tarefas e a aprovação do tema e da estratégia; completar as seções 3, 4 e 5 conforme o aplicativo final.

Os slides disponíveis de árvore geradora mínima, Dijkstra e Ford-Fulkerson foram consultados. A classe `Dijkstra` segue a abordagem dos slides da Semana 10 (conjunto de nós rotulados, vetor de distâncias e predecessores). Os slides não contêm uma classe Java pronta para o grafo básico nem a busca exata do Caixeiro Viajante, que foram implementados no projeto.
