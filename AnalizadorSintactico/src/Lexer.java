import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private String input;
    private int pos;

    public Lexer(String input) {
        this.input = input;
        this.pos = 0;
    }
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < input.length()) {
            char current = input.charAt(pos);
            //Identificar y saltar espacios en blanco
            if (Character.isWhitespace(current)) {
                pos++;
                continue;
            }
            //Identificar comentarios de una linea
            if (current == '/' && peek() == '/') {
                StringBuilder comentario = new StringBuilder();
                comentario.append("//");
                pos += 2;
                while (pos < input.length() && input.charAt(pos) != '\n') {
                    comentario.append(input.charAt(pos));
                    pos++;
                }
                tokens.add(new Token(TokenType.COMENTARIO_DE_LINEA, comentario.toString()));
                continue;
            }
            //Identificar comentarios de varias lineas
            if (current == '/' && peek() == '*') {
                StringBuilder comentario = new StringBuilder();
                comentario.append("/*");
                pos += 2;
                while (pos < input.length()) {
                    if (input.charAt(pos) == '*' && peek() == '/') {
                        comentario.append("*/");
                        pos += 2;
                        break;
                    }
                    comentario.append(input.charAt(pos));
                    pos++;
                }
                tokens.add(new Token(TokenType.COMENTARIO_DE_VARIAS_LINEAS, comentario.toString()));
                continue;
            }
            //Identificar identificadores y palabras clave
            if (Character.isLetter(current) || current == '_') {
                tokens.add(leerIdentificadorPalabrasReservadas());
                continue;
            }
            //Identificar cadenas
            if (current == '"') {
                StringBuilder sb = new StringBuilder();
                pos++; 
                boolean cerrado = false;
                while (pos < input.length()) {
                    char c = input.charAt(pos);
                    if (c == '"') {
                        cerrado = true;
                        pos++; 
                        break;
                    }
                    sb.append(c);
                    pos++;
                }
                if (!cerrado) {
                    tokens.add(new Token(TokenType.ERROR, "STRING_NO_CERRADA: " + sb.toString()));
                }else
                    tokens.add(new Token(TokenType.STRING, sb.toString()));
                continue;
            }
            //Identificar numeros
            if (Character.isDigit(current)) {
                tokens.add(readNumber());
                continue;
            }
            //Identificar otros simbolos (operacion y agrupacion)
            switch (current) {
                case ':':
                    if (current == ':' && peek() == '=') {
                        tokens.add(new Token(TokenType.ASSIGN, ":="));
                        pos += 2;
                        continue;
                    }else{
                        tokens.add(new Token(TokenType.DOS_PUNTOS, String.valueOf(current)));
                        pos++;
                    }
                    break;
                case '+':
                    tokens.add(new Token(TokenType.PLUS, String.valueOf(current)));
                    pos++;
                    break;
                case '-':
                    tokens.add(new Token(TokenType.MINUS, String.valueOf(current)));
                    pos++;
                    break;
                case '*':
                    tokens.add(new Token(TokenType.MULT, String.valueOf(current)));
                    pos++;
                    break;
                case '/':
                    tokens.add(new Token(TokenType.DIV, String.valueOf(current)));
                    pos++;
                    break;
                case ';':
                    tokens.add(new Token(TokenType.PUNTO_Y_COMA, String.valueOf(current)));
                    pos++;
                    break;
                case '(':
                    tokens.add(new Token(TokenType.PARENTESIS_ABIERTO, String.valueOf(current)));
                    pos++;
                    break;
                case ')':
                    tokens.add(new Token(TokenType.PARENTESIS_CERRADO, String.valueOf(current)));
                    pos++;
                    break;
                case '{':
                    tokens.add(new Token(TokenType.LLAVE_ABIERTA, String.valueOf(current)));
                    pos++;
                    break;
                case '}':
                    tokens.add(new Token(TokenType.LLAVE_CERRADA, String.valueOf(current)));
                    pos++;
                    break;
                case '[':
                    tokens.add(new Token(TokenType.CORCHETE_ABIERTO, String.valueOf(current)));
                    pos++;
                    break;
                case ']':
                    tokens.add(new Token(TokenType.CORCHETE_CERRADO, String.valueOf(current)));
                    pos++;
                    break;
                default:
                    tokens.add(new Token(TokenType.ERROR, String.valueOf(current)));
                    pos++;
                    break;
            }
        }
        return tokens;
    }
    private Token leerIdentificadorPalabrasReservadas() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isLetterOrDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            pos++;
        }
        String palabra = sb.toString().toLowerCase();
        TokenType tipo;
        //buleanos
        if(palabra.equals("true") || palabra.equals("false")){
            tipo = TokenType.BOOLEANO;
        }
        //nulo
        else if(palabra.equals("null")){
            tipo = TokenType.NULL;
        }
        //modificadores de acceso
        else if(palabra.equals("public") || palabra.equals("private") || palabra.equals("protected")){
            tipo = TokenType.MODIFICADOR_ACCESO;
        }
        //modificadores de comportamiento
        else if(palabra.equals("static") || palabra.equals("final") || palabra.equals("abstract")){
            tipo = TokenType.MODIFICADOR_COMPORTAMIENTO;
        }
        //Identificadores
        else 
            tipo = TokenType.IDENTIFIER;
        return new Token(tipo, sb.toString());
    }
    private Token readNumber() {
        StringBuilder sb = new StringBuilder();
        boolean hasDot = false;
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (Character.isDigit(c)) {
                sb.append(c);
                pos++;
            }
            else if (c == '.' && !hasDot) {
                hasDot = true;
                sb.append(c);
                pos++;
            }
            else {
                break;
            }
        }
        return new Token(TokenType.NUMBER, sb.toString());
    }
    private char peek() {
        if (pos + 1 >= input.length()) return '\0';
        return input.charAt(pos + 1);
    }
    private char peek(int offset) {
        if (pos + offset >= input.length()) return '\0';
        return input.charAt(pos + offset);
    }
    public void mostrarAnalisis() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mostrarAnalisis'");
    }
}
