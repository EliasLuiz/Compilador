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

public class AnalisadorSintatico {

    private LinkedHashMap<Integer, ArvoreBinaria<Token>> arvores;
    private LinkedHashMap<Integer, ArrayList<Token>> linhas;
    private HashMap<String, String> lexemas;

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
    
    private void carregar(String path) throws IOException, ClassNotFoundException {
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
    
    private ArvoreBinaria<Token> termo(ArrayList<Token> linha, int start, int end){
        if("(".equals(linha.get(start).getTipo()) &&
           ")".equals(linha.get(end).getTipo())){
            return condicao(linha, start+1, end-1);
        }
        else if(start == end &&
                .equals(linha.get(start).getTipo())
    }
    
    
    
}
