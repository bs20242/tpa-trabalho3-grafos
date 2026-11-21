package grafos;

import colecao.ListaEncadeada;
import java.util.Iterator;

/** Sequencia de locais de um percurso e sua distancia total. */
public class Rota<T> implements Iterable<T> {
    private final ListaEncadeada<T> locais;
    private final double distanciaTotal;

    Rota(ListaEncadeada<T> locais, double distanciaTotal) {
        this.locais = locais;
        this.distanciaTotal = distanciaTotal;
    }

    public double getDistanciaTotal() {
        return distanciaTotal;
    }

    @Override
    public Iterator<T> iterator() {
        return locais.iterator();
    }

    @Override
    public String toString() {
        return locais.toString();
    }
}
