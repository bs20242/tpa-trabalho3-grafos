# Trabalho 3 - Grafos: rotas de entrega

IFES Campus Serra - Técnicas de Programação Avançada

Professor: Victorio Albani de Carvalho

Integrantes: Bernardo Simão Rosa, Matheus Abreu e Levi Monteiro

Versão inicial das seções 1 e 2. A estratégia descrita será apresentada ao professor para aprovação, conforme a etapa 1 do enunciado.

## 1. Especificação do problema

### 1.1. Situação escolhida

Uma loja precisa organizar a ordem das suas entregas. O entregador sai da loja, visita cada local cadastrado uma vez e retorna à loja. A proposta é encontrar a ordem que tenha a menor distância total, incluindo o caminho de volta.

O problema é uma aplicação do Caixeiro Viajante. Para três ou mais locais, buscamos um ciclo hamiltoniano de menor custo: um percurso que visita todos os vértices uma vez e termina no vértice de partida. A loja aparece no começo e no fim da sequência.

Cada vértice representa a loja ou um local de entrega. Cada aresta representa um deslocamento disponível entre dois locais, com sua distância em quilômetros. Nesta versão, a distância é a mesma nos dois sentidos. O grafo é não direcionado, sem laços e sem arestas repetidas. Os pesos são finitos e não negativos.

As distâncias são informadas nos dados de entrada. Não há consulta a mapas, cálculo de coordenadas ou consideração de trânsito. Quando não há uma aresta entre dois locais, esse deslocamento direto não está disponível. Caso nenhuma ordem permita visitar todos e retornar, o programa deve informar que não existe uma rota válida para os dados fornecidos.

### 1.2. Algoritmos

O caminhamento em largura permite conferir quais locais são alcançáveis a partir da loja. Ele visita os vizinhos com ajuda de uma fila. Esse percurso não considera os pesos para escolher a ordem e não resolve o Caixeiro Viajante.

Para calcular a rota, foi implementada uma busca exata que testa as ordens possíveis de visita. A loja fica fixa como origem. A cada chamada recursiva, o algoritmo escolhe um local ainda não visitado e ligado ao local atual. Depois de testar essa escolha, desfaz a marcação para experimentar outra ordem. Esse procedimento é chamado de backtracking.

Quando todos os locais foram visitados, o algoritmo verifica a ligação de volta à loja. Se ela existir, soma seu peso e compara a distância completa com a melhor encontrada. Como todas as ordens viáveis são consideradas, o resultado é a menor distância para os pesos fornecidos. Em caso de empate, basta retornar uma das melhores rotas.

O número de possibilidades cresce rapidamente. Por isso, a implementação aceita até 10 locais, contando a loja. Esse limite é uma escolha do projeto para usar uma solução exata e simples em conjuntos pequenos. Ele não é uma exigência do enunciado e faz parte da proposta a ser avaliada pelo professor.

Com apenas a loja, a distância é zero. Com a loja e um cliente, a rota vai até o cliente e volta pela mesma ligação, somando duas vezes seu peso. Esses casos são tratados como percursos de entrega; o caso de dois vértices não é um ciclo simples no sentido usual de grafos não direcionados.

### 1.3. Representação do grafo

A representação principal usa listas de adjacência. O grafo guarda uma lista de vértices, e cada vértice guarda uma lista de arestas com o destino e o peso. Uma ligação não direcionada é armazenada nas duas listas dos seus extremos.

Foi reaproveitada a `ListaEncadeada` dos trabalhos anteriores. A inclusão no início da lista não ordenada é constante, e o contador de nós permite consultar seu tamanho sem percorrê-la. Foi acrescentado um iterador para percorrer a lista com `for`, mantendo os nós internos protegidos.

Durante o cálculo da rota, os pesos são copiados para uma matriz auxiliar. Assim, cada etapa recursiva consulta uma distância diretamente, sem procurar a mesma ligação repetidamente nas listas. Essa matriz é criada para cada cálculo; o grafo continua armazenado por listas de adjacência.

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

`pesoEntre` consulta uma ligação direta e retorna infinito positivo se ela não existir. `obterVertices` devolve uma cópia da lista, sem permitir mudanças nos nós do grafo. Os próprios objetos `T` são compartilhados; seus valores de identificação não devem ser modificados durante o uso.

### 2.2. Caminhamento em largura

`caminharEmLargura` localiza a origem, coloca esse vértice na fila e marca seu índice como visitado. Em seguida, retira o primeiro elemento da fila e acrescenta ao final os vizinhos ainda não visitados. A fila usa a classe `No` da biblioteca anterior, com referências para o início e o fim.

O vértice é marcado ao entrar na fila. Assim, um ciclo ou dois caminhos até o mesmo vértice não provocam visitas repetidas. As marcações são criadas novamente a cada execução.

O resultado contém somente o componente alcançável a partir da origem. Um local isolado aparece sozinho. Se a origem não existir, o método retorna `[]`. A ordem dos vizinhos segue a lista de adjacência, que insere os elementos no início.

### 2.3. Busca da rota

`CaixeiroViajante.calcular` recebe o grafo e o valor da origem. Ele prepara a matriz de pesos e inicia a busca recursiva. O vetor `visitados` evita repetir locais, enquanto `caminho` guarda os índices escolhidos.

```java
for (int proximo = 0; proximo < pesos.length; proximo++) {
    if (!visitados[proximo] && Float.isFinite(pesos[atual][proximo])) {
        visitados[proximo] = true;
        caminho[quantidade] = proximo;
        visitar(proximo, quantidade + 1, distancia + pesos[atual][proximo]);
        // Desfaz a escolha para tentar outra ordem de visita.
        visitados[proximo] = false;
    }
}
```

A volta à origem também entra na comparação. Uma rota sem essa ligação não é aceita. O resultado `Rota<T>` guarda a sequência, com a origem repetida no final, e a distância total. A soma usa `double`, embora cada aresta receba um `float`. Se não houver solução, o método retorna `null`.

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
| Preparar a matriz da rota | O(V³) | Faz V² consultas de peso, e cada consulta pode custar O(V). |
| Busca exata da rota | O(V!) | Enumera as ordens dos V - 1 locais após fixar a origem. A implementação também percorre os candidatos em cada chamada. |

O grafo ocupa O(V + E), pois cada aresta aparece duas vezes. O caminhamento usa O(V) de memória auxiliar, além do texto de saída. A busca da rota usa O(V²) para a matriz, O(V) para os vetores e O(V) para a pilha de recursão. Seu custo total de tempo pode ser escrito como O(V³ + V!), dominado pelo termo fatorial quando V cresce.

Essas expressões descrevem o crescimento do algoritmo se o limite de entrada fosse aumentado. O limite prático de 10 locais não torna a enumeração uma estratégia escalável. O tamanho dos textos produzidos por `toString` também acrescenta custo à geração das saídas.

### 2.5. Conferência do funcionamento

Os testes verificam a biblioteca de listas reaproveitada, vértices repetidos, ligações nos dois sentidos, pesos inválidos, caminhamento com ciclos, componentes desconectados e repetição de consultas.

Para a rota, um caso com a loja e três clientes tem as distâncias: Loja-A = 10, Loja-B = 15, Loja-C = 20, A-B = 35, A-C = 25 e B-C = 30. Os três ciclos distintos, desconsiderando o sentido inverso, têm custos 95, 80 e 95. A biblioteca encontra a distância 80 e inclui a volta à loja. Também são testados ausência de rota, origem ausente, pesos zero e fracionários, casos pequenos e o limite de locais.

Esses valores são exemplos de entrada conferidos pelos testes, não medições de desempenho. A leitura dos arquivos e o menu serão descritos nas seções seguintes após a integração do aplicativo.
