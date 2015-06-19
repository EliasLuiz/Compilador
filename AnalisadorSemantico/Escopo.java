package AnalisadorSemantico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Escopo {

    private ArrayList<HashMap<String, String>> escopos;
    private Stack<Integer> nivel;
    private int idEscopo;

    public Escopo() {
        escopos = new ArrayList<>();
        escopos.add(new HashMap<>());
        idEscopo = 0;
        nivel.add(idEscopo);
    }

    public void adicionaEscopo() {
        escopos.add(new HashMap<>());
        nivel.push(++idEscopo);
    }

    public void adicionaVariavel(String variavel, String tipo) {
        //Se variavel ja existe
        for (int i = escopos.size() - 1; i >= 0; i--) {
            if (escopos.get(i).containsKey(variavel)) {
                return;
            }
        }

        escopos.get(escopos.size() - 1).put(variavel, tipo);
    }

    public String tipoVariavel(String variavel) {
        for (int i = escopos.size() - 1; i >= 0; i--) {
            if (escopos.get(i).containsKey(variavel)) {
                return escopos.get(i).get(variavel);
            }
        }
        return null;
    }

    public void removeEscopo() {
        escopos.remove(escopos.size() - 1);
        nivel.pop();
    }

    public int getEscopo() {
        return nivel.peek();
    }
}
