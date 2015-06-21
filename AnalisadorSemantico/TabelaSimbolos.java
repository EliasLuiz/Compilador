package AnalisadorSemantico;

import java.io.Serializable;
import java.util.HashMap;

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
        
        } else 
            tabela.put(s.hash, s);
    }

    public Simbolo getSimbolo(String hash, int linha) {
        //Se foi utilizado mais a frente
        if(linha > tabela.get(hash).ultimoUso)
            tabela.get(hash).ultimoUso = linha;
        
        return tabela.get(hash);
    }

}
