package AnalisadorSemantico;

public class Simbolo {
    public String nome;
    public String tipo;
    public int ultimoUso;
    public int escopo;

    public Simbolo(String nome, String tipo, int ultimoUso, int escopo) {
        this.nome = nome;
        this.tipo = tipo;
        this.ultimoUso = ultimoUso;
        this.escopo = escopo;
    }
    
    public boolean equals(Simbolo x){
        return nome.equals(x.nome) && escopo == x.escopo;
    }
}
