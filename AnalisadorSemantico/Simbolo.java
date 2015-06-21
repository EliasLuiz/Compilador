package AnalisadorSemantico;

import java.util.ArrayList;

public class Simbolo {
    public String nome;
    public String tipo;
    public String hash;
    public int ultimoUso;
    public boolean isFuncao;
    public boolean isVetor;
    
    ArrayList<String> parametros;
    //Parametros armazena o tipo dos parametros da funcao

    public Simbolo(String nome, String tipo, String hash, int ultimoUso, boolean isFuncao, boolean isVetor) {
        this.nome = nome;
        this.tipo = tipo;
        this.hash = hash;
        this.ultimoUso = ultimoUso;
        this.isFuncao = isFuncao;
        this.isVetor = isVetor;
        this.parametros = null;
        if(isFuncao)
            this.parametros = new ArrayList<>();
    }
    
    public boolean equals(Simbolo x){
        return hash.equals(x.hash);
    }
    
    public ArrayList<String> getParametros(){
        return parametros;
    }
    
    public void addParametro(String tipoParametro){
        parametros.add(tipoParametro);
    }
}
