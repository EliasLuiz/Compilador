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
    private boolean programa(){
        Integer[] keys = (Integer[]) linhas.keySet().toArray();
        boolean erro = false;
        for (int i = keys.length-1; i >= 0; i++) {
            if(linhas.get(keys[i]).contains(new Token("end", ""))){
                if (linhas.get(keys[i]).size() > 2){
                    System.err.println("Linha " + keys[i] + ": \"fim\" deve estar"
                            + "em uma linha a parte.");
                    erro = true;
                }
                if(i < keys.length-1){
                    System.err.println("Linha " + keys[i+1] + ": Instrucoes apos o "
                            + "termino do programa.");
                    erro = true;
                }
                return erro;
            }
        }
        System.err.println("Linha 1: Fim do programa nao encontrado.");
        return true;
    }
    private boolean estruturaBlocos(){
        return estruturaIf() | estruturaWhile() | estruturaFor() | estruturaDef();
    }
    private boolean atribuicoes(){
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
                if(pilha.empty()){
                    System.err.println("Linha " + nLinha + ": Todos os blocos "
                            + "condicionais ja foram encerrados.");
                    erro = true;
                }
                pilha.pop();
                if(linha.size() != 2){
                    System.err.println("Linha " + nLinha + ": Palavra-chave \"fim-se\""
                            + "deve estar sozinha na linha.");
                    erro = true;
                }
            }
            
            //Se for um if
            else if(indexIf != -1){
                pilha.push(nLinha);
                if (indexIf > 0) {
                    System.err.println("Linha " + nLinha + ": Token antes da " 
                            + "palavra-chave \"se\".");
                    erro = true;
                }
                int indexThen = indexOf(linha, new Token("then", ""));
                if (indexThen == -1) {
                    System.err.println("Linha " + nLinha + ": Ausencia de " 
                            + "\"entao\" apos palavra-chave \"se\".");
                    erro = true;
                }
                if (indexThen + 1 < linha.size()-1) {
                    System.err.println("Linha " + nLinha + ": Token apos a " 
                            + "palavra-chave \"entao\".");
                    erro = true;
                }
                try { condicao(linha, indexIf+1, indexThen-1); }
                catch (ErroSintatico e) { 
                    System.err.println("Linha " + nLinha + ": " + e.erro + ".");
                    erro = true; 
                }
            }
            
            //Se for um else
            else if(indexElse != -1){
                //Pilha passa a armazenar linha do else
                pilha.pop();
                pilha.push(nLinha);
                if (linha.size() != 2) {
                    System.err.println("Linha " + nLinha + ": Palavra-chave \"senao\""
                            + "deve estar sozinha na linha.");
                    erro = true;
                }
            }
        }
        //Se houver if nao encerrado
        while(!pilha.empty()){
            Integer nLinha = pilha.pop();
            System.err.println("Linha " + nLinha + ": Bloco condicional"
                    + "nao encerrado.");
            erro = true;
        }
        return erro;
    }
    private boolean estruturaWhile(){
        Stack<Integer> pilha = new Stack<>();
        Integer[] keys = (Integer[]) linhas.keySet().toArray();
        boolean erro = false;
        
        //Itera pelas linhas de codigo
        for (Integer nLinha : keys) {
            ArrayList<Token> linha = linhas.get(nLinha);
            int indexWhile = indexOf(linha, new Token("while", ""));
            int indexEndWhile = indexOf(linha, new Token("endwhile", ""));
            
            //Se for um endwhile
            if(indexEndWhile != -1){
                if(pilha.empty()){
                    System.err.println("Linha " + nLinha + ": Todos os blocos "
                            + "enquanto ja foram encerrados.");
                    erro = true;
                }
                pilha.pop();
                if(linha.size() != 2){
                    System.err.println("Linha " + nLinha + ": Palavra-chave \"fim-enquanto\""
                            + "deve estar sozinha na linha.");
                    erro = true;
                }
            }
            
            //Se for um while
            else if(indexWhile != -1){
                pilha.push(nLinha);
                if (indexWhile > 0) {
                    System.err.println("Linha " + nLinha + ": Token antes da " 
                            + "palavra-chave \"enquanto\".");
                    erro = true;
                }
                int indexDo = indexOf(linha, new Token("do", ""));
                if (indexDo == -1) {
                    System.err.println("Linha " + nLinha + ": Ausencia de " 
                            + "\"entao\" apos palavra-chave \"faca\".");
                    erro = true;
                }
                if (indexDo + 1 < linha.size()-1) {
                    System.err.println("Linha " + nLinha + ": Token apos a " 
                            + "palavra-chave \"faca\".");
                    erro = true;
                }
                try { condicao(linha, indexWhile+1, indexDo-1); }
                catch (ErroSintatico e) { 
                    System.err.println("Linha " + nLinha + ": " + e.erro + ".");
                    erro = true; 
                }
            }
        }
        //Se houver while nao encerrado
        while(!pilha.empty()){
            Integer nLinha = pilha.pop();
            System.err.println("Linha " + nLinha + ": Estrutura de repeticao"
                    + "nao encerrada.");
            erro = true;
        }
        return erro;
    }
    private boolean estruturaFor(){
        Stack<Integer> pilha = new Stack<>();
        Integer[] keys = (Integer[]) linhas.keySet().toArray();
        boolean erro = false;
        
        //Itera pelas linhas de codigo
        for (Integer nLinha : keys) {
            ArrayList<Token> linha = linhas.get(nLinha);
            int indexFor = indexOf(linha, new Token("for", ""));
            int indexEndFor = indexOf(linha, new Token("endfor", ""));
            
            //Se for um endfor
            if(indexEndFor != -1){
                if(pilha.empty()){
                    System.err.println("Linha " + nLinha + ": Todos os blocos "
                            + "para ja foram encerrados.");
                    erro = true;
                }
                pilha.pop();
                if(linha.size() != 2){
                    System.err.println("Linha " + nLinha + ": Palavra-chave \"fim-para\""
                            + "deve estar sozinha na linha.");
                    erro = true;
                }
            }
            
            //Se for um for
            else if(indexFor != -1){
                pilha.push(nLinha);
                int indexFrom = indexOf(linha, new Token("from", ""));
                int indexTo = indexOf(linha, new Token("to", ""));
                int indexDo = indexOf(linha, new Token("do", ""));
                if (indexFor > 0) {
                    System.err.println("Linha " + nLinha + ": Token antes da " 
                            + "palavra-chave \"para\".");
                    erro = true;
                }
                if (indexFrom == -1) {
                    System.err.println("Linha " + nLinha + ": Ausencia de " 
                            + "\"de\" apos palavra-chave \"para\".");
                    erro = true;
                }
                if (indexTo == -1) {
                    System.err.println("Linha " + nLinha + ": Ausencia de " 
                            + "\"ate\" apos palavra-chave \"de\".");
                    erro = true;
                }
                if (indexDo == -1) {
                    System.err.println("Linha " + nLinha + ": Ausencia de " 
                            + "\"faca\" apos palavra-chave \"ate\".");
                    erro = true;
                }
                if (indexDo + 1 < linha.size()-1) {
                    System.err.println("Linha " + nLinha + ": Token apos a " 
                            + "palavra-chave \"faca\".");
                    erro = true;
                }
                if(!"id".equals(linha.get(indexFor+1).getTipo())){
                    System.err.println("Linha " + nLinha + ": Identificador a ser "
                            + "iterado deve ser uma variavel.");
                    erro = true;
                }
                if(!"int".equals(linha.get(indexFrom+1).getTipo()) &&
                   !"float".equals(linha.get(indexFrom+1).getTipo())){
                    System.err.println("Linha " + nLinha + ": Valor inicial deve ser "
                            + "numerico.");
                    erro = true;
                }
                if(!"int".equals(linha.get(indexTo+1).getTipo()) &&
                   !"float".equals(linha.get(indexTo+1).getTipo())){
                    System.err.println("Linha " + nLinha + ": Valor final deve ser "
                            + "numerico.");
                    erro = true;
                }
                if(indexFrom != -1 &&
                   indexFrom - indexFor > 2){
                    System.err.println("Linha " + nLinha + ": Quantidade excessiva "
                            + "de identificadores a serem iterados.");
                    erro = true;
                }
                if(indexTo != -1 &&
                   indexTo - indexFrom > 2){
                    System.err.println("Linha " + nLinha + ": Quantidade excessiva "
                            + "de valores iniciais.");
                    erro = true;
                }
                if(indexDo != -1 &&
                   indexDo - indexTo > 2){
                    System.err.println("Linha " + nLinha + ": Quantidade excessiva "
                            + "de valores finais.");
                    erro = true;
                }
            }
        }
        //Se houver for nao encerrado
        while(!pilha.empty()){
            Integer nLinha = pilha.pop();
            System.err.println("Linha " + nLinha + ": Estrutura de repeticao"
                    + "nao encerrada.");
            erro = true;
        }
        return erro;
    }
    private boolean estruturaDef(){
        Stack<Integer> pilha = new Stack<>();
        Integer[] keys = (Integer[]) linhas.keySet().toArray();
        boolean erro = false;
        
        //Itera pelas linhas de codigo
        for (Integer nLinha : keys) {
            ArrayList<Token> linha = linhas.get(nLinha);
            int indexDef = indexOf(linha, new Token("def", ""));
            int indexEndDef = indexOf(linha, new Token("enddef", ""));
            
            //Se for um enddef
            if(indexEndDef != -1){
                if(pilha.empty()){
                    System.err.println("Linha " + nLinha + ": Todas as definicoes "
                            + "de funcoes ja foram encerrados.");
                    erro = true;
                }
                pilha.pop();
                if(linha.size() != 2){
                    System.err.println("Linha " + nLinha + ": Palavra-chave \"fim-funcao\""
                            + "deve estar sozinha na linha.");
                    erro = true;
                }
            }
            
            //Se for um def
            else if(indexDef != -1){
                pilha.push(nLinha);
                if (indexDef > 0) {
                    System.err.println("Linha " + nLinha + ": Token antes da " 
                            + "palavra-chave \"funcao\".");
                    erro = true;
                }
                try { funcao(linha, indexDef+1, linha.size()-1); }
                catch (ErroSintatico e) { 
                    System.err.println("Linha " + nLinha + ": " + e.erro + ".");
                    erro = true; 
                }
            }
        }
        //Se houver def nao encerrado
        while(!pilha.empty()){
            Integer nLinha = pilha.pop();
            System.err.println("Linha " + nLinha + ": Definicao de funcao"
                    + "nao encerrada.");
            erro = true;
        }
        return erro;
    }
    
    /* ANALISE MICRO */
    private ArvoreBinaria<Token> atribuicao(ArrayList<Token> linha, int linhaBegin, int linhaEnd)
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
            throw new ErroSintatico("Termo invalido");
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
        boolean erro = false;
        
        /* Analise de blocos do codigo */
        erro |= programa();
        erro |= estruturaBlocos();
        erro |= atribuicoes();
        
        if(print){
            System.out.println("\n\nAnalise Sintatica:");
            for(Map.Entry<Integer, ArvoreBinaria<Token>> entry: arvores.entrySet()){
                System.out.println(entry.getKey());
                entry.getValue().print();
                System.out.println("\n");
            }
            System.out.println("\n\n");
        }
        
        return erro;
    }
}
