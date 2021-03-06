package AnalisadorSemantico;

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
    private int indexOf(ArrayList array, Object x, int start, int end) {
        for (int i = start; i <= end; i++) {
            if (array.get(i).equals(x)) {
                return i;
            }
        }
        return -1;
    }
    private int indexOfTipo(ArrayList<Token> array, String tipo, int start, int end) {
        for (int i = start; i <= end; i++) {
            if (array.get(i).getTipo().equals(tipo)) {
                return i;
            }
        }
        return -1;
    }
    //Procura o elemento mais a direita fora de parenteses
    //  usada devido ao fato de a gramatica derivar a esquerda
    private int rIndexOfParen(ArrayList array, Object x, int start, int end) throws ErroSemantico{
        int pilha = 0;
        for (int i = end; i >= start; i--) {
            if (array.get(i).equals(new Token(")", "")))
                pilha++;
            else if (array.get(i).equals(new Token("(", "")))
                pilha--;
            if (   array.get(i).equals(x)
                && pilha == 0) {
                return i;
            }
        }
        return -1;
    }
    //Verifica se contem determinado token
    private boolean contains(ArrayList<Token> linha, Token t, int start, int end){
        for (int i = start; i <= end; i++) {
            if(t.equals(linha.get(i)))
                return true;
        }
        return false;
    }
    //Verifica se tem determinado tipo de variavel
    private boolean contains(ArrayList<Token> linha, String tipo, int start, int end) throws ErroSemantico{
        for (int i = start; i <= end; i++){
            if(tipo.equals(linha.get(i).getTipo()))
                return true;
            else if(   "id".equals(linha.get(i).getTipo())
                    || "fun".equals(linha.get(i).getTipo())){
                Simbolo s = tabelaSimbolos.getSimbolo(escopos.getVariavel(linha.get(i).getValor()), nLinha);
                if (s.tipo.equals(tipo))
                    return true;
            }
        }
        return false;
    }
    
    
    
    /* FUNCOES DE ANALISE SEMANTICA */
    //Retorna o tipo de um expressao/condicao
    private String tipoExpressao(ArrayList<Token> linha, int start, int end) throws ErroSemantico {
        
        //Se nao existe expressao
        if(start > end)
            return "null";
        
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
                || contains(linha, new Token("<", ""), start, end)
                || contains(linha, new Token("and", ""), start, end)
                || contains(linha, new Token("or", ""), start, end)
                || contains(linha, new Token("not", ""), start, end)
                || contains(linha, new Token("true", ""), start, end)
                || contains(linha, new Token("false", ""), start, end)
                || contains(linha, "bool", start, end))
            return "bool";
        //Se e string
        else if(contains(linha, "str", start, end))
            return "str";
        //Se e float
        else if(contains(linha, "float", start, end) ||
                contains(linha, new Token("/", ""), start, end))
            return "float";
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
            
            if(   (   op < 6 
                   && tipoDir.equals(tipoEsq) 
                   && !"null".equals(tipoDir) 
                   && !"bool".equals(tipoDir))              //Se e comparador
               || (   op < 8 
                   && tipoDir.equals(tipoEsq) 
                   && "bool".equals(tipoDir))               //Se e operador logico binario
               || (   op == 8 
                   && "null".equals(tipoEsq) 
                   && "bool".equals(tipoDir))               //Se e operador logico unario
               || (    op == 9 
                   &&  tipoDir.equals(tipoEsq) 
                   && "num".equals(tipoDir))                //Se e operador +
               || (    op == 10 
                   && (   (   tipoDir.equals(tipoEsq) 
                           && "num".equals(tipoDir))
                       || 
                          (   "null".equals(tipoEsq)
                           && "num".equals(tipoDir))))      //Se e operador -
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
        String nomeVar;
        boolean isVetor = false;
        
        //identificacao de vetor vetor
        if(var.contains("[")){
            nomeVar = var.substring(0, var.indexOf('[')).trim();
            isVetor = true;
        }
        else
            nomeVar = var;
        
        //Declaracao da variavel + definicao do tipo
        escopos.adicionaVariavel(nomeVar);
        String s = escopos.getVariavel(nomeVar);
        tabelaSimbolos.addSimbolo( new Simbolo(nomeVar, tipoExpressao(linha, 2, linha.size()-1), 
                escopos.getVariavel(nomeVar), nLinha, false, isVetor));
        
        //Checagem de consistencia
        consistenciaTipo(linha, 2, linha.size()-1);
    }
    private void testaParametros(ArrayList<Token> linhaChamada, int start) throws ErroSemantico {
        int nivel = 0, fim = start+2;
        //Encontrando o fim da chamada
        for (int i = start+1; i < linhaChamada.size(); i++) {
            Token t = linhaChamada.get(i);
            if("(".equals(t.getTipo()))
                nivel++;
            else if(")".equals(t.getTipo()))
                nivel--;
            if(nivel == 0){
                fim = i;
                break;
            }
        }
        ArrayList<String> params = tabelaSimbolos.getSimbolo(
                linhaChamada.get(start).getValor(), 0).parametros;
        int cont = 0;
        //Testa os parametros
        for (int i = start+2; i < fim; i++){
            Token t = linhaChamada.get(i);
            int limite = indexOf(linhaChamada, new Token(",", ""), start, linhaChamada.size()-1)-1;
            if(limite == -2)
                limite = fim;
            try{
                if(!tipoExpressao(linhaChamada, i, limite).equals(params.get(cont)))
                    throw new ErroSemantico("Tipo invalido de parametro para \"" +
                            linhaChamada.get(start).getValor() + "\" (Parametro " + cont + " e " + 
                            tipoExpressao(linhaChamada, i, limite) + " e era esperado" +
                            params.get(cont) + ").");
            } catch (IndexOutOfBoundsException e){
                throw new ErroSemantico("Quantidade invalida de parametros para \"" +
                        linhaChamada.get(start).getValor() + "\" (" + params.size() + " parametros).");
            }
            i = limite+1;
            cont++;
        }
        if(cont != params.size())
            throw new ErroSemantico("Quantidade invalida de parametros para \"" +
                    linhaChamada.get(start).getValor() + "\" (" + params.size() + " parametros).");
    }
    //Analise/Declaracao de funcao
    //Nota: funcoes => lazy evaluation
    //Nota2: tipagem de funcao baseado no tipo dos parametros da 1a chamada a mesma
    //       ja que o tipo dos parametros e determinado pelo tipo dos valores passados
    //       na 1a chamada
    //       tipo do retorno assumido como tipo do 1o comando return
    private void analisaFuncao(ArrayList<Token> linhaChamada, int start) throws ErroSemantico {
        
        //Inicializa o simbolo que representara a funcao (assume que nao esta na tabela de simbolos)
        Simbolo funcao = new Simbolo(linhaChamada.get(start).getValor(), "null", 
                linhaChamada.get(start).getValor(), nLinha, true, false);
        
        //Procura o fim da funcao
        int nivel = 1, fim = start + 2;
        while (nivel > 0){
            if("(".equals(linhaChamada.get(fim).getTipo()))
                nivel++;
            else if(")".equals(linhaChamada.get(fim).getTipo()))
                nivel--;
            fim++;
        }
        fim--;
        //Adiciona tipo dos parametro da funcao
        for (int i = start + 2; i <= fim; i++) {
            int limite = indexOf(linhaChamada, new Token(",", ""), i, fim) -1 ;
            if(limite == -2)
                limite = fim-1;
            String tipo = tipoExpressao(linhaChamada, i, limite);
            if(!"null".equals(tipo))
                funcao.addParametro(tipo);
            i = limite + 1;
        }
        
        //Backup dos escopos antigos
        Escopo backup = escopos;
        escopos = new Escopo();
        escopos.idEscopo = backup.idEscopo;
        int backupLinha = nLinha;
        
        
        boolean naFuncao = false, encontrouFuncao = false;
        
        for (Map.Entry<Integer, ArrayList<Token>> entrySet : linhas.entrySet()) {
            nLinha = entrySet.getKey();
            ArrayList<Token> linha = entrySet.getValue();
            
            //Se chegou na funcao
            if(   contains(linha, new Token("fun", funcao.nome), 0, linha.size()-1)
               && contains(linha, new Token("def", ""), 0, linha.size()-1)){
                
                naFuncao = true;
                encontrouFuncao = true;
                escopos.adicionaEscopo();
                
                //Adiciona parametro da funcao na tabela de simbolos
                int cont = 0;
                fim = linha.size()-1;
                for (int i = 3; i < fim; i++) {
                    int limite = indexOf(linha, new Token(",", ""), i, fim) -1 ;
                    if(limite == -2)
                        limite = fim-1;
                    String nome = linha.get(i).getValor();
                    escopos.adicionaVariavel(nome);
                    Simbolo s = null;
                    try{
                        s = new Simbolo(nome, funcao.parametros.get(cont),
                                escopos.getVariavel(nome), nLinha, false, false);
                    } catch (IndexOutOfBoundsException e){
                        throw new ErroSemantico("Quantidade invalida de parametros para \"" +
                                funcao.nome + "\" (" + funcao.parametros.size() + " parametros).");
                    }
                    tabelaSimbolos.addSimbolo(s);
                    i = limite + 1;
                    cont++;
                }
                if(cont != funcao.parametros.size())
                    throw new ErroSemantico("Quantidade invalida de parametros para \"" +
                            funcao.nome + "\" (" + funcao.parametros.size() + " parametros).");
            }
            
            //Se nao esta na funcao
            if (!naFuncao)
                continue;
            
            //Se saiu da funcao
            if(contains(linha, new Token("enddef", ""), 0, linha.size()-1)){
                funcao.tipo = "null";
                break;
            }
            
            //Analisa normalmente a funcao
            try {
                analisaLinha(linha);
            } catch (ErroSemantico e) {
                erros.add(new ErroSemantico(nLinha, e.erro));
            }
            
            //Se encontrou valor de retorno
            if(   "=".equals(linha.get(1).getTipo())
               && funcao.nome.equals(linha.get(0).getValor())){
                funcao.tipo = tipoExpressao(linha, 2, linha.size()-1);
                break;
            }
        }
        
        //Restaura backup de escopo atualizando os ids
        backup.idEscopo = escopos.idEscopo;
        escopos = backup;
        nLinha = backupLinha;
        
        //Caso a funcao nao tenha sido declarada
        if(!encontrouFuncao)
            throw new ErroSemantico("Funcao " + funcao.nome + " nao declarada.");
        
        escopos.adicionaFuncao(funcao.nome);
        tabelaSimbolos.addSimbolo(funcao);
        
    }
    //Analisa uma linha de codigo, chamando as funcoes especificas
    public void analisaLinha(ArrayList<Token> linha) throws ErroSemantico {
        
        try {
            
            //Caso conteudo da linha seja um se
            if("if".equals(linha.get(0).getTipo())){
                escopos.adicionaEscopo();
                testeCondicao(linha, "se");
            }
            
            //Caso conteudo da linha seja um enquanto
            else if("while".equals(linha.get(0).getTipo())){
                escopos.adicionaEscopo();
                testeCondicao(linha, "enquanto");
            }
            
            //Caso conteudo da linha seja um para
            else if("for".equals(linha.get(0).getTipo())){
                escopos.adicionaEscopo();
                escopos.adicionaVariavel(linha.get(1).getValor());
                tabelaSimbolos.addSimbolo(new Simbolo(linha.get(1).getValor(), "int",
                        escopos.getVariavel(linha.get(1).getValor()), nLinha, false, false));
            }
            
            //Caso conteudo da linha seja uma atribuicao
            else if (indexOf(linha, new Token("=", ""), 0, linha.size()-1) != -1) {
                testeAtribuicao(linha);
            }
            
            //Caso conteudo da linha seja um senao
            else if("else".equals(linha.get(0).getTipo())){
                escopos.removeEscopo();
                escopos.adicionaEscopo();
            }
            
            //Caso conteudo da linha seja um fim-se
            else if("endif".equals(linha.get(0).getTipo())){
                escopos.removeEscopo();
            }
            
            //Caso conteudo da linha seja um fim-enquanto
            else if("endwhile".equals(linha.get(0).getTipo())){
                escopos.removeEscopo();
            }
            
            //Caso conteudo da linha seja um fim-para
            else if("endfor".equals(linha.get(0).getTipo())){
                escopos.removeEscopo();
            }
        
        } catch (ErroSemantico e) {
                e.linha = nLinha;
                erros.add(e);
        }
    }
    
    
    
    /* FUNCAO PRINCIPAL */
    public boolean analisar(boolean print){
        
        boolean isFuncao = false;
        
        //Analise do programa
        for (Map.Entry<Integer, ArrayList<Token>> entrySet : linhas.entrySet()) {
            nLinha = entrySet.getKey();
            ArrayList<Token> linha = entrySet.getValue();
            
            //Pula as definicoes de funcao
            if(contains(linha, new Token("def", ""), 0, linha.size()-1))
                isFuncao = true;
            else if(contains(linha, new Token("enddef", ""), 0, linha.size()-1)){
                isFuncao = false;
                continue;
            }
            
            if (isFuncao)
                continue;
                
            //Se encontrou chamada a uma funcao a analisa
            int indexFun = indexOfTipo(linha, "fun", 0, linha.size()-1);
            while(indexFun != -1) {
                try {
                    escopos.getVariavel(linha.get(indexFun).getValor());
                    testaParametros(linha, indexFun);
                } catch (ErroSemantico e) { //Se nao foi declarado
                    try { 
                        analisaFuncao(linha, indexFun);
                        testaParametros(linha, indexFun);
                    } 
                    catch (ErroSemantico ex) { 
                        erros.add(new ErroSemantico(nLinha, ex.erro)); 
                    }
                }
                //Caso tenha mais de uma chamada de funcao na mesma linha
                indexFun = indexOfTipo(linha, "fun", indexFun+1, linha.size()-1);
            }
                
            try { analisaLinha(linha); } 
            catch (ErroSemantico e) {
                erros.add(new ErroSemantico(nLinha, e.erro)); 
            }
        }
        
        Collections.sort(erros);
        for (ErroSemantico e : erros) {
            System.err.println(e);
        }
        
        if(print && erros.isEmpty()){
            System.out.println("Analise Semantica:");
            tabelaSimbolos.print();
        }
        
        return !erros.isEmpty();
    }
}
