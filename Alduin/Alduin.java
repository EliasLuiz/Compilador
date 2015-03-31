package Alduin;

import AnalisadorLexico.AnalisadorLexico;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Alduin {
    public static void main(String[] args){
        
        String ipath = "", //como fazer para multiplos arquivos?
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
                    case "-logo":
                        System.out.println("" +
"                       .$          M.                       \n" +
"                      .MD          M.                       \n" +
"                     .MMM.       .MMM                       \n" +
"                     .MM .       ,  MM                      \n" +
"                     MM.  ..        MMM                     \n" +
"                    MMM   ..        .MM.                    \n" +
"                   MMM    M+         MMM.                   \n" +
"                   MMM    IM     .    MMM.                  \n" +
"                  MMM?    MMMMMM      MMMM                  \n" +
"                 MMMM    NMMMMD       MMMM                  \n" +
"                MMMMM    MMM.  DMM.    MMMM                 \n" +
"               .MMMM.    M       MM    MMMMM                \n" +
"               MMMMM    .M       .M    NMMMM.               \n" +
"              MMMMMM.           ,M+    .MMMMM               \n" +
"              MMMMMMM       ..MMM.    .MMMMMMM.             \n" +
"            .MMMMMM. M.   .MMMD.     M...MMMMMN.            \n" +
"           .MMMMMM       .MM             MMMMMM             \n" +
"          .8MMMMMM       MMM             ?MMMMMM            \n" +
"          .MMMMMMM       ZMMM             MMMMMMN           \n" +
"          MMMMMMM~  .M .. MMMM.    . M?   MMMMMMM$          \n" +
"         MMMMMMMM    .MM MNMMMM.  $MM     MMMMMMMM          \n" +
"         ?MMMMMMMMM+$MMMMMMMMMMM~MMMMMM:DNMMMMMMMM.         \n" +
"          MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM.          \n" +
"           MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMI           \n" +
"            MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM            \n" +
"            MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM             \n" +
"             MMMMMMM.MMMMMMMMMMMMMMMMM8MMMMMMM.             \n" +
"              MMMMMM: .MM MMMMMMMN.M.  MMMMMMM              \n" +
"              ZMMMMM.  .M ..MMMMM  D. .MMMMMM               \n" +
"               MMMMM.   .    MMM       MMMMM                \n" +
"                MMMM         MMM       ,MMMO                \n" +
"                .MMM          MMN     .MMMM                 \n" +
"                 MMMM         MMM    .MMMM                  \n" +
"                  NMMMM.     .MMM  .MMMMM                   \n" +
"                  .MMMMM     8MM   MMMMMM                   \n" +
"                    MMMM    ~MM.   MMMMM.                   \n" +
"                    MMMM    MM.    MMMM                     \n" +
"                     MMM  .MM      MMM                      \n" +
"                     .MM  ,M?      MMM                      \n" +
"                      MM  .MM      MM                       \n" +
"                       M.  .M.     M                        \n" +
"                        .  ,M.     M                        \n" +
"                          NM                                \n" +
"                         +M                                 \n" +
"                         .M     ..                          \n" +
"                          MM. .MM                           \n" +
"                           MMMMM                            \n" +
"                            MMM                             \n" +
"                            ,MN                             \n" +
"                             M                              ");
                        break;
                    }
            }
        } catch (Exception ex) {
            System.err.println("Erro na passagem dos parametros");
            Logger.getLogger(Alduin.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        
        AnalisadorLexico a = new AnalisadorLexico();
        
        if(ipath != ""){
            try { a.analisar(ipath, true); } 
            catch (IOException ex) {
                System.err.println("Arquivo " + ipath + " nao pode ser aberto");
                Logger.getLogger(Alduin.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
            }

            try { a.salvar("tokens.tmp"); }
            catch (IOException ex) {
                Logger.getLogger(Alduin.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
            }
        }
    }
}
