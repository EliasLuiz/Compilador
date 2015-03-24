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
import java.util.ListIterator;
import java.util.Map;

public class AnalisadorLexico {

    private LinkedHashMap<Integer, ArrayList<Token>> tokens;
    private HashMap<String, String> lexemas;

    public AnalisadorLexico() {
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
        lexemas.put("[", "colch1");
        lexemas.put("]", "colch2");
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

    public ArrayList<Token> analisarExpressao(String linha, Bool isFuncao, Bool isComment) throws IOException {
        char c;
        boolean isString = false, isInt = false, isFloat = false, isVar = false;
        int lexBegin = 0;
        ArrayList<Token> lista = new ArrayList<>();

        for (int i = 0; i < linha.length(); i++) {

            c = linha.charAt(i);

            //Identificacao de comentarios
            if (c == '#') {
                isComment.x = !isComment.x;
                continue;
            }
            //Remocao de comentarios
            if (isComment.x) {
                continue;
            }
            
            //Tratamento de strings
            else if (c == '"') {
                //Caso seja string pode ir qualquer coisa dentro
                if (isString) {
                    lista.add(new Token("str", linha.substring(lexBegin, i-1)));
                } else {
                    lexBegin = i + 1;
                }
                isString = !isString;
            }
            
            //Caso seja uma expressao
            else if (!isString) {
                
                if (c == ')') {
                    lista.add(new Token(lexemas.get(")"), ""));
                    return lista;
                }
                
                //Tratamento de numeros
                if (Character.isDigit(c)) {
                    if (!isVar && !isInt && !isFloat) {
                        isInt = true;
                        lexBegin = i;
                    }
                }
                
                //Tratamento de virgula e ponto para numeros
                else if (((c == ',' && !isFuncao.x) || c == '.') && isInt) {
                    isInt = false;
                    isFloat = true;
                }  
                
                
                else if (Character.isLetter(c) && !isVar) {
                    isVar = true;
                    if (!isInt && !isFloat) {
                        lexBegin = i;
                    } else {
                        isInt = false;
                        isFloat = false;
                    }
                } 
                
                //Gerando tokens para quem estava ativo
                else if(isVar){
                    lista.add(new Token("var", linha.substring(lexBegin, i)));
                    isVar = false;
                } else if(isInt) {
                    lista.add(new Token("int", linha.substring(lexBegin, i)));
                    isInt = false;
                } else if(isFloat) {
                    lista.add(new Token("float", linha.substring(lexBegin, i)));
                    isFloat = false;
                }
                
                
                if (isFuncao.x && c == ',') {
                    isVar = false;
                    isFloat = false;
                    isInt = false;
                }
                
                //Chama a funcao recursivamente para analisar o conteudo
                //do parenteses
                else if (c == '(') {
                    ArrayList<Token> l = new ArrayList<>();
                    Bool aux = new Bool();
                    aux.x = isFuncao.x;
                    //Caso esteja iniciando uma chamada de funcao
                    if (!lista.isEmpty() && lista.get(lista.size() - 1).tipo == "var") {
                        lista.get(lista.size() - 1).tipo = lexemas.get("fun");
                        isFuncao.x = true;
                    //Caso seja apenas uma expressao entre parenteses
                    } else {
                        isFuncao.x = false;
                    }
                    l = analisarExpressao(linha.substring(i + 1), isFuncao, isComment);
                    isFuncao.x = aux.x;
                    lista.add(new Token(lexemas.get("("), ""));
                    for (Token t : l) {
                        lista.add(t);
                    }
                } 
                
                //Caso seja um operador
                else {
                    String c1 = "", c2 = "";
                    if(linha.length() - i > 1)
                        c1 += linha.charAt(i+1);
                    if(linha.length() - i > 2){
                        c2 += linha.charAt(i+2);
                    }
                    String res;
                    if(lexemas.get(c + c1 + c2) != null){
                        res = lexemas.get(c + c1 + c2);
                        i += 2;
                    } else if (lexemas.get(c + c1) != null){
                        res = lexemas.get(c + c1);
                        i += 1;
                    } else if (lexemas.get(c + "") != null){
                        res = lexemas.get(c + "");
                    }
                }
            }
        }
        
        for (ListIterator<Token> iter = lista.listIterator(); iter.hasNext(); ) {
            Token t = iter.next();
            if(t.tipo.equals("var") || t.tipo.equals("fun")){
                if(lexemas.get(t.valor) != null){
                    t.tipo = lexemas.get(t.valor);
                    t.valor = "";
                }
                iter.set(t);
            }
        }

        
        return lista;

    }

    private BufferedReader carregar(String path) throws IOException {
        BufferedReader reader;
        reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(path), "Cp1252"));
        return reader;
    }

    public void salvar(String path) throws IOException {
        FileOutputStream fos = new FileOutputStream(path);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(tokens);
        oos.close();
        fos.close();
    }
    
    public void analisar(String path) throws IOException {
        int nlinha = 0;
        Bool isComment = new Bool(), isFuncao = new Bool();
        isComment.x = false;
        isFuncao.x = false;

        BufferedReader b = carregar(path);

        while (b.ready()) {
            ArrayList<Token> lista = analisarExpressao(b.readLine(), isFuncao, isComment);

            if (!lista.isEmpty()) {
                tokens.put(nlinha, lista);
            }

            nlinha++;
            
            for(Token t : lista)
                System.out.print(t);
            System.out.println("");
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        AnalisadorLexico a = new AnalisadorLexico();
        a.analisar("exemplo.txt");
        //a.salvar("tokens.txt");
    }
}

//Classe criada para que o valor de booleanos pudessem ser utilizado
//atraves de varias chamadas de funcao
class Bool{ public boolean x; }