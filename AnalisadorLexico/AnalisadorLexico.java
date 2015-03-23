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

    public AnalisadorLexico() {
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

    public void analisar(String path) throws IOException {
        BufferedReader b = carregar("exemplo.txt");
        String linha;

        int nlinha = 1, index = 0;
        char c;
        
        boolean isComment = false, isNum = false, isVar = false, isString = false;
        int lexStart;

        while (b.ready()) {
            linha = b.readLine();

            System.out.print(nlinha + "  ");

            for (int i = 0; i < linha.length(); i++) {

                c = linha.charAt(i);
                
                /*
                    MUDAR DE IF PARA SWITCH ?
                */

                //Identificacao e "pulo" dos comentarios
                if (c == '#') {
                    isComment = !isComment;
                    continue;
                }
                if (isComment) {
                    continue;
                }

                //Identificacao de strings
                if (c == '"') {
                    if (isString) {
                        //gera token de string
                        //linha[lexStart:i]
                    } else {
                        lexStart = i+1;
                    }

                    isString = !isString;
                }

                //Tratamento de espacos em branco
                if ( ( c == ' ' ) ||
                     ( i == linha.length() - 1 ) ) {
                    if (isNum) {
                        //gera token de numero
                        //linha[lexStart:i]
                        isNum = false;
                    } else if (isVar) {
                        //gera token de variavel
                        //linha[lexStart:i]
                        isVar = false;
                    }
                }
                
                

                //TO DO
                System.out.print(c);

            }

            System.out.println("");

            // TO DO
            nlinha++;
        }
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

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        AnalisadorLexico a = new AnalisadorLexico();
        a.analisar("exemplo.txt");
        a.salvar("tokens.txt");
    }
}
