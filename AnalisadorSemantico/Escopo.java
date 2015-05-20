package AnalisadorSemantico;

import java.util.ArrayList;
import java.util.HashMap;

public class Escopo {
    private ArrayList<HashMap<String, String>> escopos;
    
    public Escopo(){
        escopos = new ArrayList<>();
        escopos.add(new HashMap<>());
    }
    
    
    
    public void adicionaEscopo(int nivel, String variavel, String tipo){
        //Se nivel de escopo nao existe
        while(escopos.size() < nivel+1)
            escopos.add(new HashMap<>());
        
        //Se variavel ja existe
        if(escopos.get(nivel).containsKey(variavel))
            return;
        
        HashMap<String, String> aux = escopos.get(nivel);
        aux.put(variavel, tipo);
        escopos.set(nivel, aux);
    }
    public String buscaVariavel(String variavel){
        for (int i = escopos.size()-1; i > 0; i--) {
            if(escopos.get(i).containsKey(variavel))
                return escopos.get(i).get(variavel);
        }
        return null;
    }
    public void removeEscopo(){
        escopos.remove(escopos.size()-1);
    }
}


