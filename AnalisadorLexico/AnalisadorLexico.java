package AnalisadorLexico;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class AnalisadorLexico {
    
    private LinkedHashMap<Integer, ArrayList<String>> tokens;
    private HashMap<String, String> lexemas;
    
    public AnalisadorLexico(){        
        tokens = new LinkedHashMap<>();
        lexemas = new HashMap<>();
        
        //############### LEXEMAS DA LINGUAGEM #################
        //Aritmeticos
        lexemas.put("+", "sum");
        lexemas.put("-", "sub");
        lexemas.put("*", "mult");
        lexemas.put("x", "mult");
        lexemas.put("/", "div");
        lexemas.put(":", "div");
        lexemas.put(".", ".");
        lexemas.put(",", ".");
        //Comparativos
        lexemas.put(">", "gt");
        lexemas.put(">=", "gte");
        lexemas.put("=>", "gte");
        lexemas.put("<", "lt");
        lexemas.put("<=", "lte");
        lexemas.put("=<", "lte");
        lexemas.put("==", "eq");
        lexemas.put("!=", "neq");
        //Gerais
        lexemas.put("=", "atrib");
        lexemas.put("int", "int,");
        lexemas.put("float", "float,");
        lexemas.put("str", "str,");
        lexemas.put("var", "id,");
        //Palavras-chave
            //Condicionais
            lexemas.put("se", "cond");
            lexemas.put("então", "initcond");
            lexemas.put("senão", "altcond");
            lexemas.put("fim-se", "endcond");
            //Loops
            lexemas.put("para", "forloop");
            lexemas.put("de", "rng1forloop");
            lexemas.put("até", "rng2forloop");
            lexemas.put("faça", "initforloop");
            lexemas.put("fim-para", "endforloop");
            lexemas.put("enquanto", "whileloop");
            lexemas.put("fim-enquanto", "endwhileloop");
        //#####################################################
        
    }
    
    public void analisar(String path) throws IOException{
        BufferedReader b = carregar("exemplo.txt");
        String linha;
        while(b.ready()){
            linha = b.readLine();
            
            // 1TO DO
            
        }
    }
    
    private BufferedReader carregar(String path) throws IOException{
        BufferedReader reader;
        reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(path), "Cp1252"));
        return reader;
    }
    
    public void salvar(String path) throws IOException{
        FileOutputStream fos = new FileOutputStream(path);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(tokens);
        oos.close();
        fos.close();
    }
    
    public static void main(String[] args) throws IOException, ClassNotFoundException{
        AnalisadorLexico a = new AnalisadorLexico();
        a.analisar("exemplo.txt");
        a.salvar("tokens.txt");
    }
}
