package AnalisadorSemantico;

public class Simbolo {
    public String nome;
    public String tipo;
    public int ultimoUso;
    public int escopo;
    public boolean isFuncao;
    public boolean isVetor;

    public Simbolo(String nome, String tipo, int ultimoUso, int escopo, boolean isFuncao, boolean isVetor) {
        this.nome = nome;
        this.tipo = tipo;
        this.ultimoUso = ultimoUso;
        this.escopo = escopo;
        this.isFuncao = isFuncao;
        this.isVetor = isVetor;
    }
    
    public boolean equals(Simbolo x){
        return nome.equals(x.nome) && escopo == x.escopo;
    }
}
