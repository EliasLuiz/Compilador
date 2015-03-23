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
    
    private LinkedHashMap<Integer, ArrayList<Token>> tokens;
    private HashMap<String, String> lexemas;
    
    public AnalisadorLexico(){
        tokens = new LinkedHashMap<>();
        lexemas = new HashMap<>();
        
        //############### LEXEMAS DA LINGUAGEM #################
        //Aritmeticos
        lexemas.put("+", "sum");
        lexemas.put("-", "sub");
        lexemas.put("*", "mult");
        lexemas.put(" x ", "mult");
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
        lexemas.put("(", "paren1");
        lexemas.put(")", "paren2");
        lexemas.put("=", "atrib");
        lexemas.put("int", "int");
        lexemas.put("float", "float");
        lexemas.put("str", "str,");
        lexemas.put("var", "var");
        lexemas.put("fun", "fun");
        lexemas.put("vetor", "decl");
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
    
    public ArrayList<Token> analisarExpressao(String linha, boolean isFuncao) throws IOException{
        char c;
        boolean isString = false, isInt = false, isFloat = false, isVar = false,
                isComment = false;
        int lexBegin;
        ArrayList<Token> lista = new ArrayList<>();
            
            for(int i = 0; i < linha.length(); i++){
                
                c = linha.charAt(i);
                
                //Identificacao de comentarios
                if(c == '#'){
                    isComment = !isComment;
                    continue;
                }
                //Remocao de comentarios
                if(isComment)
                    continue;
                //Tratamento de strings
                else if(c == '"'){
                    if(isString){
                        //Cria token de string
                        //linha[lexBegin:i-1]
                    }
                    else{
                        lexBegin = i+1;
                    }
                    isString = !isString;
                }
                //Caso seja string pode ir qualquer coisa dentro
                else if(!isString){
                    //Tratamento de numeros
                    if(Character.isDigit(c)){
                        if(!isVar && !isInt && !isFloat){
                            isInt = true;
                            lexBegin = i;
                        }
                    }
                    //Tratamento de virgula e ponto para numeros
                    else if (((c == ',' && !isFuncao) || c == '.') && isInt) {
                        isInt = false;
                        isFloat = true;
                    }
                    else if(Character.isLetter(c) && !isVar){
                        isVar = true;
                        if(!isInt && !isFloat){
                            lexBegin = i;
                        }
                        else{
                            isInt = false;
                            isFloat = false;
                        }
                    }
                    else if(c == '('){
                        if(!lista.isEmpty() && lista.get(lista.size()-1).tipo == "var"){
                            lista.get(lista.size()-1).tipo = lexemas.get("fun");
                            lista.add(new Token(lexemas.get("("), ""));
                            ArrayList<Token> l = analisarExpressao(linha.substring(i+1), true);
                        }
                    }
                    
//                   else
//                        new token(lexemas.get(c), "")
                }
            }
            
            return lista;
            
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
    
    public void analisar(String path) throws IOException{
        int nlinha = 0, index = 0;
        
        BufferedReader b = carregar(path);
        
        while(b.ready()){
            ArrayList<Token> lista = analisarExpressao(b.readLine(), false);
                        
            
            if(!lista.isEmpty()){
                tokens.put(index, lista);
                index++;
            }
            
            nlinha++;
        }
    }
    
    public static void main(String[] args) throws IOException, ClassNotFoundException{
        AnalisadorLexico a = new AnalisadorLexico();
        a.analisar("exemplo.txt");
        //a.salvar("tokens.txt");
    }
}
