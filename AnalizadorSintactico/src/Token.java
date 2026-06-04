public class Token {
    private TokenType Tipo;
    private String Lexema;

    public Token(TokenType tipo, String lexema) {
        this.Tipo = tipo;
        Lexema = lexema;
    }

    public TokenType getType() {
        return Tipo;
    }

    public String getLexema() {
        return Lexema;
    }

    @Override
    public String toString() {
        return Tipo.getDescripcion() + " -> " + Lexema;
    }
}
