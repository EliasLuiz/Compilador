package AnalisadorSintatico;

import AnalisadorLexico.Token;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

public class AnalisadorSintatico {

    /* VARIAVEIS */
    private LinkedHashMap<Integer, ArvoreBinaria<Token>> arvores;
    private LinkedHashMap<Integer, ArrayList<Token>> linhas;



    /* CONSTRUTORES */
    public AnalisadorSintatico(String path) 
            throws IOException, ClassNotFoundException {
        arvores = new LinkedHashMap<>();
        carregar(path);
    }
    public AnalisadorSintatico(LinkedHashMap<Integer, ArrayList<Token>> tokens){
        arvores = new LinkedHashMap<>();
        linhas = tokens;
    }

    
    
    /* IO */
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
    public LinkedHashMap<Integer, ArvoreBinaria<Token>> getArvores(){
        return arvores;
    }
    public LinkedHashMap<Integer, ArrayList<Token>> getTokens() {
        return linhas;
    }
    
    
    
    /* FUNCOES AUXILIARES */
    private int indexOf(ArrayList array, Object x){
        for(int i = 0; i < array.size(); i++)
            if(array.get(i) == x)
                return i;
        return -1;
    }
    private int indexOf(ArrayList array, Object x, int start, int end){
        for(int i = start; i < end; i++)
            if(array.get(i) == x)
                return i;
        return -1;
    }
    
    
    
    /* ====================IDENTIFICADORES DE GRAMATICA==================== */
    
    /* ANALISE MACRO */
    private boolean programa() throws ErroSintatico{
        Integer[] keys = (Integer[]) linhas.keySet().toArray();
        for (int i = keys.length-1; i >= 0; i++) {
            if(linhas.get(keys[i]).contains(new Token("end", ""))){
                if (linhas.get(keys[i]).size() > 1)
                    System.err.println("Linha " + keys[i] + ": \"fim\" deve estar"
                            + "em uma linha a parte");
                if(i < keys.length-1)
                    System.err.println("Linha " + keys[i+1] + ": Instrucoes apos o "
                            + "termino do programa");
                //Se nao ocorreu nenhum erro
                else
                    return false;
                //Se ocorreu erro
                return true;
            }
        }
        System.err.println("Linha 1: Fim do programa nao encontrado");
        //Se ocorreu erro
        return true;
    }
    private boolean estruturaBlocos(){
        return estruturaIf() || estruturaWhile() || estruturaFor() || estruturaDef();
    }
    private boolean estruturaIf(){
        Stack<Integer> pilha = new Stack<>();
        Integer[] keys = (Integer[]) linhas.keySet().toArray();
        boolean erro = false;
        
        //Itera pelas linhas de codigo
        for (Integer nLinha : keys) {
            ArrayList<Token> linha = linhas.get(nLinha);
            int indexIf = indexOf(linha, new Token("if", ""));
            int indexElse = indexOf(linha, new Token("else", ""));
            int indexEndif = indexOf(linha, new Token("endif", ""));
            
            //Se for um endif
            if(indexEndif != -1){
                pilha.pop();
                if(linha.size() != 1){
                    System.err.println("Linha " + nLinha + ": Palavra-chave \"fim-se\""
                            + "deve estar sozinha na linha");
                    erro = true;
                }
            }
            
            //Se for um if
            else if(indexIf != -1){
                pilha.push(nLinha);
                if (indexIf > 0) {
                    System.err.println("Linha " + nLinha + ": Token antes da " 
                            + "palavra-chave \"se\"");
                    erro = true;
                }
                int indexThen = indexOf(linha, new Token("then", ""));
                if (indexThen == -1) {
                    System.err.println("Linha " + nLinha + ": Ausencia de " 
                            + "\"entao\" apos palavra-chave \"se\"");
                    erro = true;
                }
                if (indexThen + 1 < linha.size()) {
                    System.err.println("Linha " + nLinha + ": Token apos a " 
                            + "palavra-chave \"entao\"");
                    erro = true;
                }
                try { condicao(linha, indexIf+1, indexThen-1); }
                catch (ErroSintatico e) { 
                    System.err.println("Linha " + nLinha + ": " + e.erro);
                    erro = true; 
                }
            }
            
            //Se for um else
            else if(indexElse != -1){
                //Pilha passa a armazenar linha do else
                pilha.pop();
                pilha.push(nLinha);
                if (linha.size() != 1) {
                    System.err.println("Linha " + nLinha + ": Palavra-chave \"senao\""
                            + "deve estar sozinha na linha");
                    erro = true;
                }
            }
        }
        return erro;
    }
    
    
    /* ANALISE MEDIA */
    
    
    
    /* ANALISE MICRO */
    private ArvoreBinaria<Token> atribuicao(int linhaBegin, int linhaEnd)
            throws ErroSintatico{
        
    }
    private ArvoreBinaria<Token> condicao(ArrayList<Token> linha, int start, int end)
            throws ErroSintatico{
        
    }
    private ArvoreBinaria<Token> termo(ArrayList<Token> linha, int start, int end)
            throws ErroSintatico{
        if("(".equals(linha.get(start).getTipo()) &&
           ")".equals(linha.get(end).getTipo()))
            return condicao(linha, start+1, end-1);
        
        else if(start == end &&
                ("id".equals(linha.get(start).getTipo()) ||
                "float".equals(linha.get(start).getTipo()) ||
                "int".equals(linha.get(start).getTipo()) ||
                "true".equals(linha.get(start).getTipo()) ||
                "false".equals(linha.get(start).getTipo())))
            return new ArvoreBinaria<>(linha.get(start));
        
        else if("fun".equals(linha.get(start).getTipo()) &&
                "(".equals(linha.get(start+1).getTipo()) &&
                ")".equals(linha.get(end).getTipo()))
            return funcao(linha, start, end);
        
        else
            throw new ErroSintatico("Termo inválido");
    }
    private ArvoreBinaria<Token> funcao(ArrayList<Token> linha, int start, int end) 
            throws ErroSintatico{
        if(!"fun".equals(linha.get(start).getTipo()) ||
                !"(".equals(linha.get(start+1).getTipo()) ||
                !")".equals(linha.get(end).getTipo()))
            throw new ErroSintatico("Chamada a funcao mal formada");
        
        boolean correto = true;
        int i = start;
        int virgula;
        //Analise dos parametros
        while(i != end){
            virgula = indexOf(null, new Token(",", ""), i, end);
            if(virgula != -1){
                condicao(linha, i, virgula-1);
                i = virgula + 1;
            }
            else{
                condicao(linha, i, end);
                i = end;
            }
        }
    }
    
    /* ==================================================================== */
    
    
    
    /* FUNCAO PRINCIPAL */
    public boolean analisar(boolean print){
        
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
