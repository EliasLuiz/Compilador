package AnalisadorSintatico;

import AnalisadorLexico.Lexemas;
import AnalisadorLexico.Token;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnalisadorSintatico {

    /* VARIAVEIS */
    protected LinkedHashMap<Integer, ArvoreBinaria<Token>> arvores;
    protected LinkedHashMap<Integer, ArrayList<Token>> linhas;
    protected HashMap<String, String> lexemas;

    
    
    /* CONSTRUTORES */
    public AnalisadorSintatico(String path) throws IOException, ClassNotFoundException {
        arvores = new LinkedHashMap<>();
        carregar(path);
        
        Lexemas lex = new Lexemas();
        lexemas = lex.getLexemas();
    }
    public AnalisadorSintatico(LinkedHashMap<Integer, ArrayList<Token>> tokens) 
            throws IOException, ClassNotFoundException {
        arvores = new LinkedHashMap<>();
        linhas = tokens;
        
        Lexemas lex = new Lexemas();
        lexemas = lex.getLexemas();
    }

    
    
    /* IO */
    protected void carregar(String path) throws IOException, ClassNotFoundException {
        FileInputStream fos = new FileInputStream(path);
        ObjectInputStream oos = new ObjectInputStream(fos);
        linhas = (LinkedHashMap<Integer, ArrayList<Token>>) oos.readObject();
        fos.close();
        oos.close();
    }
    public void salvar(String path) throws IOException {
        FileOutputStream fos = new FileOutputStream(path);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(arvores);
        oos.close();
        fos.close();
    }
    public LinkedHashMap<Integer, ArvoreBinaria<Token>> getArvores(){
        return arvores;
    }
    
    
    
    /* FUNCOES AUXILIARES */
    protected int indexOf(ArrayList<Object> array, Object x){
        for(int i = 0; i < array.size(); i++)
            if(array.get(i) == x)
                return i;
        return -1;
    }
    protected int indexOf(ArrayList<Object> array, Object x, int start, int end){
        for(int i = start; i < end; i++)
            if(array.get(i) == x)
                return i;
        return -1;
    }
    
    
    
    /* IDENTIFICADORES DE GRAMATICA */
    protected ArvoreBinaria<Token> termo(ArrayList<Token> linha, int start, int end) 
            throws ErroSintatico{
        if("(".equals(linha.get(start).getTipo()) &&
           ")".equals(linha.get(end).getTipo()))
            return condicao(linha, start+1, end-1);
        else if(start == end && 
                lexemas.get("var").equals(linha.get(start).getTipo()) ||
                lexemas.get("float").equals(linha.get(start).getTipo()) ||
                lexemas.get("int").equals(linha.get(start).getTipo()) ||
                lexemas.get("verdadeiro").equals(linha.get(start).getTipo()) ||
                lexemas.get("falso").equals(linha.get(start).getTipo()))
            return new ArvoreBinaria<>(linha.get(start));
        else if(lexemas.get("fun").equals(linha.get(start).getTipo()) &&
                "(".equals(linha.get(start+1).getTipo()) &&
                ")".equals(linha.get(end).getTipo()))
            return funcao(linha, start, end);
        else
            throw new ErroSintatico("Termo inválido");
    }
    protected ArvoreBinaria<Token> funcao(ArrayList<Token> linha, int start, int end){
        
    }
    
    
    
    /* FUNCAO PRINCIPAL */
    public void analisar(boolean print){
        
        /* Analise do codigo linha a linha */
        
        if(print){
            System.out.println("\n\nAnalise Sintatica:");
            for(Map.Entry<Integer, ArvoreBinaria<Token>> entry: arvores.entrySet()){
                System.out.println(entry.getKey());
                entry.getValue().print();
                System.out.println("\n");
            }
            System.out.println("\n\n");
        }
    }
}
