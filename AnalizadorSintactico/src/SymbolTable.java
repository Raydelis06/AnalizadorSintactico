import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {
    private final Deque<Map<String, Symbol>> pilaAmbitos = new ArrayDeque<>();

    public SymbolTable() {
        entrarAmbito(); 
    }
    /**
     * Abre un nuevo ámbito (llamar al encontrar '{').
     */
    public void entrarAmbito() {
        pilaAmbitos.push(new HashMap<>());
    }
    /**
     * Cierra el ámbito actual y descarta sus símbolos (llamar al encontrar '}').
     * No permite cerrar el ámbito global para evitar estados inválidos.
     */
    public void salirAmbito() {
        if (pilaAmbitos.size() <= 1) {
            // Nunca cerrar el ámbito global; simplemente ignorar la llamada extra.
            return;
        }
        pilaAmbitos.pop();
    }
    /**
     * Devuelve cuántos ámbitos hay activos (1 = solo el global).
     */
    public int profundidadActual() {
        return pilaAmbitos.size();
    }

    public void addSymbol(String name, String type) throws SemanticException {
        Map<String, Symbol> ambito = pilaAmbitos.peek();
        if (ambito.containsKey(name)) {
            throw new SemanticException("Error semantico: El simbolo '" + name + "' ya esta declarado.");
        }
        ambito.put(name, new Symbol(name, type));
    }
    public Symbol getSymbol(String name) throws SemanticException {
        for (Map<String, Symbol> ambito : pilaAmbitos) {
            if (ambito.containsKey(name)) {
                return ambito.get(name);
            }
        }
        throw new SemanticException(
            "Error semántico: Variable '" + name + "' no declarada."
        );
    }

    /**
     * Comprueba si un símbolo existe sin lanzar excepción.
     */
    public boolean existe(String name) {
        for (Map<String, Symbol> ambito : pilaAmbitos) {
            if (ambito.containsKey(name)) return true;
        }
        return false;
    }
    /**
     * Devuelve todos los símbolos visibles en el ámbito actual (para mostrar
     * en la GUI en el tab de "Estructura").
     */
    public List<Symbol> getSimbolosVisibles() {
        Map<String, Symbol> visibles = new HashMap<>();
        // Recorre desde el más global al más local; el local sobreescribe en caso de shadowing
        List<Map<String, Symbol>> lista = new ArrayList<>(pilaAmbitos);
        for (int i = lista.size() - 1; i >= 0; i--) {
            visibles.putAll(lista.get(i));
        }
        return new ArrayList<>(visibles.values());
    }
    /**
     * Reinicia la tabla por completo 
     */
    public void reset() {
        pilaAmbitos.clear();
        entrarAmbito(); // restaurar ámbito global vacío
    }
}
