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
    private void escrever(String linha) {
        try {
            file.write(linha + "\n");
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(3);
        }
    }

    
    
    /* FUNCOES AUXILIARES */
    //Otimizacao
    private int getRegistrador(int linha) {
        try {
            for (int i = 0; i < variaveis.length; i++)
                if (variaveis[i].isEmpty() || 
                    tabelaSimbolos.getSimbolo(escopos.getVariavel(variaveis[i]), 0).ultimoUso < linha) {
                    return i;
                }
        } catch (ErroSemantico e) {}
        return -1;
    }
    private void addVariavel(String nome, int linha) throws ErroOtimizacao{
        if(buscaVariavel(nome) != -1)
            return;
        int x = getRegistrador(linha);
        if(x == -1)
            throw new ErroOtimizacao(linha, "Quantidade de registradores estourada.");
        variaveis[x] = nome;
    }
    private int buscaVariavel(String nome) {
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
            case "int":
            case "float":
            case "fun":
                return t.getValor();
            case "id":
                addVariavel(t.getValor(), nLinha);
                return "a" + buscaVariavel(t.getValor()) + " ";
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
                return "}";
            case "if":
            case "while":
            case "for":
                return t.getTipo() + "(";
            case "then":
            case "do":
                return "){";
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
        int linhaFinal = nLinha + arvore.nOperacoes();
        while(arvore.altura() != 1){
            lista.add(arvore.subarvoreReplace(new Token("id", ".aux" + cont)).inOrdem());
            lista.get(lista.size()-1).add(0, new Token("=", ""));
            lista.get(lista.size()-1).add(0, new Token("id", ".aux" + cont));
            escopos.adicionaVariavel(".aux" + cont);
            try{
                tabelaSimbolos.addSimbolo(new Simbolo(".aux" + cont, "", 
                        escopos.getVariavel(".aux" + cont), linhaFinal, false, false));
            } catch (ErroSemantico e){}
        }
        return lista;
    }
    private void declaraVariaveis(){
        for (int i = 0; i < 10; i++)
            try { file.append("var a" + i + ";"); 
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(3);
            }
    }
    private void atribuicao() throws ErroOtimizacao {
        ArrayList<ArrayList<Token>> listas = leArvore();
        for (ArrayList<Token> lista : listas){
            try {
                for (Token t : lista)
                    file.append(token(t));
                file.append(";\n");
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(3);
            }
        }
    }
    
    private void executarLinha(ArrayList<Token> linha) {
        //casos especiais:
        //  ate (to):
        //      conferir se parada maior ou menor
        //      imprimir "var <=|>= num; var++|--"
    }
    private void executarFuncao(ArrayList<Token> linhaChamada, int start) 
            throws ErroSemantico, IOException, ErroOtimizacao {

        //Inicializa o simbolo que representara a funcao (assume que nao esta na tabela de simbolos)
        String nomeFun = linhaChamada.get(start).getValor();

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

        boolean naFuncao = false, encontrouFuncao = false;

        for (Map.Entry<Integer, ArrayList<Token>> entrySet : linhas.entrySet()) {
            nLinha = entrySet.getKey();
            ArrayList<Token> linha = entrySet.getValue();

            //Se chegou na funcao
            if (linha.contains(new Token("fun", nomeFun))
                    && linha.contains(new Token("def", ""))) {

                naFuncao = true;
                encontrouFuncao = true;
                escopos.adicionaEscopo();
                
                //Escreve declaracao da funcao
                for (Token t : linha) {
                    if("id".equals(t.getTipo()))
                    file.append(token(t));
                }
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

            //Analisa normalmente a funcao
            if (linha.contains(new Token("id", nomeFun))) {
                file.append("return ");
                for (int i = 2; i < linha.size(); i++) {
                    if("id".equals(linha.get(i).getTipo()))
                        file.append("a"+buscaVariavel(linha.get(i).getValor()));
                    else
                        file.append(token(linha.get(i)));
                }
            }
            executarLinha(linha);

            //Se encontrou valor de retorno
            if ("=".equals(linha.get(1).getTipo())
                    && funcao.nome.equals(linha.get(0).getValor())) {
                funcao.tipo = tipoExpressao(linha, 2, linha.size() - 1);
                break;
            }
        }

        //Restaura backup de escopo atualizando os ids
        backup.idEscopo = escopos.idEscopo;
        escopos = backup;
        nLinha = backupLinha;

        //Caso a funcao nao tenha sido declarada
        if (!encontrouFuncao) {
            throw new ErroSemantico("Funcao " + funcao.nome + " nao declarada.");
        }

        escopos.adicionaFuncao(funcao.nome);
        tabelaSimbolos.addSimbolo(funcao);

    }
    //Analisa uma linha de codigo, chamando as funcoes especificas

    
    
    /* FUNCAO PRINCIPAL */
    public void executar() {
        boolean isFuncao = false;

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

            //Se encontrou chamada a uma funcao a analisa
            int indexFun = indexOfTipo(linha, "fun", 0, linha.size() - 1);
            while (indexFun != -1) {
                try {
                    escopos.getVariavel(linha.get(indexFun).getValor());
                } catch (ErroSemantico e) { //Se nao foi declarado
                    try {
                        executarFuncao(linha, indexFun);
                    } catch (ErroSemantico ex) {}
                }
                //Caso tenha mais de uma chamada de funcao na mesma linha
                indexFun = indexOfTipo(linha, "fun", indexFun + 1, linha.size() - 1);
            }
        }
    }
}
