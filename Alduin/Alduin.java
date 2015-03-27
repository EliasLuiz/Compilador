package Alduin;

import AnalisadorLexico.AnalisadorLexico;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Alduin {
    public static void main(String[] args){
        
        String ipath = "teste.txt", //como fazer para multiplos arquivos?
               opath = "exemplo.js";
        boolean print = false;
        
        try{
            //leitura dos argumentos da linha de comando
            for(int i = 0; i<args.length; i++){
                if(null != args[i])
                    switch (args[i]) {
                    case "-o":
                        //arquivo de output (compilado)
                        opath = args[i+1];
                        break;
                    case "-i":
                        //arquivo de input (codigo-fonte)
                        ipath = args[i+1];
                        break;
                    case "-print":
                        //imprimir passo a passo
                        print = true;
                        break;
                    case "-help":
                        //imprimir a ajuda do programa
                        System.out.println("");
                        System.out.println("-i arquivo    --    arquivo fonte a ser analisado");
                        System.out.println("-o arquivo    --    arquivo de saida compilado");
                        System.out.println("-print        --    imprime o processo de "
                                         + "compilacao passo-a-passo");
                        System.out.println("-help         --    imprime a ajuda do programa");
                        System.out.println("");
                        System.exit(0);
                    }
            }
        } catch (Exception ex) {
            System.err.println("Erro na passagem dos parametros");
            Logger.getLogger(Alduin.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        AnalisadorLexico a = new AnalisadorLexico();
        
        try { a.analisar(ipath, true); } 
        catch (IOException ex) {
            System.err.println("Arquivo " + ipath + " nao pode ser aberto");
            Logger.getLogger(Alduin.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try { a.salvar("tokens.tmp"); }
        catch (IOException ex) {
            Logger.getLogger(Alduin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
