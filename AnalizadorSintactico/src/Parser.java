import java.util.List;

public class Parser {

    private final List<Token> tokens;

    private int current = 0;

    public Parser(List<Token> tokens) {

        this.tokens = tokens;

    }

    
    public void parse() throws SyntaxException {
        
        asignacion();
        
       
        if (!isAtEnd() && peek().getType() != TokenType.EOF) {

            throw new SyntaxException("Linea " + 1 + ": Elementos inesperados al final de la instruccion.");

        }
    }

   
    private void asignacion() throws SyntaxException {
       
        if (!check(TokenType.IDENTIFIER)) {

            throw new SyntaxException("Linea " + 1 + ": Se esperaba IDENTIFIER al inicio, pero se encontro " + peek().getType());

        }
        advance(); 

        
        if (!check(TokenType.ASSIGN)) {

            throw new SyntaxException("Linea " + 1 + ": Se esperaba ':=' pero despues del IDENTIFIER");
        }
        advance(); 

        
        expresion();
    }

    
    private void expresion() throws SyntaxException {
        
        termino();

       
        while (match(TokenType.PLUS, TokenType.MINUS, TokenType.MULT, TokenType.DIV)) {
            termino();
        }
    }

   
    private void termino() throws SyntaxException {

        if (check(TokenType.NUMBER) || check(TokenType.IDENTIFIER)) {
            advance(); 
        } else {

            
            throw new SyntaxException("Linea " + 1 + ": Se esperaba NUMBER o IDENTIFIER, pero se encontro " + peek().getType());

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

    
    private Token peek() {

        return tokens.get(current);

    }

    
    private boolean isAtEnd() {

        return current >= tokens.size() || peek().getType() == TokenType.EOF;

    }
}