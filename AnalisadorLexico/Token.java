package AnalisadorLexico;

import java.io.Serializable;

public class Token implements Serializable{
    
    String tipo;
    String valor;

    public Token(String tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        if(!valor.isEmpty())
            return "<" + tipo + "," + valor + ">";
        else
            return "<" + tipo + ">";
    }
    
    @Override
    public boolean equals(Object obj){
        Token t = (Token) obj;
        return tipo.equals(t.getTipo()) && valor.equals(t.getValor());
    }
    
}
