import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, Symbol> table;

    public SymbolTable() {
        this.table = new HashMap<>();
    }

    public void addSymbol(String name, String type) throws SemanticException {
        if (table.containsKey(name)) {
            throw new SemanticException("Error semantico: El simbolo '" + name + "' ya esta declarado.");
        }
        table.put(name, new Symbol(name, type));
    }

    public Symbol getSymbol(String name) throws SemanticException {
        if (!table.containsKey(name)) {
            throw new SemanticException("Error semantico: Variable  '" + name + "' no declarada.");
        }
        return table.get(name);
    }
}
