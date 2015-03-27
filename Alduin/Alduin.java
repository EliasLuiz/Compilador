package Alduin;

import AnalisadorLexico.AnalisadorLexico;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Alduin {
    public static void main(String[] args){
        AnalisadorLexico a = new AnalisadorLexico();
        
        String ipath = "exemplo.txt", //como fazer para multiplos arquivos?
               opath = "";
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
                }
            }
        } catch (Exception ex) {
            System.err.println("Erro na passagem dos parâmetros");
            Logger.getLogger(Alduin.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try { a.analisar(ipath, print); } 
        catch (IOException ex) {
            System.err.println("Arquivo " + ipath + " não pode ser aberto");
            Logger.getLogger(Alduin.class.getName()).log(Level.SEVERE, null, ex);
        }
        try { a.salvar("tokens.tmp"); }
        catch (IOException ex) {
            Logger.getLogger(Alduin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
