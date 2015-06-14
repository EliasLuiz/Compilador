package AnalisadorSintatico;

import AnalisadorLexico.Token;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

public class AnalisadorSintatico {

    /* VARIAVEIS */
    private LinkedHashMap<Integer, ArvoreBinaria<Token>> arvores;
    private LinkedHashMap<Integer, ArrayList<Token>> linhas;
    private ArrayList<ErroSintatico> erros;

    
    
    /* CONSTRUTORES */
    public AnalisadorSintatico(String path)
            throws IOException, ClassNotFoundException {
        arvores = new LinkedHashMap<>();
        carregar(path);
        erros = new ArrayList<>();
    }
    public AnalisadorSintatico(LinkedHashMap<Integer, ArrayList<Token>> tokens) {
        arvores = new LinkedHashMap<>();
        linhas = tokens;
        erros = new ArrayList<>();
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
    public LinkedHashMap<Integer, ArvoreBinaria<Token>> getArvores() {
        return arvores;
    }
    public LinkedHashMap<Integer, ArrayList<Token>> getTokens() {
        return linhas;
    }

    
    
    /* FUNCOES AUXILIARES */
    private int indexOf(ArrayList array, Object x) {
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).equals(x)) {
                return i;
            }
        }
        return -1;
    }
    private int indexOf(ArrayList array, Object x, int start, int end) {
        for (int i = start; i < end; i++) {
            if (array.get(i).equals(x)) {
                return i;
            }
        }
        return -1;
    }
    private int rIndexOf(ArrayList array, Object x, int start, int end) {
        //Procura o elemento mais a direita
        //  usada devido ao fato de a gramatica derivar a esquerda
        for (int i = end-1; i >= start; i--) {
            if (array.get(i).equals(x)) {
                return i;
            }
        }
        return -1;
    }
    private void linha2instr(){
        Object[] keys = linhas.keySet().toArray();
        ArrayList<Token> aux = new ArrayList<>();
        int nLinha = 0;
        
        for (int i = 0; i < keys.length; i++) {
            ArrayList<Token> linha = linhas.get(keys[i]);
            
            //Se instrucao continua na outra linha
            if(!linha.get(linha.size()-1).equals(new Token(".endinstr", ""))){
                //armazena a o numero da linha
                if(aux.isEmpty()){
                    nLinha = i;
                }
                //copia o conteudo
                for(Token t: linha){
                    aux.add(t);
                }
            }
            //Se chegou ao fim da instrucao
            else if(!aux.isEmpty()){
                for(Token t: linha){
                    aux.add(t);
                }
                //Substitui por uma linha
                linhas.replace((Integer)keys[nLinha], aux);
                //Retira as outras linhas
                for(int j = nLinha + 1; j <= i; j++)
                    linhas.remove(keys[j]);
                
                aux = new ArrayList<>();
            }
        }
        
        //Remove os endinstr de todas as instrucoes
        keys = linhas.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            aux = linhas.get(keys[i]);
            aux.remove(aux.size()-1);
            linhas.replace((Integer)keys[i], aux);
        }
    }

    
    
    /* ====================IDENTIFICADORES DE GRAMATICA==================== */
    
    /* ANALISE MACRO */
    private boolean programa() {
        Object[] keys = linhas.keySet().toArray();
        
        boolean erro = false;
        for (int i = 0; i < keys.length; i++) {
            if (linhas.get(keys[i]).contains(new Token("end", ""))) {
                if (linhas.get(keys[i]).size() > 2) {
                    erros.add(new ErroSintatico((Integer)keys[i], "\"fim\" deve estar"
                            + "em uma linha a parte."));
                    erro = true;
                }
                if (i < keys.length - 1) {
                    erros.add(new ErroSintatico((Integer)keys[i], "Instrucoes apos o "
                            + "termino do programa."));
                    erro = true;
                }
                return erro;
            }
        }
        erros.add(new ErroSintatico(1, "Fim do programa nao encontrado."));
        return true;
    }
    private boolean estruturaBlocos() {
        Stack<ErroSintatico> pilha = new Stack<>();
        //Pilha de erro sintatico para armazenar a linha de inicio
        //    e o tipo do bloco (int, String) (Sim, eh uma gambiarra para
        //    nao criar outra classe so para isso)
        boolean erro = false;

        //Itera pelas linhas de codigo
        for (Map.Entry<Integer, ArrayList<Token>> entrySet : linhas.entrySet()) {
            Integer nLinha = entrySet.getKey();
            ArrayList<Token> linha = entrySet.getValue();
            
            
            /* =========== BLOCO CONDICIONAL =========== */
            int indexIf = indexOf(linha, new Token("if", ""));
            //Se for um if
            if (indexIf != -1){
                erro |= estruturaIf(nLinha, linha);
                pilha.push(new ErroSintatico(nLinha, "se"));
            }
            int indexElse = indexOf(linha, new Token("else", ""));
            //Se for um else
            if (indexElse != -1) {
                //Pilha passa a armazenar linha do else
                if(!"se".equals(pilha.peek().linha)){
                    erros.add(new ErroSintatico(nLinha, "\"senao\" sem \"se\" "
                            + "correspondente."));
                    erro = true;
                }
                else{
                    pilha.pop();
                    pilha.push(new ErroSintatico(nLinha, "senao"));
                }
                if (linha.size() != 1) {
                    erros.add(new ErroSintatico(nLinha, "Palavra-chave \"senao\" "
                            + "deve estar sozinha na linha."));
                    erro = true;
                }
            }
            int indexEndif = indexOf(linha, new Token("endif", ""));
            //Se for um endif
            if (indexEndif != -1) {
                if (pilha.empty()) {
                    erros.add(new ErroSintatico(nLinha, "Todos os blocos "
                            + "condicionais ja foram encerrados."));
                    erro = true;
                }
                else if(!"se".equals(pilha.peek().erro)
                     && !"senao".equals(pilha.peek().erro)){
                    erros.add(new ErroSintatico(nLinha, "\"fim-se\" sem \"se\" "
                            + "ou \"senao\" correspondente."));
                    erro = true;
                }
                else{
                    pilha.pop();
                }
                if (linha.size() != 1) {
                    erros.add(new ErroSintatico(nLinha, "Palavra-chave \"fim-se\" "
                            + "deve estar sozinha na linha."));
                    erro = true;
                }
            }
            /* =========== FIM BLOCO CONDICIONAL =========== */
            
            /* =========== BLOCO ENQUANTO =========== */
            int indexWhile = indexOf(linha, new Token("while", ""));
            //Se for um while
            if (indexWhile != -1){
                erro |= estruturaWhile(nLinha, linha);
                pilha.push(new ErroSintatico(nLinha, "enquanto"));
            }
            int indexEndWhile = indexOf(linha, new Token("endwhile", ""));
            //Se for um endwhile
            if (indexEndWhile != -1) {
                if (pilha.empty()) {
                    erros.add(new ErroSintatico(nLinha, "Todos os blocos "
                            + "\"enquanto\" ja foram encerrados."));
                    erro = true;
                }
                else if(!"enquanto".equals(pilha.peek().erro)){
                    erros.add(new ErroSintatico(nLinha, "\"fim-enquanto\" sem "
                            + "\"enquanto\" correspondente."));
                    erro = true;
                }
                else{
                    pilha.pop();
                }
                if (linha.size() != 1) {
                    erros.add(new ErroSintatico(nLinha, "Palavra-chave \"fim-enquanto\" "
                            + "deve estar sozinha na linha."));
                    erro = true;
                }
            }
            /* =========== FIM BLOCO ENQUANTO =========== */
        
            /* =========== BLOCO PARA =========== */
            int indexFor = indexOf(linha, new Token("for", ""));
            //Se for um for
            if (indexFor != -1){
                erro |= estruturaFor(nLinha, linha);
                pilha.push(new ErroSintatico(nLinha, "para"));
            }
            int indexEndFor = indexOf(linha, new Token("endfor", ""));
            //Se for um endfor
            if (indexEndFor != -1) {
                if (pilha.empty()) {
                    erros.add(new ErroSintatico(nLinha, "Todos os blocos "
                            + "\"para\" ja foram encerrados."));
                    erro = true;
                }
                else if(!"para".equals(pilha.peek().erro)){
                    erros.add(new ErroSintatico(nLinha, "\"fim-para\" sem "
                            + "\"para\" correspondente."));
                    erro = true;
                }
                else{
                    pilha.pop();
                }
                if (linha.size() != 1) {
                    erros.add(new ErroSintatico(nLinha, "Palavra-chave \"fim-para\" "
                            + "deve estar sozinha na linha."));
                    erro = true;
                }
            }
            /* =========== FIM BLOCO PARA =========== */
        
            /* =========== BLOCO DEF =========== */
            int indexDef = indexOf(linha, new Token("def", ""));
            //Se for um def
            if (indexDef != -1){
                erro |= estruturaDef(nLinha, linha);
                pilha.push(new ErroSintatico(nLinha, "funcao"));
            }
            int indexEndDef = indexOf(linha, new Token("enddef", ""));
            //Se for um enddef
            if (indexEndDef != -1) {
                if (pilha.empty()) {
                    erros.add(new ErroSintatico(nLinha, "Todos as definicoes "
                            + "de funcoes ja foram encerrados."));
                    erro = true;
                }
                else if(!"funcao".equals(pilha.peek().erro)){
                    erros.add(new ErroSintatico(nLinha, "\"fim-funcao\" sem "
                            + "\"funcao\" correspondente."));
                    erro = true;
                }
                else{
                    pilha.pop();
                }
                if (linha.size() != 2) {
                    erros.add(new ErroSintatico(nLinha, "Palavra-chave \"fim-funcao\" "
                            + "deve estar sozinha na linha."));
                    erro = true;
                }
            }
            /* =========== FIM BLOCO DEF =========== */
            
        }
        
        //Se houver bloco nao encerrado
        while (!pilha.empty()) {
            ErroSintatico e = pilha.pop();
            
            erros.add(new ErroSintatico(e.linha, "Bloco \"" + e.erro
                    + "\" nao encerrado."));
            erro = true;
        }
        return erro;
    }
    private boolean atribuicoes() {
        
        boolean erro = false;
        
        for (Map.Entry<Integer, ArrayList<Token>> entrySet : linhas.entrySet()) {
            
            Integer nLinha = entrySet.getKey();
            ArrayList<Token> linha = entrySet.getValue();
            int indexAtrib = indexOf(linha, new Token("=", ""));
            
            if(indexAtrib != -1){
                
//                TRATAR ATRIBUICAO PARA VETOR
                
                if(indexAtrib != 1 ||
                   !"id".equals(linha.get(0).getTipo())){
                    erros.add(new ErroSintatico(nLinha, "Atribuicoes deve ser a uma"
                            + "variavel"));
                    erro = true;
                }
                else{
                    try {
                        ArvoreBinaria<Token> arvore = new ArvoreBinaria<>(new Token("=", ""));
                        arvore.setEsq(new ArvoreBinaria<>(linha.get(0)));
                        arvore.setDir(condicao(linha, 0, linha.size()-1));
                        arvores.put(nLinha, arvore);
                    } catch (ErroSintatico e) {
                        e.linha = nLinha;
                        erros.add(e);
                        erro = true;
                    }
                }
            }
        }
        return erro;
    }
    private boolean declVetor(){   
        
        boolean erro = false;
        
        for (Map.Entry<Integer, ArrayList<Token>> entrySet : linhas.entrySet()) {
            Integer nLinha = entrySet.getKey();
            ArrayList<Token> linha = entrySet.getValue();
            
            int indexVet = indexOf(linha, new Token("vet", ""));
            
            if(indexVet != -1){
                
                if(indexVet > 0){
                    erros.add(new ErroSintatico(nLinha, "Token antes da palavra chave "
                            + "\"vetor\"."));
                    erro = true;
                }
                if(indexVet == linha.size()-2){
                    erros.add(new ErroSintatico(nLinha, "Vetor a ser declarado nao "
                            + "especificado."));
                    erro = true;
                }
                //Caso vetor tenha sido especificado
                else {
                    if(!"id".equals(linha.get(indexVet + 1).getTipo())){
                        erros.add(new ErroSintatico(nLinha, "Vetor deve possuir um nome de "
                                + "variavel valido."));
                        erro = true;
                    }
                    if(indexVet + 6 > linha.size()){
                        erros.add(new ErroSintatico(nLinha, "Tamanho do vetor nao "
                                + "especificado."));
                        erro = true;
                    }
                    //Vetor 1 ou 2 dimensoes
                    if(indexVet + 6 >= linha.size()){
                        if(!"[".equals(linha.get(indexVet + 2).getTipo()) ||
                           !"]".equals(linha.get(indexVet + 4).getTipo())){
                            erros.add(new ErroSintatico(nLinha, "Tamanho do vetor nao "
                                    + "especificado corretamente."));
                            erro = true;
                        }
                        if(!"int".equals(linha.get(indexVet + 3).getTipo())){
                            erros.add(new ErroSintatico(nLinha, "Tamanho do vetor deve"
                                    + "ser inteiro."));
                            erro = true;
                        }
                    }
                    //Vetor 2 dimensoes
                    if(indexVet + 9 == linha.size()){
                       if(!"[".equals(linha.get(indexVet + 5).getTipo()) ||
                          !"]".equals(linha.get(indexVet + 7).getTipo())){
                            erros.add(new ErroSintatico(nLinha, "Tamanho do vetor nao "
                                    + "especificado."));
                            erro = true;
                        }
                        
                        if(!"int".equals(linha.get(indexVet + 3).getTipo())){
                            erros.add(new ErroSintatico(nLinha, "Tamanho do vetor deve"
                                    + "ser inteiro."));
                            erro = true;
                        }
                    }
                    else if (indexVet + 9 < linha.size()) {
                        erros.add(new ErroSintatico(nLinha, "Nao deve haver tokens "
                                + "apos a declaracao de um vetor."));
                        erro = true;
                    }
                }
            }
        }
        return erro;
    }
    
    
    /* ANALISE MEDIA */
    private boolean estruturaIf(int nLinha,  ArrayList<Token> linha) {
        boolean erro = false;

        int indexIf = indexOf(linha, new Token("if", ""));

        if (indexIf > 0) {
            erros.add(new ErroSintatico(nLinha, "Token antes da "
                    + "palavra-chave \"se\"."));
            erro = true;
        }
        int indexThen = indexOf(linha, new Token("then", ""));
        if (indexThen == -1) {
            erros.add(new ErroSintatico(nLinha, "Ausencia de "
                    + "\"entao\" apos palavra-chave \"se\"."));
            erro = true;
        }
        if (indexThen + 1 < linha.size()) {
            erros.add(new ErroSintatico(nLinha, "Token apos a "
                    + "palavra-chave \"entao\"."));
            erro = true;
        }
        try {
            condicao(linha, indexIf + 1, indexThen - 1);
        } catch (ErroSintatico e) {
            e.linha = nLinha;
            erros.add(e);
            erro = true;
        }
        
        return erro;
    }
    private boolean estruturaWhile(int nLinha, ArrayList<Token> linha) {
        boolean erro = false;
        
        int indexWhile = indexOf(linha, new Token("while", "")); //Se for um while

        if (indexWhile > 0) {
            erros.add(new ErroSintatico(nLinha, "Token antes da "
                    + "palavra-chave \"enquanto\"."));
            erro = true;
        }
        
        int indexDo = indexOf(linha, new Token("do", ""));
        if (indexDo == -1) {
            erros.add(new ErroSintatico(nLinha, "Ausencia de "
                    + "\"entao\" apos palavra-chave \"faca\"."));
            erro = true;
        }
        else{
            if (indexDo + 1 < linha.size()) {
                erros.add(new ErroSintatico(nLinha, "Token apos a "
                        + "palavra-chave \"faca\"."));
                erro = true;
            }
            try {
                condicao(linha, indexWhile + 1, indexDo - 1);
            } catch (ErroSintatico e) {
                e.linha = nLinha;
                erros.add(e);
                erro = true;
            }
        }
        return erro;
    }
    private boolean estruturaFor(int nLinha, ArrayList<Token> linha) {
        boolean erro = false;
            
        int indexFor = indexOf(linha, new Token("for", ""));
        int indexFrom = indexOf(linha, new Token("from", ""));
        int indexTo = indexOf(linha, new Token("to", ""));
        int indexDo = indexOf(linha, new Token("do", ""));
        
        if (indexFor > 0) {
            erros.add(new ErroSintatico(nLinha, "Token antes da "
                    + "palavra-chave \"para\"."));
            erro = true;
        }
        if (indexFrom == -1) {
            erros.add(new ErroSintatico(nLinha, "Ausencia de "
                    + "\"de\" apos palavra-chave \"para\"."));
            erro = true;
        }
        if (indexTo == -1) {
            erros.add(new ErroSintatico(nLinha, "Ausencia de "
                    + "\"ate\" apos palavra-chave \"de\"."));
            erro = true;
        }
        if (indexDo == -1) {
            erros.add(new ErroSintatico(nLinha, "Ausencia de "
                    + "\"faca\" apos palavra-chave \"ate\"."));
            erro = true;
        }
        if (indexDo + 1 < linha.size()) {
            erros.add(new ErroSintatico(nLinha, "Token apos a "
                    + "palavra-chave \"faca\"."));
            erro = true;
        }
        if(indexFor + 1 < linha.size()){
            if (!"id".equals(linha.get(indexFor + 1).getTipo())) {
                erros.add(new ErroSintatico(nLinha, "Identificador a ser "
                        + "iterado deve ser uma variavel."));
                erro = true;
            }
            if (!"int".equals(linha.get(indexFrom + 1).getTipo())
                    && !"float".equals(linha.get(indexFrom + 1).getTipo())) {
                erros.add(new ErroSintatico(nLinha, "Valor inicial deve ser "
                        + "numerico."));
                erro = true;
            }
            if (!"int".equals(linha.get(indexTo + 1).getTipo())
                    && !"float".equals(linha.get(indexTo + 1).getTipo())) {
                erros.add(new ErroSintatico(nLinha, "Valor final deve ser "
                        + "numerico."));
                erro = true;
            }
        }
        if (indexFrom != -1
                && indexFrom - indexFor > 2) {
            erros.add(new ErroSintatico(nLinha, "Quantidade excessiva "
                    + "de identificadores a serem iterados."));
            erro = true;
        }
        if (indexTo != -1
                && indexTo - indexFrom > 2) {
            erros.add(new ErroSintatico(nLinha, "Quantidade excessiva "
                    + "de valores iniciais."));
            erro = true;
        }
        if (indexDo != -1
                && indexDo - indexTo > 2) {
            erros.add(new ErroSintatico(nLinha, "Quantidade excessiva "
                    + "de valores finais."));
            erro = true;
        }
        
        return erro;
    }
    private boolean estruturaDef(int nLinha, ArrayList<Token> linha) {
        boolean erro = false;
        
        int indexDef = indexOf(linha, new Token("def", ""));

        if (indexDef > 0) {
            erros.add(new ErroSintatico(nLinha, "Token antes da "
                    + "palavra-chave \"funcao\"."));
            erro = true;
        }
        try {
            funcao(linha, indexDef + 1, linha.size() - 1);
        } catch (ErroSintatico e) {
            e.linha = nLinha;
            erros.add(e);
            erro = true;
        }
        
        return erro;
    }
    
    
    /* ANALISE MICRO */
    private ArvoreBinaria<Token> condicao(ArrayList<Token> linha, int start, int end)
            throws ErroSintatico {
        
        //Armazena os indices das aparicoes dos tokens na linha
        Integer[] indexComparativos = new Integer[8];
        indexComparativos[0] = rIndexOf(linha, new Token("and", ""), start, end);
        indexComparativos[1] = rIndexOf(linha, new Token("or", ""), start, end);
        indexComparativos[2] = rIndexOf(linha, new Token("==", ""), start, end);
        indexComparativos[3] = rIndexOf(linha, new Token("!=", ""), start, end);
        indexComparativos[4] = rIndexOf(linha, new Token("<", ""), start, end);
        indexComparativos[5] = rIndexOf(linha, new Token("<=", ""), start, end);
        indexComparativos[6] = rIndexOf(linha, new Token(">", ""), start, end);
        indexComparativos[7] = rIndexOf(linha, new Token(">=", ""), start, end);
        
        //Descobre qual comparativo esta mais a direita
        int maior = -1;
        for(int j = 0; j < 8; j++){
            if(indexComparativos[j] > maior){
                maior = indexComparativos[j];
            }
        }
        
        //Caso seja apenas uma expressao
        if(maior == -1)
            return expressao(linha, start, end);
        
        //Geracao da arvore sintatica
        ArvoreBinaria<Token> arvore = new ArvoreBinaria<>(linha.get(maior));
        arvore.setDir(condicao(linha, start, maior-1));
        arvore.setEsq(expressao(linha, maior+1, end));
        
        return arvore;
    }
    private ArvoreBinaria<Token> expressao(ArrayList<Token> linha, int start, int end) 
            throws ErroSintatico {
        
        //Armazena os indices das aparicoes dos tokens na linha
        Integer[] indexOperadores = new Integer[8];
        indexOperadores[0] = rIndexOf(linha, new Token("+", ""), start, end);
        indexOperadores[1] = rIndexOf(linha, new Token("-", ""), start, end);
        
        //Descobre qual comparativo esta mais a direita
        int maior = -1;
        for(int j = 0; j < 2; j++){
            if(indexOperadores[j] > maior){
                maior = indexOperadores[j];
            }
        }
        
        //Caso seja apenas uma expressao
        if(maior == -1)
            return expressaoPrec(linha, start, end);
        
        //Geracao da arvore sintatica
        ArvoreBinaria<Token> arvore = new ArvoreBinaria<>(linha.get(maior));
        arvore.setDir(expressao(linha, start, maior-1));
        arvore.setEsq(expressaoPrec(linha, maior+1, end));
        
        return arvore;
    }
    private ArvoreBinaria<Token> expressaoPrec(ArrayList<Token> linha, int start, int end) 
            throws ErroSintatico{
        
        //Armazena os indices das aparicoes dos tokens na linha
        Integer[] indexOperadores = new Integer[8];
        indexOperadores[0] = rIndexOf(linha, new Token("*", ""), start, end);
        indexOperadores[1] = rIndexOf(linha, new Token("/", ""), start, end);
        
        //Descobre qual comparativo esta mais a direita
        int maior = -1;
        int op = 0;
        for(int j = 0; j < 2; j++){
            if(indexOperadores[j] > maior){
                maior = indexOperadores[j];
                op = j;
            }
        }
        
        //Caso seja apenas um termo
        if(maior == -1)
            return termo(linha, start, end);
        
        //Geracao da arvore sintatica
        ArvoreBinaria<Token> arvore = new ArvoreBinaria<>(linha.get(op));
        arvore.setDir(termo(linha, start, op-1));
        arvore.setEsq(expressaoPrec(linha, op+1, end));
        
        return arvore;
    }
    private ArvoreBinaria<Token> termo(ArrayList<Token> linha, int start, int end)
            throws ErroSintatico {
        if ("(".equals(linha.get(start).getTipo())
                && ")".equals(linha.get(end).getTipo())) {
            return condicao(linha, start + 1, end - 1);
        //terminal
        } else if (start == end
                && ("id".equals(linha.get(start).getTipo())
                || "float".equals(linha.get(start).getTipo())
                || "int".equals(linha.get(start).getTipo())
                || "true".equals(linha.get(start).getTipo())
                || "false".equals(linha.get(start).getTipo()))) {
            return new ArvoreBinaria<>(linha.get(start));
        //funcao
        } else if ("fun".equals(linha.get(start).getTipo())
                && "(".equals(linha.get(start + 1).getTipo())
                && ")".equals(linha.get(end).getTipo())) {
            return funcao(linha, start, end);
        //vetor
        } else if ("id".equals(linha.get(start).getTipo())
                && "[".equals(linha.get(start+1).getTipo())
                && "]".equals(linha.get(end).getTipo())) {
            int indexFim1 = indexOf(linha, new Token("[", ""), start+2, end);
            int indexComeco2 = indexOf(linha, new Token("]", ""), start+1, end-1);
            
            ArvoreBinaria<Token> arvore = new ArvoreBinaria<>(linha.get(start));
            
            //2 dimensoes
            if (indexFim1 != -1 && indexComeco2 != -1) {
                condicao(linha, start+1, indexFim1-1);
                condicao(linha, indexComeco2+1, end-1);
            }
            //1 dimensao
            else{
                condicao(linha, start+1, end-1);
            }
            
            String valor = "";
            
            for(int i = start; i <= end; i++){
                valor += "".equals(linha.get(i).getValor()) ?
                              linha.get(i).getTipo():
                              linha.get(i).getValor();
            }
            
            return new ArvoreBinaria<>(new Token("id", valor));
        } else {
            throw new ErroSintatico("Termo invalido");
        }
    }
    private ArvoreBinaria<Token> funcao(ArrayList<Token> linha, int start, int end)
            throws ErroSintatico {
        if (end - start > 2
            && (!"fun".equals(linha.get(start).getTipo())
                || !"(".equals(linha.get(start + 1).getTipo())
                || !")".equals(linha.get(end).getTipo()))) {
            throw new ErroSintatico("Funcao mal formada");
        }
        
        int i = start+2;
        int virgula;
        //Analise dos parametros
        while (i != end) {
            virgula = indexOf(linha, new Token(",", ""), i, end-1);
            if (virgula != -1) {
                condicao(linha, i, virgula - 1);
                i = virgula + 1;
            } else {
                condicao(linha, i, end-1);
                i = end;
            }
        }
            
        String valor = "";
        for(int j = start; j <= end; j++){
            valor += "".equals(linha.get(j).getValor()) ?
                          linha.get(j).getTipo():
                          linha.get(j).getValor();
        }

        return new ArvoreBinaria<>(new Token("fun", valor));
    }

    /* ==================================================================== */
    
    
    
    /* FUNCAO PRINCIPAL */
    
    public boolean analisar(boolean print) {
        boolean erro = false;
        
        /* Retira os endinstr e unifica as linhas */
        linha2instr();

        /* Analise de blocos do codigo */
        erro |= programa();
        erro |= estruturaBlocos();
        //erro |= atribuicoes();
        //erro |= declVetor();
        
        
        //Imprime os erros da execucao
        if(erro){
            //Ordena os erros pela linha em que ocorreram
            Collections.sort(erros);
            
            for(ErroSintatico e: erros){
                System.err.println(e.toString());
            }
        }
        
        
        //Se fez analise sintatica corretamente, mostra as arvores
        //    sintaticas resultantes
        else if (print) {
            System.out.println("\n\nAnalise Sintatica:");
            for (Map.Entry<Integer, ArvoreBinaria<Token>> entry : arvores.entrySet()) {
                System.out.print(entry.getKey());
                entry.getValue().print();
                System.out.println("\n");
            }
            System.out.println("\n\n");
        }

        return erro;
    }
    
}
