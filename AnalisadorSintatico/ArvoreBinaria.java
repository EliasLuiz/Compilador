package AnalisadorSintatico;

import java.util.ArrayList;


public class ArvoreBinaria<T> {
    
    private ArvoreBinaria<T> esq, dir;
    private T nodo;
    
    public ArvoreBinaria(){
        esq = null;
        dir = null;
        nodo = null;
    }
    
    public ArvoreBinaria(T node){
        esq = null;
        dir = null;
        nodo = node;
    }
    
    public void setNodo(T x){
        nodo = x;
    }
    
    public T getNodo(){
        return nodo;
    }

    public ArvoreBinaria<T> getEsq() {
        return esq;
    }

    public void setEsq(ArvoreBinaria<T> esq) {
        this.esq = esq;
    }

    public ArvoreBinaria<T> getDir() {
        return dir;
    }

    public void setDir(ArvoreBinaria<T> dir) {
        this.dir = dir;
    }
    
//    public void insereEsq(T x){
//        esq = new ArvoreBinaria<>();
//        esq.nodo = x;
//    }
//    
//    public void insereDir(T x){
//        dir = new ArvoreBinaria<>();
//        dir.nodo = x;
//    }
    
    public ArrayList<T> inOrdem(){
        ArrayList<T> lista = new ArrayList<>();
        if(esq != null){
            ArrayList<T> x = esq.inOrdem();
            for(T i: x){
                lista.add(i);
            }
        }
        if(nodo != null)
            lista.add(nodo);
        if(dir != null){
            ArrayList<T> x = dir.inOrdem();
            for(T i: x){
                lista.add(i);
            }
        }
        return lista;
    }
}
