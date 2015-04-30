/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AnalisadorSintatico;

/**
 *
 * @author Elias
 */
public class ErroSintatico extends Exception{
    public String erro;

    public ErroSintatico(String e) {
        erro = e;
    }
}
