package AnalisadorSintatico;

import java.util.ArrayList;


public class ArvoreBinaria<T> {
    
    public ArvoreBinaria esq, dir;
    public T nodo;
    
    public ArvoreBinaria(){
        esq = null;
        dir = null;
        nodo = null;
    }
    
    public void setNodo(T x){
        nodo = x;
    }
    
    public T getNodo(){
        return nodo;
    }
    
    public void insereEsq(ArvoreBinaria x){
        esq = x;
    }
    
    public void insereDir(ArvoreBinaria x){
        dir = x;
    }
    
    public ArrayList<T> inOrdem(){
        ArrayList<T> lista = new ArrayList<>();
        if(esq != null)
            for(T i: esq.inOrdem()){
                lista.add(i);
            }
        if(nodo != null)
            lista.add(nodo);
        if(dir != null)
            for(T i: dir.inOrdem()){
                lista.add(i);
            }
        return lista;
    }
    
    
}
