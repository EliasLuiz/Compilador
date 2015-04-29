package AnalisadorSintatico;

public class AnalisadorSintatico {
    
    public static void main(String[] args) {
        ArvoreBinaria<Character> w = new ArvoreBinaria<>('=');
        
        ArvoreBinaria<Character> x = new ArvoreBinaria<>('x');
        w.setEsq(x);
        
        ArvoreBinaria<Character> y = new ArvoreBinaria<>('+');
        y.setEsq(new ArvoreBinaria<>('1'));
        y.setDir(new ArvoreBinaria<>('2'));
        w.setDir(y);
        
        System.out.println(w.inOrdem().toString());
        w.print();
    }
    
}
