# Trabalho 3 - Grafos: rotas de entrega

IFES Campus Serra - Técnicas de Programação Avançada

Professor: Victorio Albani de Carvalho

Integrantes: Bernardo Simão Rosa, Matheus Abreu e Levi Monteiro

Versão inicial das seções 1 e 2. A estratégia descrita será apresentada ao professor para aprovação, conforme a etapa 1 do enunciado.

## 1. Especificação do problema

### 1.1. Situação escolhida

Uma loja precisa organizar suas entregas de duas formas complementares sobre o mesmo mapa de ruas:

1. **Rota completa do dia**: o entregador sai da loja, visita cada local cadastrado uma vez e retorna à loja com a menor distância total (Caixeiro Viajante).
2. **Entrega expressa**: encontrar o menor caminho direto ou indireto entre quaisquer dois locais do grafo, sem precisar visitar todos os demais clientes (algoritmo de Dijkstra).

No primeiro caso, para três ou mais locais, buscamos um ciclo hamiltoniano de menor custo: um percurso que visita todos os vértices uma vez e termina no vértice de partida, com a loja no começo e no fim da sequência.

Cada vértice representa a loja ou um local de entrega. Cada aresta representa um deslocamento disponível entre dois locais, com sua distância em quilômetros. Nesta versão, a distância é a mesma nos dois sentidos. O grafo é não direcionado, sem laços e sem arestas repetidas. Os pesos são finitos e não negativos.

As distâncias são informadas nos dados de entrada. Não há consulta a mapas, cálculo de coordenadas ou consideração de trânsito. Quando não há uma aresta entre dois locais, esse deslocamento direto não está disponível. Caso nenhuma ordem permita visitar todos e retornar, ou não exista ligação entre origem e destino, o algoritmo selecionado retorna ausência de rota (`null`) para os dados fornecidos.

### 1.2. Algoritmos

O caminhamento em largura permite conferir quais locais são alcançáveis a partir de uma origem. Ele visita os vizinhos com ajuda de uma fila. Esse percurso não considera os pesos para escolher a ordem e não calcula rotas de distância mínima.

Para a **rota completa**, foi implementada uma busca exata por *backtracking* com poda (*branch and bound*). A loja fica fixa como origem. A cada chamada recursiva, o algoritmo escolhe um local ainda não visitado e ligado ao local atual. Se a distância parcial acumulada já for maior ou igual à menor distância encontrada até o momento, o ramo é podado sem descer na recursão, pois não existem pesos negativos que possam reduzir o total. Caso contrário, avança na recursão e, ao retornar, desfaz a marcação para testar outra ordem.

Quando todos os locais foram visitados, o algoritmo verifica a ligação de volta à loja. Se ela existir, soma seu peso e atualiza a melhor rota encontrada. O número de possibilidades cresce rapidamente; por isso, a busca exata aceita até 10 locais, contando a loja. Esse limite é uma escolha do projeto para garantir resposta imediata em conjuntos pequenos, não uma exigência do enunciado.

Com apenas a loja, a distância é zero. Com a loja e um cliente, a rota vai até o cliente e volta pela mesma ligação, somando duas vezes seu peso. Esses casos são tratados como percursos de entrega; o caso de dois vértices não é um ciclo simples no sentido usual de grafos não direcionados.

Para a **entrega expressa** entre dois locais quaisquer (origem e destino), foi implementado o **algoritmo de Dijkstra**, seguindo o material das aulas (Semana 10). Ele mantém os conjuntos de vértices já rotulados e não rotulados, escolhendo a cada iteração o vértice não rotulado com menor distância estimada desde a origem, fixando sua distância e atualizando as estimativas dos seus vizinhos. Como não enumera permutações, o Dijkstra não possui limite de 10 vértices e responde mesmo quando não há ligação direta entre origem e destino, desde que exista um caminho intermediário.

### 1.3. Representação do grafo

A representação principal usa listas de adjacência. O grafo guarda uma lista de vértices, e cada vértice guarda uma lista de arestas com o destino e o peso. Uma ligação não direcionada é armazenada nas duas listas dos seus extremos.

Foi reaproveitada a `ListaEncadeada` dos trabalhos anteriores. A inclusão no início da lista não ordenada é constante, e o contador de nós permite consultar seu tamanho sem percorrê-la. Foi acrescentado um iterador para percorrer a lista com `for`, mantendo os nós internos protegidos.

Antes de executar tanto o Caixeiro Viajante quanto o Dijkstra, o método auxiliar `matrizPesos()` percorre cada vértice e sua lista de vizinhos uma única vez para montar uma matriz de pesos `float[V][V]` em tempo O(V²). Assim, cada etapa dos algoritmos consulta uma distância em tempo constante diretamente na matriz, sem procurar a mesma ligação repetidamente nas listas. Essa matriz auxiliar é criada apenas durante o cálculo; o grafo permanece armazenado por listas de adjacência.

## 2. Biblioteca de grafos

### 2.1. Classes e métodos

A classe `Grafo<T>` armazena valores genéricos. Ela recebe um `Comparator<T>` para identificar vértices iguais. Por exemplo, o aplicativo pode comparar os nomes dos locais sem diferenciar letras maiúsculas e minúsculas.

O vértice e a aresta são classes internas. O vértice contém o valor, um índice e a lista de vizinhos. A aresta contém o vértice de destino e o peso `float`.

```java
private final Comparator<T> comparador;
private final ListaEncadeada<Vertice<T>> vertices;
private int totalArestas;
```

A inclusão de vértices verifica a existência do valor antes de acrescentá-lo:

```java
public boolean adicionarVertice(T valor) {
    if (valor == null || localizarVertice(valor) != null) return false;
    vertices.adicionar(new Vertice<>(valor, vertices.quantidadeNos(), comparador));
    return true;
}
```

A inclusão de arestas localiza os dois vértices, confere o peso e rejeita laços ou ligações já existentes. Após essas verificações, acrescenta os dois sentidos:

```java
inicio.vizinhos.adicionar(new Aresta<>(fim, peso));
fim.vizinhos.adicionar(new Aresta<>(inicio, peso));
totalArestas++;
```

`pesoEntre` consulta uma ligação direta e retorna infinito positivo se ela não existir. `obterVertices` devolve uma cópia da lista, sem permitir mudanças nos nós do grafo. Os próprios objetos `T` são compartilhados; seus valores de identificação não devem ser modificados durante o uso. No mesmo pacote `grafos`, os métodos auxiliares `indiceDe`, `matrizPesos` e `montarSequencia` permitem que `CaixeiroViajante` e `Dijkstra` montem a matriz de distâncias em O(V²) e construam instâncias de `Rota<T>` sem expor vértices internos fora do pacote.

### 2.2. Caminhamento em largura

`caminharEmLargura` localiza a origem, coloca esse vértice na fila e marca seu índice como visitado. Em seguida, retira o primeiro elemento da fila e acrescenta ao final os vizinhos ainda não visitados. A fila usa a classe `No` da biblioteca anterior, com referências para o início e o fim.

O vértice é marcado ao entrar na fila. Assim, um ciclo ou dois caminhos até o mesmo vértice não provocam visitas repetidas. As marcações são criadas novamente a cada execução.

O resultado contém somente o componente alcançável a partir da origem. Um local isolado aparece sozinho. Se a origem não existir, o método retorna `[]`. A ordem dos vizinhos segue a lista de adjacência, que insere os elementos no início.

### 2.3. Busca das rotas: Caixeiro Viajante e Dijkstra

`CaixeiroViajante.calcular` recebe o grafo e o valor da origem. Ele obtém a matriz de pesos diretamente por `grafo.matrizPesos()` e inicia a busca recursiva. O vetor `visitados` evita repetir locais, enquanto `caminho` guarda os índices escolhidos. Antes de cada descida recursiva, a distância parcial `novaDistancia` é comparada com `menorDistancia` para podar ramos que já não podem superar a melhor rota encontrada:

```java
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
```

A volta à origem também entra na comparação. Uma rota sem essa ligação não é aceita. O resultado `Rota<T>` guarda a sequência, com a origem repetida no final, e a distância total. A soma usa `double`, embora cada aresta receba um `float`. Se não houver solução, o método retorna `null`.

Para o menor caminho entre dois locais, `Dijkstra.calcular` recebe o grafo, a origem e o destino. A implementação usa três vetores indexados pelo índice interno dos vértices (`distancias`, `predecessores` e `rotulados`), iniciando a origem com distância `0` e os demais vértices com infinito positivo e predecessor `-1`:

```java
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
```

Assim que o destino é rotulado, o laço é encerrado. O percurso mínimo é reconstruído seguindo os predecessores do destino até `-1` e devolvido no mesmo tipo `Rota<T>`, sem repetir a origem no final. Quando o destino não é alcançável a partir da origem, o método retorna `null`.

### 2.4. Complexidade da implementação

Considere `V` vértices, `E` arestas não direcionadas e `d(u)` vizinhos do vértice `u`. As análises abaixo consideram cada comparação de valores como constante. Com nomes de tamanho variável, o custo de comparar as strings também precisa ser considerado.

| Operação | Custo no pior caso | Motivo |
|---|---|---|
| Adicionar vértice | O(V) | Percorre os vértices para evitar repetição; inserir na lista custa O(1). |
| Adicionar aresta entre `u` e `v` | O(V + d(u)) | Localiza os extremos e procura uma ligação já existente antes das duas inserções. |
| Consultar peso entre `u` e `v` | O(V + d(u)) | Localiza os extremos e percorre a lista de vizinhos de `u`. |
| Consultar quantidades | O(1) | Lê os contadores de vértices e arestas. |
| Copiar a lista de vértices | O(V) | Percorre a lista uma vez. |
| Caminhamento em largura | O(V + E) | Inicializa as marcações e percorre os vértices e suas adjacências alcançáveis. |
| Preparar a matriz de pesos (`matrizPesos`) | O(V²) | Preenche a matriz `V x V` e percorre cada vértice e sua lista de vizinhos uma vez (`V² + V + 2E`). |
| Menor caminho entre dois locais (`Dijkstra`) | O(V²) | Monta a matriz em O(V²) e executa até `V` iterações buscando o próximo vértice e relaxando `V` vizinhos por vez. |
| Busca exata da rota (`CaixeiroViajante`) | O(V!) | Enumera as ordens dos V - 1 locais após fixar a origem. A poda descarta ramos ruins na prática, mas o pior caso continua fatorial. |

O grafo ocupa O(V + E), pois cada aresta aparece duas vezes. O caminhamento usa O(V) de memória auxiliar, além do texto de saída. Tanto o `Dijkstra` quanto o `CaixeiroViajante` utilizam O(V²) de memória auxiliar para a matriz de pesos e O(V) para os vetores de controle (e pilha de recursão no caso do Caixeiro Viajante). No `Dijkstra`, o custo total de tempo é O(V²). No `CaixeiroViajante`, somando a montagem da matriz, a busca e a reconstrução da sequência (`O(V²)`), o custo total de tempo é O(V² + V!), dominado pelo termo fatorial quando V cresce.

Essas expressões descrevem o crescimento dos algoritmos em função do tamanho da entrada. No Caixeiro Viajante, a poda acelera casos com boas rotas iniciais, mas o limite prático de 10 locais continua necessário porque a enumeração exata não escala polinomialmente. Já o Dijkstra mantém tempo quadrático O(V²) com a matriz auxiliar, atendendo grafos maiores. O tamanho dos textos produzidos por `toString` também acrescenta custo à geração das saídas.

### 2.5. Conferência do funcionamento

Os testes verificam a biblioteca de listas reaproveitada, vértices repetidos, ligações nos dois sentidos, pesos inválidos, caminhamento com ciclos, componentes desconectados e repetição de consultas.

Para o Caixeiro Viajante, o caso com a loja e três clientes tem as distâncias: Loja-A = 10, Loja-B = 15, Loja-C = 20, A-B = 35, A-C = 25 e B-C = 30. Os três ciclos distintos, desconsiderando o sentido inverso, têm custos 95, 80 e 95. A biblioteca encontra a distância 80 e inclui a volta à loja. Também são testados ausência de rota, origem ausente, pesos zero e fracionários, casos pequenos, preservação do ótimo exato com poda em grafo completo e o limite de 10 locais.

Para o Dijkstra (`TesteDijkstra`), no mesmo grafo de exemplo, a busca entre `A` e `B` encontra `[A, Loja, B]` com distância 25 (menor que a ligação direta de 35). Os testes conferem ainda relaxamento em múltiplas etapas, origem igual ao destino com custo 0, vértice desconectado retornando `null`, pesos zero e fracionários, grafo com 15 vértices (acima do limite do Caixeiro Viajante) e rejeição de entradas inválidas.

Esses valores são exemplos de entrada conferidos pelos testes, não medições de desempenho. A leitura dos arquivos e o menu serão descritos nas seções seguintes após a integração do aplicativo.
