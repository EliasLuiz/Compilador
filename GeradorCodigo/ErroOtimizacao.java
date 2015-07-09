package GeradorCodigo;

public class ErroOtimizacao extends Exception implements Comparable<ErroOtimizacao>{
    
    public int linha;
    public String erro;

    public ErroOtimizacao(String e) {
        linha = -1;
        erro = e;
    }
    public ErroOtimizacao(int l, String e) {
        linha = l;
        erro = e;
    }
    
    @Override
    public String toString(){
        return "Linha " + linha + ": " + erro;
    }

    @Override
    //Ordena os erros pelo numero da linha em que ocorreram
    public int compareTo(ErroOtimizacao e) {
        return new Integer(linha).compareTo(e.linha);
    }
}
