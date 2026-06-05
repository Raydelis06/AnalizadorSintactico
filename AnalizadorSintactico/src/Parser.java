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
        asignacion();
        if (!isAtEnd() && peek().getType() != TokenType.EOF) {
            throw new SyntaxException("Linea " + getLineNumber() + ": Elementos inesperados al final de la instruccion.");
        }
    }

    private void asignacion() throws SyntaxException, SemanticException {
        if (!check(TokenType.IDENTIFIER)) {
            throw new SyntaxException("Linea " + getLineNumber() + ": Se esperaba IDENTIFIER al inicio");
        }
        
        String varName = peek().getLexema();
        advance();

        if (!check(TokenType.ASSIGN)) {
            throw new SyntaxException("Linea " + getLineNumber() + ": Se esperaba ':='");
        }
        advance();

        String tipoExpresion = expresion();
        
        try {
            String tipoExistente = analyzer.checkVariable(varName);
            analyzer.checkAssignment(tipoExistente, tipoExpresion);
        } catch (SemanticException e) {
            if (e.getMessage().contains("no declarada")) {
                analyzer.declareVariable(varName, tipoExpresion);
            } else {
                throw e;
            }
        }
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
