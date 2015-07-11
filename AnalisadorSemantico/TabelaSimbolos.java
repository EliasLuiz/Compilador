package AnalisadorSemantico;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TabelaSimbolos implements Serializable {

    private HashMap<String, Simbolo> tabela;
    //chave para o hash: (simbolo.nome + simbolo.escopo)

    public TabelaSimbolos() {
        tabela = new HashMap<>();
    }

    public void addSimbolo(Simbolo s) throws ErroSemantico {
        //Se ja existe
        if (tabela.containsKey(s.hash)) {
            
            //Se tipo incompativel
            if (!tabela.get(s.hash).tipo.equals(s.tipo))
                throw new ErroSemantico(s.ultimoUso, "Variavel \"" + s.nome + "\" e do tipo \""
                        + tabela.get(s.hash).tipo + "\" e foi atribuida valor do tipo \""
                        + s.tipo + "\".");
            
            //Se foi utilizado mais a frente
            if(s.ultimoUso > tabela.get(s.hash).ultimoUso)
                tabela.get(s.hash).ultimoUso = s.ultimoUso;
            
            if(s.isFuncao)
                tabela.replace(s.hash, s);
        
        } else 
            tabela.put(s.hash, s);
    }

    public Simbolo getSimbolo(String hash, int linha) {
        //Se foi utilizado mais a frente
        if(linha > tabela.get(hash).ultimoUso)
            tabela.get(hash).ultimoUso = linha;
        
        return tabela.get(hash);
    }
    
    public String[] getFuncoes(){
        ArrayList<String> funcoes = new ArrayList<>();
        for (Map.Entry<String, Simbolo> entrySet : tabela.entrySet()) 
            if(entrySet.getValue().isFuncao)
                funcoes.add(entrySet.getKey());
        
        Object[] aux = funcoes.toArray();
        String[] ret = new String[aux.length];
        for (int i = 0; i < ret.length; i++)
            ret[i] = aux[i].toString();
        return ret;
    }

    public void print(){
        for(String key : tabela.keySet()){
            System.out.println(key + " = {\n" + tabela.get(key).toString() + "\n}");
        }
    }
}
