package AnalisadorSemantico;

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

public class AnalisadorSemantico {
    
    /* VARIAVEIS */
    private LinkedHashMap<Integer, ArrayList<Token>> linhas;
    private ArrayList<ErroSemantico> erros;
    private Escopo escopos;
    private TabelaSimbolos tabelaSimbolos;
    
    /* VARIAVEIS AUXILIARES */
    private int nLinha;
    
    
    
    /* CONSTRUTORES */
    public AnalisadorSemantico(String path)
            throws IOException, ClassNotFoundException {
        carregar(path);
        erros = new ArrayList<>();
    }
    public AnalisadorSemantico(LinkedHashMap<Integer, ArrayList<Token>> tokens) {
        linhas = tokens;
        erros = new ArrayList<>();
        escopos = new Escopo();
        tabelaSimbolos = new TabelaSimbolos();
    }

    
    
    /* IO */
    private void carregar(String path) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(path);
        ObjectInputStream ois = new ObjectInputStream(fis);
        linhas = (LinkedHashMap<Integer, ArrayList<Token>>) ois.readObject();
        fis.close();
        ois.close();
    }
    public void salvar(String path) throws IOException {
        FileOutputStream fos = new FileOutputStream(path);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(tabelaSimbolos);
        oos.close();
        fos.close();
    }
    public TabelaSimbolos getTabelaSimbolos() {
        return tabelaSimbolos;
    }
    
    
    
    /* FUNCOES AUXILIARES */
    //Procura o elemento mais a direita fora de parenteses
    //  usada devido ao fato de a gramatica derivar a esquerda
    private int rIndexOfParen(ArrayList array, Object x, int start, int end) throws ErroSemantico{
        Stack<Integer> pilha = new Stack<>();
        for (int i = end; i >= start; i--) {
            if (array.get(i).equals(new Token(")", "")))
                pilha.push(1);
            else if (array.get(i).equals(new Token("(", ""))){
                pilha.pop();
            }
            if (array.get(i).equals(x)
                && pilha.empty()) {
                return i;
            }
        }
        return -1;
    }
    //Verifica se contem determinado token
    private boolean contains(ArrayList<Token> linha, Token t, int start, int end){
        for (int i = start; i < end; i++) {
            if(t.equals(linha.get(i)))
                return true;
        }
        return false;
    }
    //Verifica se tem determinado tipo de variavel
    private boolean contains(ArrayList<Token> linha, String tipo, int start, int end) throws ErroSemantico{
        for (int i = start; i < end; i++){
            if(tipo.equals(linha.get(i).getTipo()))
                return true;
            else if(   "id".equals(linha.get(i).getTipo())
                    || "fun".equals(linha.get(i).getTipo())){
                Simbolo s = tabelaSimbolos.getSimbolo(linha.get(i).getValor(), escopos.getEscopo(), nLinha);
                if (s.tipo.equals(tipo))
                    return true;
            }
        }
        return false;
    }
    
    
    
    /* FUNCOES DE ANALISE SEMANTICA */
    //Retorna o tipo de um expressao/condicao
    private String tipoExpressao(ArrayList<Token> linha, int start, int end) throws ErroSemantico {
        
        //Checa se e expressao entre parenteses
        //Encontra o operador mais a direita fora de parenteses
        Token[] operador = new Token[14];
        operador[0] = new Token("==", "");
        operador[1] = new Token("!=", "");
        operador[2] = new Token(">=", "");
        operador[3] = new Token("<=", "");
        operador[4] = new Token(">", "");
        operador[5] = new Token("<", "");
        operador[6] = new Token("and", "");
        operador[7] = new Token("or", "");
        operador[8] = new Token("not", "");
        operador[9] = new Token("+", "");
        operador[10] = new Token("-", "");
        operador[11] = new Token("*", "");
        operador[12] = new Token("/", "");
        operador[13] = new Token("=", "");
        int maior = -1;
        for(int j = 0; j < 14; j++){
            if(rIndexOfParen(linha, operador[j], start, end) > maior){
                maior = rIndexOfParen(linha, operador[j], start, end);
                break;
            }
        }
        //Se nao tiver nenhum operador fora de parenteses e tiver parenteses
        if(   maior == -1
           && linha.get(start) == new Token("(", "") 
           && linha.get(end) == new Token(")", ""))
            return tipoExpressao(linha, start+1, end-1);
        
        
        
        //Se e boleano
        else if(   contains(linha, new Token("==", ""), start, end)
                || contains(linha, new Token("!=", ""), start, end)
                || contains(linha, new Token(">=", ""), start, end)
                || contains(linha, new Token("<=", ""), start, end)
                || contains(linha, new Token(">", ""), start, end)
                || contains(linha, new Token(">", ""), start, end)
                || contains(linha, new Token("and", ""), start, end)
                || contains(linha, new Token("or", ""), start, end)
                || contains(linha, new Token("not", ""), start, end)
                || contains(linha, new Token("true", ""), start, end)
                || contains(linha, new Token("false", ""), start, end)
                || contains(linha, "bool", start, end))
            return "bool";
        //Se e string
        else if(contains(linha, "string", start, end))
            return "string";
        //Se e float
        else if(contains(linha, "float", start, end) ||
                contains(linha, new Token("/", ""), start, end))
            return "float";
        //Se nao existe expressao
        else if(start > end)
            return "null";
        //Se e int
        else
            return "int";
        
    }
    //Avalia consistencia de tipo de uma expressao/condicao
    private void consistenciaTipo(ArrayList<Token> linha, int start, int end) throws ErroSemantico {
        
        //Se e uma expressao vazia
        if(start > end)
            return;
        
        //Se e expressao entre parenteses
        if(linha.get(start) == new Token("(", "") &&
           linha.get(end) == new Token(")", ""))
            consistenciaTipo(linha, start+1, end-1);
        
        
        Token[] operador = new Token[14];
        operador[0] = new Token("==", "");
        operador[1] = new Token("!=", "");
        operador[2] = new Token(">=", "");
        operador[3] = new Token("<=", "");
        operador[4] = new Token(">", "");
        operador[5] = new Token("<", "");
        operador[6] = new Token("and", "");
        operador[7] = new Token("or", "");
        operador[8] = new Token("not", "");
        operador[9] = new Token("+", "");
        operador[10] = new Token("-", "");
        operador[11] = new Token("*", "");
        operador[12] = new Token("/", "");
        operador[13] = new Token("=", "");
        
        //Encontra o operador mais a direita
        int maior = -1;
        int op = -1;
        for(int j = 0; j < 9; j++){
            if(rIndexOfParen(linha, operador[j], start, end) > maior){
                maior = rIndexOfParen(linha, operador[j], start, end);
                op = j;
            }
        }
        
        //Checa consistencia de tipo
        if(maior != -1){
            String tipoEsqPuro = tipoExpressao(linha, start, maior-1);
            String tipoDirPuro = tipoExpressao(linha, maior+1, end);
            String tipoEsq = ( "int".equals(tipoEsqPuro) || "float".equals(tipoEsqPuro) ) ? "num" : tipoEsqPuro;
            String tipoDir = ( "int".equals(tipoDirPuro) || "float".equals(tipoDirPuro) ) ? "num" : tipoDirPuro;
            
            /*//Se e comparador
            if(op < 6 && tipoDir.equals(tipoEsq) && !"null".equals(tipoDir)){
                consistenciaTipo(linha, start, maior-1);
                consistenciaTipo(linha, maior+1, end);
            }
            
            //Se e operador logico binario
            else if(op < 8 && tipoDir.equals(tipoEsq) && "bool".equals(tipoDir)){
                consistenciaTipo(linha, start, maior-1);
                consistenciaTipo(linha, maior+1, end);
            }
            
            //Se e operador logico unario
            else if(op == 8 && "null".equals(tipoEsq) && "bool".equals(tipoDir)){
                consistenciaTipo(linha, maior+1, end);
            }
            
            //Se e operador +
            else if(   op == 9 
                    && (   (   tipoDir.equals(tipoEsq) 
                            && (   "string".equals(tipoDir) 
                                || "num".equals(tipoDir)))
                        || (   "string".equals(tipoEsq)
                            && "num".equals(tipoDir)))){
                consistenciaTipo(linha, start, maior-1);
                consistenciaTipo(linha, maior+1, end);
            }
            
            //Se e operador -
            else if(   op == 10 
                    && (   (   tipoDir.equals(tipoEsq) 
                            && "num".equals(tipoDir))
                        || 
                           (   "null".equals(tipoEsq)
                            && "num".equals(tipoDir)))){
                consistenciaTipo(linha, start, maior-1);
                consistenciaTipo(linha, maior+1, end);
            }
            
            //Se e operador aritimetico binario
            else if(op < 13 && tipoDir.equals(tipoEsq) && "num".equals(tipoDir)){
                consistenciaTipo(linha, start, maior-1);
                consistenciaTipo(linha, maior+1, end);
            }
            
            //Se e operador =
            else if(op == 13 && tipoDir.equals(tipoEsq) && !"null".equals(tipoDir)){
                consistenciaTipo(linha, start, maior-1);
                consistenciaTipo(linha, maior+1, end);
            }*/
            
            if(   (   op < 6 && tipoDir.equals(tipoEsq) 
                   && !"null".equals(tipoDir) 
                   && !"bool".equals(tipoDir))              //Se e comparador
               || (   op < 8 && tipoDir.equals(tipoEsq) 
                   && "bool".equals(tipoDir))               //Se e operador logico binario
               || (   op == 8 
                   && "null".equals(tipoEsq) 
                   && "bool".equals(tipoDir))               //Se e operador logico unario
               || (    op == 9 
                    && (   (   tipoDir.equals(tipoEsq) 
                            && (   "string".equals(tipoDir) 
                                || "num".equals(tipoDir)))
                        || (   "string".equals(tipoEsq)
                            && "num".equals(tipoDir))))     //Se e operador +
               || (    op == 10 
                    && (   (   tipoDir.equals(tipoEsq) 
                            && "num".equals(tipoDir))
                        || 
                           (   "null".equals(tipoEsq)
                            && "num".equals(tipoDir))))     //Se e operador -
               || (   op < 13 
                   && tipoDir.equals(tipoEsq) 
                   && "num".equals(tipoDir))                //Se e operador aritimetico binario
               || (   op == 13 
                   && tipoDirPuro.equals(tipoEsqPuro) 
                   && !"null".equals(tipoDir))              //Se e operador =
              ){
                consistenciaTipo(linha, start, maior-1);
                consistenciaTipo(linha, maior+1, end);
            }
            else {
                throw new ErroSemantico("Tipos invalidos para operador \"" +
                        operador[op].getTipo() + "\".");
            }
        }
    }
    //Teste condicao do se e do enquanto
    private void testeCondicao(ArrayList<Token> linha, String bloco) throws ErroSemantico {
        if("bool".equals(tipoExpressao(linha, 1, linha.size()-2)))
            consistenciaTipo(linha, 1, linha.size()-2);
        else
            throw new ErroSemantico("Condicao do \"" + bloco + "\" deve ser booleana");
            
    }
    //Teste atribuicao
    private void testeAtribuicao(ArrayList<Token> linha) throws ErroSemantico {
        
        String var = linha.get(0).getValor();
        String nomeVar = "";
        boolean isVetor = false;
        
        //identificacao de vetor vetor
        if(var.contains("[")){
            nomeVar = var.substring(0, var.indexOf('[')).trim();
            isVetor = true;
        }
        else
            nomeVar = var;
        
        //Declaracao da variavel + definicao do tipo
        Simbolo s = new Simbolo(nomeVar, tipoExpressao(linha, 2, linha.size()-1), nLinha, escopos.getEscopo(), false, isVetor);
        tabelaSimbolos.addSimbolo(s);
        
        //Checagem de consistencia
        consistenciaTipo(linha, 2, linha.size()-1);
    }
    
    
    
    /* FUNCAO PRINCIPAL */
    public void analisar(){
        
        //Declarar nomes de funcao e parametros
        //Analisar dentro das funcoes
        //Descartar escopo e tabela
        //Declarar nomes de funcao
        //Analisar o programa
        
        
        //Analise das funcoes
        for (Map.Entry<Integer, ArrayList<Token>> entrySet : linhas.entrySet()) {
            nLinha = entrySet.getKey();
            ArrayList<Token> linha = entrySet.getValue();
            
            //Testar condicao do se == bool
                //Checar consistencia
            
            //Testar condicao do enquanto == bool
                //Checar consistencia
            
            //Testar atribuicao
                //Se variavel nao existe declara
                //Checar consistencia
        }
        
        //Declaracao das funcoes
        
        //Analise do programa principal
        for (Map.Entry<Integer, ArrayList<Token>> entrySet : linhas.entrySet()) {
            nLinha = entrySet.getKey();
            ArrayList<Token> linha = entrySet.getValue();
            
            //Testar condicao do se == bool
                //Checar consistencia
            
            //Testar condicao do enquanto == bool
                //Checar consistencia
            
            //Testar atribuicao
                //Se variavel nao existe declara
                //Checar consistencia
        }
    }
}
