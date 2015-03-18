package AnalisadorLexico;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnalisadorLexico {
    
    LinkedHashMap<Integer, ArrayList<String>> tokens;
    HashMap<String, String> lexemas;
    
    public AnalisadorLexico(String path){        
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
        lexemas.put("<", "lt");
        lexemas.put("=<", "lte");
        lexemas.put("==", "eq");
        lexemas.put("!=", "eq");
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
    
    private static BufferedReader carrega (String path) throws IOException{
        BufferedReader reader;
        reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(path), "Cp1252"));
        return reader;
    }
    
    private static void salva(String path) throws IOException{
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(path + ".lex"), "Cp1252"));
        
        //Serializa o linkedhashmap de tokens e salva
        
    }
    
    public static void main(String[] args) {
        BufferedReader reader;
        try {
            reader = carrega("teste.txt" /*pegar argumento args*/ );
        } catch (IOException ex) {
            Logger.getLogger(AnalisadorLexico.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        
    }
}
