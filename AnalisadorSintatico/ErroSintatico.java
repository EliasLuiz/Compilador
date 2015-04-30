package AnalisadorSintatico;

public class ErroSintatico extends Exception{
    public String erro;

    public ErroSintatico(String e) {
        erro = e;
    }
}
