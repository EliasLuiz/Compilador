package AnalisadorSintatico;

public class ErroSintatico extends Exception implements Comparable<ErroSintatico>{
    public int linha;
    public String erro;

    public ErroSintatico(String e) {
        erro = e;
    }
    public ErroSintatico(int l, String e) {
        linha = l;
        erro = e;
    }
    
    @Override
    public String toString(){
        return "Linha " + linha + ": " + erro;
    }
    
    @Override
    //Ordena os erros pelo numero da linha em que ocorreram
    public int compareTo(ErroSintatico e){
        return new Integer(linha).compareTo(e.linha);
    }
}
