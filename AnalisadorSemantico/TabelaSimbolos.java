package AnalisadorSemantico;

import java.io.Serializable;
import java.util.HashMap;

public class TabelaSimbolos implements Serializable {

    private HashMap<String, Simbolo> tabela;
    //chave para o hash: (simbolo.nome + simbolo.escopo)

    public TabelaSimbolos() {
        tabela = new HashMap<>();
    }

    public void addSimbolo(Simbolo s, int linha) throws ErroSemantico {
        String hash = s.nome + s.escopo;

        //Se ja existe
        if (tabela.containsKey(hash)) {
            //Se tipo incopativel
            if (!tabela.get(hash).tipo.equals(s.tipo))
                throw new ErroSemantico(linha, "Variavel \"" + s.nome + "\" e do tipo \""
                        + tabela.get(hash).tipo + "\" e foi atribuida valor do tipo \""
                        + s.tipo + "\".");

            tabela.get(hash).ultimoUso = linha;
        } else {
            s.ultimoUso = linha;
            tabela.put(hash, s);
        }
    }

    public Simbolo getSimbolo(String nome, int escopo, int linha) throws ErroSemantico {
        String hash = nome + escopo;

        if (!tabela.containsKey(hash))
            throw new ErroSemantico(linha, "Variavel \"" + nome + "\" nao declarada.");
        
        tabela.get(hash).ultimoUso = linha;
        
        return tabela.get(hash);
    }

}
