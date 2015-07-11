package GeradorCodigo;

import AnalisadorLexico.Token;
import AnalisadorSemantico.ErroSemantico;
import AnalisadorSemantico.Escopo;
import AnalisadorSemantico.Simbolo;
import AnalisadorSemantico.TabelaSimbolos;
import AnalisadorSintatico.ArvoreBinaria;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GeradorCodigo {

    /* VARIAVEIS */
    private LinkedHashMap<Integer, ArrayList<Token>> linhas;
    private HashMap<Integer, ArvoreBinaria<Token>> arvores;
    private TabelaSimbolos tabelaSimbolos;
    private BufferedWriter file;
    private Escopo escopos;
    //Vetor para controle de quem esta em cada um dos 10 "registradores"
    private String[] variaveis;
    int nLinha;

    
    
    /* CONSTRUTORES */
    public GeradorCodigo(String out, String pathTo, String pathA, String pathTa)
            throws IOException, ClassNotFoundException {
        carregar(pathTo, pathA, pathTa);
        this.init(out);
    }
    public GeradorCodigo(String out,
            LinkedHashMap<Integer, ArrayList<Token>> linhas,
            HashMap<Integer, ArvoreBinaria<Token>> arvores,
            TabelaSimbolos tabelaSimbolos)
            throws FileNotFoundException, IOException {
        this.linhas = linhas;
        this.arvores = arvores;
        this.tabelaSimbolos = tabelaSimbolos;
        this.init(out);
    }
    private void init(String out) throws IOException{
        File f = new File(out);
        if (!f.exists())
            f.createNewFile();
        file = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
        variaveis = new String[10];
        escopos = new Escopo();
        for (int i = 0; i < variaveis.length; i++) {
            variaveis[i] = "";
        }
    }

    
    
    /* IO */
    private void carregar(String pathTo, String pathA, String pathTa) 
            throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(pathTo);
        ObjectInputStream ois = new ObjectInputStream(fis);
        linhas = (LinkedHashMap<Integer, ArrayList<Token>>) ois.readObject();
        fis = new FileInputStream(pathA);
        ois = new ObjectInputStream(fis);
        arvores = (HashMap<Integer, ArvoreBinaria<Token>>) ois.readObject();
        fis = new FileInputStream(pathTa);
        ois = new ObjectInputStream(fis);
        tabelaSimbolos = (TabelaSimbolos) ois.readObject();
        fis.close();
        ois.close();
    }

    
    
    /* FUNCOES AUXILIARES */
    //Otimizacao
    private int getRegistrador() {
        for (int i = 0; i < variaveis.length; i++){
            String x = "";
            x = variaveis[i];
            try{
                if (tabelaSimbolos.getSimbolo(x, 0).ultimoUso < nLinha) {
                    return i;
                }
            } catch (Exception e) { 
                //Caso tenha saido do escopo da variavel
                return i;
            }
        }
        return -1;
    }
    private void addVariavel(String nome) throws ErroOtimizacao{
        if(buscaVariavel(nome) != -1)
            return;
        int x = getRegistrador();
        if(x == -1)
            throw new ErroOtimizacao(nLinha, "Quantidade de registradores estourada.");
        try { variaveis[x] = escopos.getVariavel(nome); } 
        catch (ErroSemantico e) {}
    }
    private int buscaVariavel(String nome) {
        try{ nome = escopos.getVariavel(nome); }
        catch (ErroSemantico e) { return -1; }
        for (int i = 0; i < variaveis.length; i++)
            if (variaveis[i].equals(nome))
                return i;
        return -1;
    }
    //Geracao de codigo
    private int indexOf(ArrayList array, Object x, int start, int end) {
        for (int i = start; i <= end; i++)
            if (array.get(i).equals(x))
                return i;
        return -1;
    }
    private int indexOfTipo(ArrayList<Token> array, String tipo, int start, int end) {
        for (int i = start; i <= end; i++)
            if (array.get(i).getTipo().equals(tipo))
                return i;
        return -1;
    }
    
    private String token(Token t) throws ErroOtimizacao {
        switch (t.getTipo()) {
            case "str":
                return "\"" + t.getValor() + "\"";
            case "int":
            case "float":
            case "array":
                return t.getValor();
            case "fun":
                return chamadaFuncao(t).getValor();
            case "id":
                escopos.adicionaVariavel(t.getValor());
                addVariavel(t.getValor());
                return "a" + buscaVariavel(t.getValor());
            case "and":
                return "&&";
            case "or":
                return "||";
            case "not":
                return "!";
            case "def":
                return "function ";
            case "endfor":
            case "endif":
            case "endwhile":
            case "enddef":
                return "}\n";
            case "if":
            case "while":
            case "for":
                return t.getTipo() + "(";
            case "else":
                return "}else{\n";
            case "then":
            case "do":
                return "){\n";
            case "from":
                return "=";
            case "end":
                return "";
            default:
                return t.getTipo();
        }
    }
    private ArrayList<ArrayList<Token>> leArvore() {
        ArvoreBinaria<Token> arvore = arvores.get(nLinha);
        ArrayList<ArrayList<Token>> lista = new ArrayList<>();
        if (arvore == null)
            return null;
        int cont = 0;
        while(arvore.altura() != 1){
            ArrayList<Token> x = arvore.subarvoreReplace(new Token("id", ".aux" + cont)).inOrdem();
            lista.add(x);
            //Se a atribuicao final
            if(!x.contains(new Token("=", ""))){
                lista.get(lista.size()-1).add(0, new Token("=", ""));
                lista.get(lista.size()-1).add(0, new Token("id", ".aux" + cont));
                escopos.adicionaVariavel(".aux" + cont);
                try{
                    tabelaSimbolos.addSimbolo(new Simbolo(".aux" + cont, "", 
                            escopos.getVariavel(".aux" + cont), nLinha, false, false));
                } catch (ErroSemantico e){}
            }
            cont++;
        }
        return lista;
    }
    private void declaraVariaveis(){
        String s = "var ";
        for (int i = 0; i < 10; i++)
            s+="a"+i+",";
        try { file.append(s.substring(0,s.length()-1) + ";\n"); 
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(3);
        }
    }
    private Token chamadaFuncao(Token funcao) throws ErroOtimizacao{
        //funcao(x,y) => funcao(a0,a1)
        String[] aux = funcao.getValor().split("[(]");
        String nome = aux[0];
        String param = aux[1].substring(0, aux[1].length()-1);
        String[] parametros = param.split(",");
        String s = nome += "(";
        for(String p : parametros){
            if("".equals(p))
                continue;
            String v = "";
            //Se e numero
            try{
                Integer.parseInt(p);
                v = p;
            } catch (NumberFormatException e){}
            try{
                Double.parseDouble(p);
                v = p;
            } catch (NumberFormatException e){}
            if(v != ""){}
            //Se e numero ou vetor
            else if(p.contains("\"") ||
               p.contains("["))
                v = p;
            //Se e funcao
            else if(p.contains("("))
                v = chamadaFuncao(new Token("fun", p)).getValor();
            //Se e id
            else
                v = token(new Token("id", p));
            s += v + ",";
        }
        s = ("".equals(parametros[0]) ? s : s.substring(0,s.length()-1)) + ")";
        return new Token("fun", s);
    }
    private void atribuicao() throws ErroOtimizacao {
        ArrayList<ArrayList<Token>> listas = leArvore();
        for (ArrayList<Token> lista : listas){
            String s = "";
            for (Token t : lista)
                s += token(t);
            try {
                file.append(s+";\n");
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(3);
            }
        }
    }
    
    private void executarLinha(ArrayList<Token> linha) throws IOException, ErroOtimizacao {
        //casos especiais:
        
        //  atribuicao
        if(linha.contains(new Token("=", ""))){
            
            //tela.mensagem
            if(linha.get(0).getValor().startsWith("tela")){
                String s = "alert(";
                for (int i = 2; i < linha.size(); i++) {
                    s += token(linha.get(i));
                }
                file.append(s + ");\n");
                return;
            }
            
            atribuicao();
        }
        
        //  ate (to):
        //      conferir se parada maior ou menor
        //      imprimir "var <=|>= num; var++|--"
        else if(linha.contains(new Token("for", ""))){
            int idx = linha.indexOf(new Token("to", ""));
            String op;
            if (Integer.parseInt(linha.get(idx-1).getValor()) <
                Integer.parseInt(linha.get(idx+1).getValor())) 
                op = "<=";
            else
                op = ">=";
            String s = "";
            for (int i = 0; i < linha.size(); i++) {
                Token t = linha.get(i);
                if("to".equals(t.getTipo()))
                    s+= ";" + token(linha.get(1)) + op;
                else if("do".equals(t.getTipo()))
                    s+= ";" + token(linha.get(1)) + ("<=".equals(op) ? "++" : "--") + token(t);
                else
                    s+=token(t);
            }
            file.append(s);
        }
        
        //  declaracao de vetor
        // vetor m[3] => m = [0,0,0];
        // vetor m[2][3] => m = [[0,0,0],[0,0,0]];
        else if(linha.contains(new Token("vet",""))){
            int idx2 = indexOf(linha, new Token("[", ""), 3, linha.size()-1);
            String s = linha.get(1).getValor() + " = [";
            for(int i = 0; i < Integer.parseInt(linha.get(3).getValor()); i++){
                if(idx2 == -1){
                    s += "0,";
                }
                else{
                    s+="[";
                    for(int j = 0; j < Integer.parseInt(linha.get(6).getValor()); j++){
                        s+="0,";
                    }
                    s = s.substring(0,s.length()-1) + "],";
                }
            }
            s = s.substring(0,s.length()-1) + "];\n";
            file.append(s);
        }
        
        else{
            for(Token t : linha)
                file.append(token(t));
        }
        
    }
    private void executarFuncao(String nomeFun) 
            throws ErroSemantico, IOException, ErroOtimizacao {

        //Backup dos escopos antigos e das variaveis em uso
        String[] backupVariaveis = new String[10];
        for (int i = 0; i < backupVariaveis.length; i++) {
            backupVariaveis[i] = variaveis[i];
            variaveis[i] = "";
        }
        Escopo backup = escopos;
        escopos = new Escopo();
        escopos.idEscopo = backup.idEscopo;
        int backupLinha = nLinha;

        boolean naFuncao = false;

        for (Map.Entry<Integer, ArrayList<Token>> entrySet : linhas.entrySet()) {
            nLinha = entrySet.getKey();
            ArrayList<Token> linha = entrySet.getValue();

            //Se chegou na funcao
            if (linha.contains(new Token("fun", nomeFun))
                    && linha.contains(new Token("def", ""))) {

                naFuncao = true;
                escopos.adicionaEscopo();
                
                //Escreve declaracao da funcao
                String chamada = "";
                for (Token t : linha) {
                    if("fun".equals(t.getTipo()))
                        chamada += t.getValor();
                    else
                        chamada += token(t);
                }
                file.append(chamada + "{\n");
                
                //Declara as variaveis
                declaraVariaveis();
                
                continue;
            }

            //Se nao esta na funcao
            if (!naFuncao) {
                continue;
            }

            //Se saiu da funcao
            if (linha.contains(new Token("enddef", ""))) {
                file.append(token(new Token("enddef", "")));
                break;
            }

            //Se encontrou valor de retorno
            if ("=".equals(linha.get(1).getTipo())
                    && nomeFun.equals(linha.get(0).getValor())) {
                String ret = "return ";
                for (int i = 2; i < linha.size(); i++) {
                    ret += token(linha.get(i));
                }
                file.append(ret + ";\n");
                continue;
            }
            
            executarLinha(linha);
        }

        //Restaura backup de escopo atualizando os ids
        backup.idEscopo = escopos.idEscopo;
        escopos = backup;
        nLinha = backupLinha;
    }

    
    
    /* FUNCAO PRINCIPAL */
    public void executar() {
        boolean isFuncao = false;
        
        //Declara as variaveis que serao usadas
        declaraVariaveis();
        
        //Analise do programa
        for (Map.Entry<Integer, ArrayList<Token>> entrySet : linhas.entrySet()) {
            nLinha = entrySet.getKey();
            ArrayList<Token> linha = entrySet.getValue();

            //Pula as definicoes de funcao
            if (linha.contains(new Token("def", ""))) {
                isFuncao = true;
            } else if (linha.contains(new Token("enddef", ""))) {
                isFuncao = false;
                continue;
            }

            if (isFuncao) {
                continue;
            }
            
            if (linha.contains(new Token("if", "")) ||
                linha.contains(new Token("while", "")) ||
                linha.contains(new Token("for", ""))) {
                escopos.adicionaEscopo();
            }
            else if (linha.contains(new Token("endif", "")) ||
                linha.contains(new Token("endwhile", "")) ||
                linha.contains(new Token("endfor", ""))) {
                escopos.removeEscopo();
            }
            

            //Se encontrou chamada a uma funcao a analisa
            int indexFun = indexOfTipo(linha, "fun", 0, linha.size() - 1);
            while (indexFun != -1) {
                try {
                    escopos.getVariavel("." + linha.get(indexFun).getValor());
                } catch (ErroSemantico e) { //Se nao foi declarado
                    escopos.adicionaVariavel("." + linha.get(indexFun).getValor());
                    try {
                        executarFuncao(linha.get(indexFun).getValor());
                    } catch (ErroSemantico ex) {
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        System.exit(3);
                    } catch (ErroOtimizacao ex) {
                        ex.printStackTrace();
                        System.exit(4);
                    }
                }
                //Caso tenha mais de uma chamada de funcao na mesma linha
                indexFun = indexOfTipo(linha, "fun", indexFun + 1, linha.size() - 1);
            }
            
            try {
                executarLinha(linha);
            } catch (IOException ex) {
                ex.printStackTrace();
                System.exit(3);
            } catch (ErroOtimizacao ex) {
                ex.printStackTrace();
                System.exit(4);
            }
            
        }
        try {
            file.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(3);
        }
    }
}
