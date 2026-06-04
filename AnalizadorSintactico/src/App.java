import java.util.Scanner;

public class App {
    public static void main(String[] args) throws Exception {
        Scanner scan = new Scanner(System.in);
        String linea = new String();

        System.out.println("============= ANALIZADOR =============");
        System.out.println("----- Escriba la linea de codigo -----");
        linea = scan.nextLine();
        System.out.println("----------- ANALISIS LEXICO ----------");
        Lexer lexer = new Lexer(linea);
        lexer.mostrarAnalisis();
    }
}
