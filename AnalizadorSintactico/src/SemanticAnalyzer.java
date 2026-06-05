public class SemanticAnalyzer {
    private SymbolTable symbolTable;

    public SemanticAnalyzer() {
        this.symbolTable = new SymbolTable();
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public void declareVariable(String name, String type) throws SemanticException {
        symbolTable.addSymbol(name, type);
    }

    public String checkVariable(String name) throws SemanticException {
        Symbol sym = symbolTable.getSymbol(name);
        return sym.getType();
    }

    public String checkOperation(String typeLeft, String typeRight, TokenType operator) throws SemanticException {
        if (typeLeft.equals("int") && typeRight.equals("int")) {
            return "int";
        }
        throw new SemanticException("Error semantico: Tipos incompatibles (" + typeLeft + " y " + typeRight + ").");
    }

    public void checkAssignment(String targetType, String expressionType) throws SemanticException {
        if (!targetType.equals(expressionType)) {
            throw new SemanticException("Error semantico: No se puede asignar '" + expressionType + "' a variable de tipo '" + targetType + "'.");
        }
    }
}
