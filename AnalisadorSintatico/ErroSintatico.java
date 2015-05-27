package AnalisadorSintatico;

public class ErroSintatico extends Exception{
    public int linha;
    public String erro;

    public ErroSintatico(String e) {
        erro = e;
    }
    public ErroSintatico(int l, String e) {
        linha = l;
        erro = e;
    }
}
