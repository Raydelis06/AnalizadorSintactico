import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lexer {
    private String input;
    private int pos;
    private int lineaActual = 1;
    private List<String> modificadoresAcceso = Arrays.asList("public", "private", "protected");
    private List<String> modificadoresComportamiento = Arrays.asList("static", "final", "abstract");
    
    // Se añadió "print" a la lista de palabras clave para que el Lexer no lo confunda con una variable - JuanC
    private List<String> otrasPalabrasClave = Arrays.asList("int", "string", "new", "if", "while", 
        "for", "return", "do", "char", "else", "break", "boolean", "finally", "super",
        "package", "switch", "case", "continue", "default", "long", "byte", "implements", 
        "double", "interface", "extends", "this", "void"
    );

    public Lexer(String input) {
        this.input = input;
        this.pos = 0;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < input.length()) {
            char current = input.charAt(pos);

            if (current == '\n') {
                lineaActual++;
                pos++;
                continue;
            }

            if (Character.isWhitespace(current)) {
                pos++;
                continue;
            }

            if (current == '/' && obtenerCaracterSiguiente() == '/') {
                StringBuilder comentario = new StringBuilder();
                comentario.append("//");
                pos += 2;
                while (pos < input.length() && input.charAt(pos) != '\n') {
                    comentario.append(input.charAt(pos));
                    pos++;
                }
                tokens.add(new Token(TokenType.COMENTARIO_DE_LINEA, comentario.toString(), lineaActual));
                continue;
            }

            if (current == '/' && obtenerCaracterSiguiente() == '*') {
                StringBuilder comentario = new StringBuilder();
                comentario.append("/*");
                pos += 2;
                boolean cerrado = false;
                while (pos < input.length()) {
                    if (input.charAt(pos) == '\n') {
                        lineaActual++;
                    }
                    if (input.charAt(pos) == '*' && obtenerCaracterSiguiente() == '/') {
                        comentario.append("*/");
                        cerrado = true;
                        pos += 2;
                        break;
                    }
                    comentario.append(input.charAt(pos));
                    pos++;
                }
                if(cerrado)
                    tokens.add(new Token(TokenType.COMENTARIO_DE_VARIAS_LINEAS, comentario.toString(), lineaActual));
                else
                    tokens.add(new Token(TokenType.ERROR, "COMENTARIO_SIN_CERRAR " + comentario.toString(), lineaActual));
                continue;
            }

            //reconoce System.out.println para ejecutar un print
            if (input.startsWith("System.out.println", pos)) {
                tokens.add(new Token(TokenType.PRINT, "System.out.println", lineaActual));
                pos += "System.out.println".length();
                continue;
            }
            //reconoce las ibrerias importadas
            if (input.startsWith("import", pos)) {
                pos += "import".length();
                while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
                    pos++;
                }
                StringBuilder paquete = new StringBuilder();

                while (pos < input.length() && input.charAt(pos) != ';') {
                    paquete.append(input.charAt(pos));
                    pos++;
                }
                tokens.add(new Token(TokenType.IMPORT,
                    paquete.toString().trim(),
                    lineaActual));
                continue;
            }

            if (Character.isLetter(current) || current == '_') {
                tokens.add(leerIdentificadorPalabrasReservadas());
                continue;
            }

            if (current == '"') {
                StringBuilder sb = new StringBuilder();
                pos++; 
                boolean cerrado = false;
                while (pos < input.length()) {
                    char c = input.charAt(pos);
                    if (c == '\n') {
                        lineaActual++;
                    }
                    if (c == '"') {
                        cerrado = true;
                        pos++; 
                        break;
                    }
                    sb.append(c);
                    pos++;
                }
                if (!cerrado) {
                    tokens.add(new Token(TokenType.ERROR, "STRING_NO_CERRADA: " + sb.toString(), lineaActual));
                }else
                    tokens.add(new Token(TokenType.STRING, sb.toString(), lineaActual));
                continue;
            }

            if (Character.isDigit(current)) {
                tokens.add(readNumber());
                continue;
            }

            switch (current) {
                case ':':
                    if (obtenerCaracterSiguiente() == '=') {
                        tokens.add(new Token(TokenType.ASSIGN, ":=", lineaActual));
                        pos += 2;
                        continue;
                    }else{
                        tokens.add(new Token(TokenType.DOS_PUNTOS, String.valueOf(current), lineaActual));
                        pos++;
                    }
                    break;
                case '.':
                    tokens.add(new Token(TokenType.PUNTO, String.valueOf(current), lineaActual));
                    pos++;
                    break;
                case ',':
                    tokens.add(new Token(TokenType.COMA, String.valueOf(current), lineaActual));
                    pos++;
                    break;
                case '=':
                    if (obtenerCaracterSiguiente() == '=') {
                        tokens.add(new Token(TokenType.OPERADOR_COMPARACION, "==", lineaActual));
                        pos += 2;
                        continue;
                    }else{
                        tokens.add(new Token(TokenType.ASSIGN, String.valueOf(current), lineaActual));
                        pos++;
                    }
                    break;
                case '+':
                    if (obtenerCaracterSiguiente() == '=') {
                        tokens.add(new Token(TokenType.OPERADOR_ARITMETICO, "+=", lineaActual));
                        pos += 2;
                        continue;
                    }else{
                        tokens.add(new Token(TokenType.PLUS, String.valueOf(current), lineaActual));
                        pos++;
                    }
                    break;
                case '-':
                    if (obtenerCaracterSiguiente() == '=') {
                        tokens.add(new Token(TokenType.OPERADOR_ARITMETICO, "-=", lineaActual));
                        pos += 2;
                        continue;
                    }else{
                        tokens.add(new Token(TokenType.MINUS, String.valueOf(current), lineaActual));
                        pos++;
                    }
                    break;
                case '*':
                    if (obtenerCaracterSiguiente() == '=') {
                        tokens.add(new Token(TokenType.OPERADOR_ARITMETICO, "*=", lineaActual));
                        pos += 2;
                        continue;
                    }else{
                        tokens.add(new Token(TokenType.MULT, String.valueOf(current), lineaActual));
                        pos++;
                    }
                    break;
                case '/':
                    if (obtenerCaracterSiguiente() == '=') {
                        tokens.add(new Token(TokenType.OPERADOR_ARITMETICO, "/=", lineaActual));
                        pos += 2;
                        continue;
                    }else{
                        tokens.add(new Token(TokenType.DIV, String.valueOf(current), lineaActual));
                        pos++;
                    }
                    break;
                case ';':
                    tokens.add(new Token(TokenType.PUNTO_Y_COMA, String.valueOf(current), lineaActual));
                    pos++;
                    break;
                case '(':
                    tokens.add(new Token(TokenType.PARENTESIS_ABIERTO, String.valueOf(current), lineaActual));
                    pos++;
                    break;
                case ')':
                    tokens.add(new Token(TokenType.PARENTESIS_CERRADO, String.valueOf(current), lineaActual));
                    pos++;
                    break;
                case '{':
                    tokens.add(new Token(TokenType.LLAVE_ABIERTA, String.valueOf(current), lineaActual));
                    pos++;
                    break;
                case '}':
                    tokens.add(new Token(TokenType.LLAVE_CERRADA, String.valueOf(current), lineaActual));
                    pos++;
                    break;
                case '[':
                    tokens.add(new Token(TokenType.CORCHETE_ABIERTO, String.valueOf(current), lineaActual));
                    pos++;
                    break;
                case ']':
                    tokens.add(new Token(TokenType.CORCHETE_CERRADO, String.valueOf(current), lineaActual));
                    pos++;
                    break;
                case '!':
                    if (obtenerCaracterSiguiente() == '=') {
                        tokens.add(new Token(TokenType.OPERADOR_COMPARACION, "!=", lineaActual));
                        pos += 2;
                        continue;
                    }else{
                        tokens.add(new Token(TokenType.OPERADOR_LOGICO, String.valueOf(current), lineaActual));
                        pos++;
                    }
                    break;
                case '<':
                    if (obtenerCaracterSiguiente() == '=') { // MODIFICACIÓN: Condición corregida 
                        tokens.add(new Token(TokenType.OPERADOR_COMPARACION, "<=", lineaActual));
                        pos += 2;
                        continue;
                    }else{
                        tokens.add(new Token(TokenType.OPERADOR_COMPARACION, "<", lineaActual));
                        pos++;
                    }
                    break;
                case '>':
                    if (obtenerCaracterSiguiente() == '=') { // MODIFICACIÓN: Condición corregida
                        tokens.add(new Token(TokenType.OPERADOR_COMPARACION, ">=", lineaActual));
                        pos += 2;
                        continue;
                    }else{
                        tokens.add(new Token(TokenType.OPERADOR_COMPARACION, ">", lineaActual));
                        pos++;
                    }
                    break;
                
                // MODIFICACIÓN: Inicio de agregados para && y ||
                case '&':
                    if (obtenerCaracterSiguiente() == '&') {
                        tokens.add(new Token(TokenType.OPERADOR_LOGICO, "&&", lineaActual));
                        pos += 2;
                        continue;
                    } else {
                        tokens.add(new Token(TokenType.ERROR, String.valueOf(current), lineaActual));
                        pos++;
                    }
                    break;
                case '|':
                    if (obtenerCaracterSiguiente() == '|') {
                        tokens.add(new Token(TokenType.OPERADOR_LOGICO, "||", lineaActual));
                        pos += 2;
                        continue;
                    } else {
                        tokens.add(new Token(TokenType.ERROR, String.valueOf(current), lineaActual));
                        pos++;
                    }
                    break;
                // MODIFICACIÓN: Fin de agregados

                default:
                    tokens.add(new Token(TokenType.ERROR, String.valueOf(current), lineaActual));
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
        
        if(palabra.equals("true") || palabra.equals("false")){
            tipo = TokenType.BOOLEANO;
        }
        else if(palabra.equals("null")){
            tipo = TokenType.NULL;
        }
        else if(palabra.equals("class")){
            tipo = TokenType.CLASS;
        }
        else if(modificadoresAcceso.contains(palabra)){
            tipo = TokenType.MODIFICADOR_ACCESO;
        }
        else if(modificadoresComportamiento.contains(palabra)){
            tipo = TokenType.MODIFICADOR_COMPORTAMIENTO;
        }
        else if(otrasPalabrasClave.contains(palabra)){
            tipo = TokenType.PALABRA_CLAVE;
        }
        else if(verSiguienteNoBlanco() == '('){
            // identificador seguido de '(' => nombre de función (declaración o llamada)
            tipo = TokenType.FUNCION;
        }
        else 
            tipo = TokenType.IDENTIFIER;
            
        return new Token(tipo, sb.toString(), lineaActual);
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
        return new Token(TokenType.NUMBER, sb.toString(), lineaActual);
    }

    private char obtenerCaracterSiguiente() {
        if (pos + 1 >= input.length()) return '\0';
        return input.charAt(pos + 1);
    }

    private char verSiguienteNoBlanco() {
        int i = pos;
        while (i < input.length() && Character.isWhitespace(input.charAt(i))) {
            i++;
        }
        return (i < input.length()) ? input.charAt(i) : '\0';
    }

    public void mostrarAnalisis() {
        List<Token> listaObtenida = tokenize();
        for(int i = 0; i < listaObtenida.size(); i++){
            System.out.println(listaObtenida.get(i).toString());
        }
    }
}
