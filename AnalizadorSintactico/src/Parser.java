import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;
    private SemanticAnalyzer analyzer;

    public Parser(List<Token> tokens, SemanticAnalyzer analyzer) {
        this.tokens = tokens;
        this.analyzer = analyzer;
    }

    public void parse() throws SyntaxException, SemanticException {
        while (!isAtEnd() && peek().getType() != TokenType.EOF) {
            sentencia();
        }
    }

    // MODIFICACIÓN: sentencia() actualizada para soportar if, switch, y saltos.
    public void sentencia() throws SyntaxException, SemanticException {
        if (check(TokenType.PALABRA_CLAVE)) {
            String lexema = peek().getLexema().toLowerCase();
            
            if (esTipoDeDato(lexema)) {
                parseDeclaracion();
                return;
            } 
            else if (lexema.equals("if")) {
                parseIf();
                return;
            } 
            else if (lexema.equals("switch")) {
                parseSwitch();
                return;
            }
            else if (lexema.equals("break") || lexema.equals("continue") || lexema.equals("return")) {
                advance(); 
                if (match(TokenType.PUNTO_Y_COMA)) {
                    return;
                } else {
                    throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba ';' después de '" + lexema + "'.");
                }
            }
        } 
        
        if (check(TokenType.LLAVE_ABIERTA)) {
            parseBloque();
        } 
        else {
            asignacion();
            if (match(TokenType.PUNTO_Y_COMA)) {
                // match ya hace advance()
            } else {
                throw new SyntaxException("Linea " + getLineNumber() + ": Se esperaba ';' al final de la instruccion.");
            }
        }
    }

    public void parseBloque() throws SyntaxException, SemanticException {
        if (check(TokenType.LLAVE_ABIERTA)) {
            advance(); 
            analyzer.getSymbolTable().entrarAmbito(); 
            
            while (!check(TokenType.LLAVE_CERRADA) && !isAtEnd()) {
                sentencia(); 
            }
            
            if (check(TokenType.LLAVE_CERRADA)) {
                advance(); 
                analyzer.getSymbolTable().salirAmbito(); 
            } else {
                throw new SyntaxException("Linea " + getLineNumber() + ": Se esperaba '}' para cerrar el bloque.");
            }
        } else {
            sentencia();
        }
    }

    public void parseDeclaracion() throws SyntaxException, SemanticException {
        String tipoDestino = peek().getLexema();
        advance(); 

        if (!check(TokenType.IDENTIFIER)) {
            throw new SyntaxException("Linea " + getLineNumber() + ": Se esperaba un identificador despues del tipo de dato.");
        }
        
        String nombreVar = peek().getLexema();
        advance();
        String tipoExpresion = tipoDestino; 

        if (check(TokenType.ASSIGN)) {
            advance(); 
            tipoExpresion = expresion();
            analyzer.checkAssignment(tipoDestino, tipoExpresion);
        }

        if (check(TokenType.PUNTO_Y_COMA)) {
            advance(); 
        } else {
            throw new SyntaxException("Linea " + getLineNumber() + ": Se esperaba ';' al final de la declaracion.");
        }

        analyzer.declareVariable(nombreVar, tipoDestino);
    }

    private void asignacion() throws SyntaxException, SemanticException {
        if (!check(TokenType.IDENTIFIER)) {
            throw new SyntaxException("Linea " + getLineNumber() + ": Se esperaba IDENTIFIER al inicio");
        }
        
        String varName = peek().getLexema();
        advance();

        if (!check(TokenType.ASSIGN)) {
            throw new SyntaxException("Linea " + getLineNumber() + ": Se esperaba ':=' o '='");
        }
        advance();

        String tipoExpresion = expresion();
        String tipoExistente = analyzer.checkVariable(varName);
        analyzer.checkAssignment(tipoExistente, tipoExpresion);
    }

    // MODIFICACIÓN: Inicio de agregados para 'if' y 'switch'
    private void parseIf() throws SyntaxException, SemanticException {
        advance(); 
        
        if (!match(TokenType.PARENTESIS_ABIERTO)) {
            throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba '(' después de 'if'.");
        }
        
        String tipoCondicion = expresion();
        
        if (!"boolean".equals(tipoCondicion)) {
            throw new SemanticException("Error semántico en Línea " + getLineNumber() 
                + ": La condición de la sentencia 'if' debe ser de tipo 'boolean'. Se encontró: '" + tipoCondicion + "'.");
        }
        
        if (!match(TokenType.PARENTESIS_CERRADO)) {
            throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba ')' después de la condición del 'if'.");
        }
        
        parseBloque();
        
        if (check(TokenType.PALABRA_CLAVE) && peek().getLexema().equalsIgnoreCase("else")) {
            advance(); 
            parseBloque(); 
        }
    }

    private void parseSwitch() throws SyntaxException, SemanticException {
        advance(); 
        
        if (!match(TokenType.PARENTESIS_ABIERTO)) {
            throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba '(' después de 'switch'.");
        }
        
        String tipoSwitch = expresion();
        
        if (!match(TokenType.PARENTESIS_CERRADO)) {
            throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba ')' después de la expresión del 'switch'.");
        }
        
        if (!match(TokenType.LLAVE_ABIERTA)) {
            throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba '{' para iniciar el cuerpo del 'switch'.");
        }
        
        analyzer.getSymbolTable().entrarAmbito();
        
        while (!check(TokenType.LLAVE_CERRADA) && !isAtEnd()) {
            if (check(TokenType.PALABRA_CLAVE) && peek().getLexema().equalsIgnoreCase("case")) {
                advance(); 
                
                String tipoCase = expresion();
                if (!tipoSwitch.equals(tipoCase)) {
                    throw new SemanticException("Error semántico en Línea " + getLineNumber() 
                        + ": El valor del 'case' (" + tipoCase + ") no es compatible con el tipo del 'switch' (" + tipoSwitch + ").");
                }
                
                if (!match(TokenType.DOS_PUNTOS)) {
                    throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba ':' después del valor del 'case'.");
                }
                
                while (!check(TokenType.LLAVE_CERRADA) && !isAtEnd() && 
                       !(check(TokenType.PALABRA_CLAVE) && (peek().getLexema().equalsIgnoreCase("case") || peek().getLexema().equalsIgnoreCase("default")))) {
                    sentencia();
                }
            } 
            else if (check(TokenType.PALABRA_CLAVE) && peek().getLexema().equalsIgnoreCase("default")) {
                advance(); 
                
                if (!match(TokenType.DOS_PUNTOS)) {
                    throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba ':' después de 'default'.");
                }
                
                while (!check(TokenType.LLAVE_CERRADA) && !isAtEnd() && 
                       !(check(TokenType.PALABRA_CLAVE) && peek().getLexema().equalsIgnoreCase("case"))) {
                    sentencia();
                }
            } 
            else {
                throw new SyntaxException("Línea " + getLineNumber() + ": Instrucción inválida dentro de un 'switch'. Se esperaba 'case' o 'default'.");
            }
        }
        
        if (!match(TokenType.LLAVE_CERRADA)) {
            throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba '}' para cerrar el cuerpo del 'switch'.");
        }
        
        analyzer.getSymbolTable().salirAmbito();
    }
    // MODIFICACIÓN: Fin de agregados para 'if' y 'switch'

    // MODIFICACIÓN: Inicio de cascada de precedencia matemática y relacional (reemplaza a los viejos métodos)
    private String expresion() throws SyntaxException, SemanticException {
        return expresionLogica();
    }

    private String expresionLogica() throws SyntaxException, SemanticException {
        String tipoIzquierdo = expresionRelacional();
        
        while (match(TokenType.OPERADOR_LOGICO)) {
            Token operador = previous();
            String tipoDerecho = expresionRelacional();
            tipoIzquierdo = analyzer.checkOperation(tipoIzquierdo, tipoDerecho, operador.getLexema());
        }
        return tipoIzquierdo;
    }

    private String expresionRelacional() throws SyntaxException, SemanticException {
        String tipoIzquierdo = termino();
        
        while (match(TokenType.OPERADOR_COMPARACION, TokenType.OPERADOR_RELACIONAL)) {
            Token operador = previous();
            String tipoDerecho = termino();
            tipoIzquierdo = analyzer.checkOperation(tipoIzquierdo, tipoDerecho, operador.getLexema());
        }
        return tipoIzquierdo;
    }

    private String termino() throws SyntaxException, SemanticException {
        String tipoIzquierdo = factor();
        
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operador = previous();
            String tipoDerecho = factor();
            tipoIzquierdo = analyzer.checkOperation(tipoIzquierdo, tipoDerecho, operador.getLexema());
        }
        return tipoIzquierdo;
    }

    private String factor() throws SyntaxException, SemanticException {
        String tipoIzquierdo = primario();
        
        while (match(TokenType.MULT, TokenType.DIV)) {
            Token operador = previous();
            String tipoDerecho = primario();
            tipoIzquierdo = analyzer.checkOperation(tipoIzquierdo, tipoDerecho, operador.getLexema());
        }
        return tipoIzquierdo;
    }

    private String primario() throws SyntaxException, SemanticException {
        if (check(TokenType.NUMBER)) {
            advance();
            return "int"; 
        } 
        else if (check(TokenType.IDENTIFIER)) {
            String nombreVar = peek().getLexema();
            advance();
            return analyzer.checkVariable(nombreVar);
        } 
        else if (match(TokenType.BOOLEANO)) {
            return "boolean";
        } 
        else if (match(TokenType.STRING)) {
            return "String";
        } 
        else if (match(TokenType.PARENTESIS_ABIERTO)) {
            String tipo = expresion(); 
            if (match(TokenType.PARENTESIS_CERRADO)) {
                return tipo;
            } else {
                throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba ')' para cerrar la expresión.");
            }
        } 
        else {
            throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba un valor constante, variable o '(' fijo.");
        }
    }
    // MODIFICACIÓN: Fin de cascada de precedencia

    private boolean esTipoDeDato(String lexema) {
        String l = lexema.toLowerCase();
        return l.equals("int") || l.equals("string") || l.equals("boolean") || 
               l.equals("double") || l.equals("char") || l.equals("byte") || l.equals("long");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return type == TokenType.EOF;
        return peek().getType() == type;
    }

    private void advance() {
        if (!isAtEnd()) current++;
    }
    
    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token peek() {
        if (current < tokens.size()) {
            return tokens.get(current);
        }
        return tokens.get(tokens.size() - 1);
    }

    private boolean isAtEnd() {
        return current >= tokens.size() || peek().getType() == TokenType.EOF;
    }

    private int getLineNumber() {
        if (current < tokens.size()) {
            return tokens.get(current).getLinea();
        }
        if (!tokens.isEmpty()) {
            return tokens.get(tokens.size() - 1).getLinea();
        }
        return 1;
    }
}
