package AnalisadorSintatico;

import java.io.Serializable;
import java.util.Iterator;
import java.util.ArrayList;


public class ArvoreBinaria<T> implements Serializable{
    
    private ArvoreBinaria<T> esq, dir, pai;
    private T nodo;
    
    public ArvoreBinaria(T node){
        esq = null;
        dir = null;
        pai = null;
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
        esq.pai = this;
        this.esq = esq;
    }

    public ArvoreBinaria<T> getDir() {
        return dir;
    }

    public void setDir(ArvoreBinaria<T> dir) {
        dir.pai = this;
        this.dir = dir;
    }
    
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
    
    @Override
    public String toString(){
        return nodo.toString();
    }
    
    public void print(){
        int alt = altura();
        int esp = (int) (Math.pow(2, alt));
        ArrayList<ArvoreBinaria<T>> fila;
        for (int i = 0; i < alt; i++) {
            fila = this.folhas(i);
            for (int j = 0; j < esp/2-1; j++)
                System.out.print("\t");
            for(Iterator<ArvoreBinaria<T>> iterator = fila.iterator(); iterator.hasNext();) {
                ArvoreBinaria<T> next = iterator.next();
                if(next != null)
                    System.out.print(next.toString());
                for (int j = 0; j < esp; j++)
                    System.out.print("\t");
            }
            esp = esp / 2;
            System.out.println("");
        }
    }
    public ArrayList<ArvoreBinaria<T>> folhas(int altura){
        ArrayList<ArvoreBinaria<T>> fila = new ArrayList<>();
        if(altura == 0)
            fila.add(this);
        else{
            ArrayList<ArvoreBinaria<T>> fAux;
            
            if(esq != null)
                fAux = esq.folhas(altura-1);
            else{
                fAux = new ArrayList<>();
                fAux.add(null);
            }
            for (ArvoreBinaria<T> fila1 : fAux) {
                fila.add(fila1);
            }
            
            if(dir != null)
                fAux = dir.folhas(altura-1);
            else{
                fAux = new ArrayList<>();
                fAux.add(null);
            }
            for (ArvoreBinaria<T> fila1 : fAux) {
                fila.add(fila1);
            }
        }
        return fila;
    }
    
    public int altura(){
        int hEsq = 0, hDir = 0;
        if(esq != null)
            hEsq = esq.altura();
        if(dir != null)
            hDir = dir.altura();
        return 1 + (hEsq > hDir ? hEsq : hDir);
    }
    
    public ArvoreBinaria<T> subarvoreReplace(T substituto){
        if(esq.altura() > 1)
            return esq.subarvoreReplace(substituto);
        else if(dir.altura() > 1)
            return dir.subarvoreReplace(substituto);
        else{
            ArvoreBinaria<T> aux = this;
            this.nodo = substituto;
            this.esq = null;
            this.dir = null;
            return aux;
        }
    }
}
