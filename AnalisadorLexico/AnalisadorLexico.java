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
import java.util.Map;
import java.util.Stack;

public class AnalisadorLexico {

    /* VARIAVEIS */
    private LinkedHashMap<Integer, ArrayList<Token>> tokens;
    private HashMap<String, String> lexemas;
    private String path;

    
    
    /* CONSTRUTOR */
    public AnalisadorLexico(String path) {
        tokens = new LinkedHashMap<>();
        
        Lexemas lex = new Lexemas();
        lexemas = lex.getLexemas();
        
        this.path = path;
    }
    
    
    
    /* IO */
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
    public LinkedHashMap<Integer, ArrayList<Token>> getTokens(){
        return tokens;
    }
    
    
    
    /* FUNCAO PRINCIPAL */
    public void analisar(boolean print) throws IOException {
        
        BufferedReader b = carregar(path);
            
        //variaveis de controle de estado
        boolean isString = false, isInt = false, isFloat = false, isVar = false,
                isComment = false;
        int nlinha = 1;
        Stack<Boolean> isFuncao = new Stack<>();
        isFuncao.push(false);
            
        //Buffer de string multi-linha
        String buffer = "";

        while (b.ready()) {
            
            //variaveis de leitura
            String linha;
            linha = b.readLine();
            char c;
            
            //variaveis auxiliares
            int lexBegin = 0;
            
            //lista para armazenar os tokens
            ArrayList<Token> lista = new ArrayList<>();

            for (int i = 0; i < linha.length(); i++) {

                c = linha.charAt(i);

                //Identificacao de comentarios
                if (c == '#' && !isString) {
                    isComment = !isComment;
                    //Se comecao um comentario logo apos uma variavel ou numero
                    //gera o token pro numero
                    if(isVar){
                        Token t;
                        //caso seja palavra chave
                        if(lexemas.get(linha.substring(lexBegin, i)) != null)
                            t = new Token(lexemas.get(linha.substring(lexBegin, i)), "");
                        //caso seja variavel
                        else 
                            t = new Token(lexemas.get("var"), 
                                    linha.substring(lexBegin, i));
                        lista.add(t);
                        isVar = false;
                    } else if(isInt) {
                        lista.add(new Token(lexemas.get("int"), 
                                linha.substring(lexBegin, i)));
                        isInt = false;
                    } else if(isFloat) {
                        lista.add(new Token(lexemas.get("float"), 
                                linha.substring(lexBegin, i).replace(",", ".")));
                        isFloat = false;
                    }
                    continue;
                }
                //Remocao de comentarios
                if (isComment) {
                    continue;
                }

                //Tratamento de strings
                else if (c == '"') {
                    //Caso seja string pode ir qualquer coisa dentro
                    if (isString) {
                        lista.add(new Token(lexemas.get("str"), 
                                buffer + linha.substring(lexBegin, i)));
                        buffer = "";
                    } else {
                        lexBegin = i + 1;
                    }
                    isString = !isString;
                }

                //Caso seja uma expressao
                else if (!isString) {

                    //Tratamento de numeros
                    if (Character.isDigit(c)) {
                        if (!isVar && !isInt && !isFloat) {
                            isInt = true;
                            lexBegin = i;
                        }
                    }

                    //Tratamento de virgula e ponto para numeros
                    else if (((c == ',' && !isFuncao.peek()) 
                              || c == '.')
                             && isInt) {
                        isInt = false;
                        isFloat = true;
                    }

                    //Detecta inicio de uma possivel variavel
                    else if (Character.isLetter(c) || c == '_') {
                        
                        if(!isVar){
                            if (isInt) {
                                lista.add(new Token(lexemas.get("int"), 
                                        linha.substring(lexBegin, i)));
                                isInt = false;
                            } else if(isFloat) {
                                lista.add(new Token(lexemas.get("float"), 
                                        linha.substring(lexBegin, i).replace(",", ".")));
                                isFloat = false;
                            }
                        
                            //Gambiarra para fazer o 5x5 funcionar
                            if(c == 'x' && 
                              lista.size() > 0 &&
                              ("int".equals(lista.get(lista.size()-1).tipo) || 
                                  "float".equals(lista.get(lista.size()-1).tipo) ||
                                  ")".equals(lista.get(lista.size()-1).tipo) ) &&
                              i < linha.length()-1 &&
                              (Character.isDigit(linha.charAt(i+1)) || '(' == 
                                  linha.charAt(i+1))){

                                lista.add(new Token(lexemas.get("x"), ""));
                                continue;
                            }
                            
                            isVar = true;
                            lexBegin = i;
                        }
                        else
                            continue;        
                    }

                    //Gerando tokens para quem estava ativo
                    else if(isVar){
                        Token t;
                        //caso seja fim-estrutura
                        if(linha.length()-i>2 &&
                                "fim-se".equals(linha.substring(lexBegin, i+3))){
                            i += 3;
                            continue;
                        }
                        else if(linha.length()-i>4 &&
                                "fim-para".equals(linha.substring(lexBegin, i+5))){
                            i += 5;
                            continue;
                        }
                        else if(linha.length()-i>8 &&
                                "fim-enquanto".equals(linha.substring(lexBegin, i+9))){
                            i += 9;
                            continue;
                        }
                        else if(linha.length()-i>6 &&
                                "fim-funcao".equals(linha.substring(lexBegin, i+7))){
                            i += 7;
                            continue;
                        }
                        //caso tenha um . no meio do nome da variavel
                        else if(c == '.'
                                && Character.isLetter(linha.charAt(i+1))){
                            i += 1;
                            continue;
                        }
                        //caso seja palavra chave
                        else if(lexemas.get(linha.substring(lexBegin, i)) != null)
                            t = new Token(lexemas.get(linha.substring(lexBegin, i)), "");
                        //caso seja variavel
                        else
                            t = new Token(lexemas.get("var"), 
                                    linha.substring(lexBegin, i));
                        lista.add(t);
                        isVar = false;
                    } else if(isInt) {
                        lista.add(new Token(lexemas.get("int"), 
                                linha.substring(lexBegin, i)));
                        isInt = false;
                    } else if(isFloat) {
                        lista.add(new Token(lexemas.get("float"), 
                                linha.substring(lexBegin, i).replace(",", ".")));
                        isFloat = false;
                    }

                    //Separa os parametros de uma funcao
                    if (isFuncao.peek() && c == ',') {
                        isVar = false;
                        isFloat = false;
                        isInt = false;
                        lista.add(new Token(",", ""));
                    }
                    

                    //Usa uma pilha para para analisar o conteudo do parenteses
                    //se e uma funcao ou se e apenas uma expressao
                    else if (c == '(') {
                        ArrayList<Token> l = new ArrayList<>();
                        //Caso esteja iniciando uma chamada de funcao
                        if (!lista.isEmpty() && 
                                lexemas.get("var").equals(lista.get(lista.size() - 1).tipo)) {
                            lista.get(lista.size() - 1).tipo = lexemas.get("fun");
                            isFuncao.push(true);
                        //Caso seja apenas uma expressao entre parenteses
                        } else {
                            isFuncao.push(false);
                        }
                        lista.add(new Token(lexemas.get("("), ""));
                    }

                    else if (c == ')') {
                        lista.add(new Token(lexemas.get(")"), ""));
                        isFuncao.pop();
                    } 

                    //Caso seja um operador
                    else if (!isVar && !isInt && !isFloat){
                        String c1 = "", c2 = "";
                        if(linha.length() - i > 1)
                            c1 += linha.charAt(i+1);
                        if(linha.length() - i > 2){
                            c2 += linha.charAt(i+2);
                        }
                        String res = "";
                        if(lexemas.get(c + c1 + c2) != null){
                            res = lexemas.get(c + c1 + c2);
                            i += 2;
                        } else if (lexemas.get(c + c1) != null){
                            res = lexemas.get(c + c1);
                            i += 1;
                        } else if (lexemas.get(c + "") != null){
                            res = lexemas.get(c + "");
                        }
                        if(!res.equals(""))
                            lista.add(new Token(res, ""));
                    }
                }
            }
            
            //Caso seja uma string multi-linha
            if(isString){
                buffer = linha.substring(lexBegin) + "\n";
            }
            
            
            //Caso a linha termine em um token
            if(isVar){
                Token t;
                //Caso seja palavra chave
                if(lexemas.get(linha.substring(lexBegin)) != null)
                    t = new Token(lexemas.get(linha.substring(lexBegin)), "");
                //Caso seja variavel
                else 
                    t = new Token(lexemas.get("var"), 
                            linha.substring(lexBegin));
                lista.add(t);
                isVar = false;
            } else if(isInt) {
                lista.add(new Token(lexemas.get("int"), 
                        linha.substring(lexBegin)));
                isInt = false;
            } else if(isFloat) {
                lista.add(new Token(lexemas.get("float"), 
                        linha.substring(lexBegin).replace(",", ".")));
                isFloat = false;
            }
            
            
            //Caso nao seja uma instrucao multi-linha
            //adiciona token <endinstr> informando fim da instrucao
            if(!isString && isFuncao.size()<=1 && !lista.isEmpty()){
                lista.add(new Token(lexemas.get(".endinstr"), ""));
            }

            //Se houverem tokens na linha adiciona no hashmap
            if (!lista.isEmpty()) {
                tokens.put(nlinha, lista);
            }

            nlinha++;
            
        }
        
        //Caso queira imprimir os tokens no console
        if(print){
            System.out.println("\n\nAnalise Lexica:");
            for(Map.Entry<Integer, ArrayList<Token>> entry: tokens.entrySet()){
                System.out.print(entry.getKey() + "\t");
                for(Token t : entry.getValue())
                    System.out.print(t + " ");
                System.out.println("");
            }
            System.out.println("\n\n");
        }
    }
}
