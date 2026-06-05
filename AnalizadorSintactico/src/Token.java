public class Token {
    private TokenType Tipo;
    private String Lexema;
    private int linea;

    public Token(TokenType tipo, String lexema, int linea) {
        this.Tipo = tipo;
        this.Lexema = lexema;
        this.linea = linea;
    }

    public TokenType getType() {
        return Tipo;
    }

    public String getLexema() {
        return Lexema;
    }

    public int getLinea() {
        return linea;
    }

    @Override
    public String toString() {
        return Tipo.getDescripcion() + " -> " + Lexema + " [Linea " + linea + "]";
    }
}
