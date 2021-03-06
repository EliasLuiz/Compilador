package AnalisadorSemantico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Escopo {

    //Nome variavel -> hash da variavel na tabela
    private ArrayList<HashMap<String, String>> escopos;
    private Stack<Integer> nivel;
    public int idEscopo;

    public Escopo() {
        escopos = new ArrayList<>();
        escopos.add(new HashMap<>());
        nivel = new Stack<>();
        idEscopo = 0;
        nivel.add(idEscopo);
    }

    public void adicionaEscopo() {
        escopos.add(new HashMap<>());
        nivel.push(++idEscopo);
    }

    public void adicionaVariavel(String variavel) {
        //Se variavel ja existe
        for (int i = escopos.size() - 1; i >= 0; i--) {
            if (escopos.get(i).containsKey(variavel)) {
                return;
            }
        }

        escopos.get(escopos.size() - 1).put(variavel, variavel + nivel.peek());
    }
    
    public void adicionaFuncao(String funcao) {
        escopos.get(0).put(funcao, funcao);
    }
    
    public String getVariavel(String variavel) throws ErroSemantico {
        for (int i = escopos.size()-1; i >= 0; i--) {
            if (escopos.get(i).containsKey(variavel)) {
                return escopos.get(i).get(variavel);
            }
        }
        throw new ErroSemantico("Variavel \"" + variavel + "\" nao foi declarada.");
    }

    public void removeEscopo() {
        escopos.remove(escopos.size() - 1);
        nivel.pop();
    }

    public int getEscopo() {
        return nivel.peek();
    }
    
    public void flush(){
        nivel = new Stack<>();
        escopos = new ArrayList<>();
    }
}
