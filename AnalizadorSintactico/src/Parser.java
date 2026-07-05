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

    
    public void sentencia() throws SyntaxException, SemanticException {
    
        if (check(TokenType.PALABRA_CLAVE) && esTipoDeDato(peek().getLexema())) {
            parseDeclaracion();
        } 
        
        else if (check(TokenType.LLAVE_ABIERTA)) {
            parseBloque();
        } 
        
        else {
            asignacion();
          

            if (check(TokenType.PUNTO_Y_COMA)) {
                advance();
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

    private String expresion() throws SyntaxException, SemanticException {
        String tipoIzquierdo = termino();
        
        while (match(TokenType.PLUS, TokenType.MINUS, TokenType.MULT, TokenType.DIV)) {
            TokenType operador = previous().getType();
            String tipoDerecho = termino();
            tipoIzquierdo = analyzer.checkOperation(tipoIzquierdo, tipoDerecho, operador);
        }
        
        return tipoIzquierdo;
    }

    private String termino() throws SyntaxException, SemanticException {
        if (check(TokenType.NUMBER)) {
            advance();
            return "int";
        } else if (check(TokenType.IDENTIFIER)) {
            String nombreVar = peek().getLexema();
            advance();
            return analyzer.checkVariable(nombreVar);
        } else {
            throw new SyntaxException("Linea " + getLineNumber() + ": Se esperaba NUMBER o IDENTIFIER");
        }
    }

    
    
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