package AnalisadorSemantico;

public class ErroSemantico extends Exception implements Comparable<ErroSemantico>{
    
    public int linha;
    public String erro;

    public ErroSemantico(String e) {
        linha = -1;
        erro = e;
    }
    public ErroSemantico(int l, String e) {
        linha = l;
        erro = e;
    }
    
    @Override
    public String toString(){
        return "Linha " + linha + ": " + erro;
    }

    @Override
    //Ordena os erros pelo numero da linha em que ocorreram
    public int compareTo(ErroSemantico e) {
        return new Integer(linha).compareTo(e.linha);
    }
}
