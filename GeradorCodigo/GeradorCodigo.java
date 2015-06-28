package GeradorCodigo;

import AnalisadorLexico.Token;
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

public class GeradorCodigo {
    
    /* VARIAVEIS */
    private LinkedHashMap<Integer, ArrayList<Token>> linhas;
    private HashMap<Integer, ArvoreBinaria<Token>> arvores;
    private TabelaSimbolos tabelaSimbolos;
    private BufferedWriter file;
    //Vetor para controle de quem esta em cada um dos 10 "registradores"
    private String[] variaveis;
    
    
    
    /* CONSTRUTORES */
    public GeradorCodigo(String out, String pathTo, String pathA, String pathTa)
            throws IOException, ClassNotFoundException {
        carregar(pathTo, pathA, pathTa);
        this.file = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(out))));
        variaveis = new String[10];
    }
    public GeradorCodigo(String out,
                         LinkedHashMap<Integer, ArrayList<Token>> linhas,
                         HashMap<Integer, ArvoreBinaria<Token>> arvores,
                         TabelaSimbolos tabelaSimbolos) 
            throws FileNotFoundException {
        this.linhas = linhas;
        this.arvores = arvores;
        this.tabelaSimbolos = tabelaSimbolos;
        this.file = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(out))));
        variaveis = new String[10];
    }

    
    
    /* IO */
    private void carregar(String pathTo, String pathA, String pathTa) throws IOException, ClassNotFoundException {
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
        }
    }

    
    
    /* FUNCOES AUXILIARES */
    private void se(ArrayList<Token> linha){
        
    }
    private void senao(ArrayList<Token> linha){
        
    }
    private void funcao(ArrayList<Token> linha){
        
    }
    private void retorno(ArrayList<Token> linha){
        
    }
    private void para(ArrayList<Token> linha){
        
    }
    private void enquanto(ArrayList<Token> linha){
        
    }
    private void fim(){
        escrever("}\n");
    }
    private ArrayList<ArrayList<Token>> leArvore(int nLinha){
        ArvoreBinaria<Token> arvore = arvores.get(nLinha);
        
    }
    private void atribuicao(int nLinha){
        
        
    }

    
    
    /* FUNCAO PRINCIPAL */
    public void executar(boolean print) {
        
    }       
}
