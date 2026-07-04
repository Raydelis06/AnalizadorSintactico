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
    public String checkOperation(String tipoIzq, String tipoDer, TokenType operador)
            throws SemanticException {
        return checkOperation(tipoIzq, tipoDer, tokenTypeToOperador(operador));
    }
    
    /**
     * Verifica que la operación entre dos tipos sea válida y retorna el tipo
     * resultante.
     */
    public String checkOperation(String typeLeft, String typeRight, String operator)
            throws SemanticException {
 
        switch (operator) {
 
            // ── Operadores aritméticos ─────────────────────────────────
            case "+":
                // Concatenación: si alguno es String → resultado String
                if (esString(typeLeft) || esString(typeRight)) {
                    if (esString(typeLeft) || esNumerico(typeLeft) || esString(typeRight) || esNumerico(typeRight)) {
                        return "String";
                    }
                    throw errorOperacion(typeLeft, typeRight, operator);
                }
                // Aritmética numérica
                return resultadoAritmetico(typeLeft, typeRight, operator);
 
            case "-":
            case "*":
            case "/":
                return resultadoAritmetico(typeLeft, typeRight, operator);
 
            // ── Operadores relacionales ────────────────────────────────
            case "<":
            case ">":
            case "<=":
            case ">=":
                if (esNumerico(typeLeft) && esNumerico(typeRight)) {
                    return "boolean";
                }
                throw new SemanticException(
                    "Error semántico: El operador '" + operator
                    + "' solo se puede usar entre tipos numéricos, no entre '"
                    + typeLeft + "' y '" + typeRight + "'."
                );
 
            // ── Operadores de comparación ──────────────────────────────
            case "==":
            case "!=":
                return checkComparacion(typeLeft, typeRight, operator);
 
            // ── Operadores lógicos ─────────────────────────────────────
            case "&&":
            case "||":
                if ("boolean".equals(typeLeft) && "boolean".equals(typeRight)) {
                    return "boolean";
                }
                throw new SemanticException(
                    "Error semántico: El operador '" + operator
                    + "' solo se puede usar entre booleanos, no entre '"
                    + typeLeft + "' y '" + typeRight + "'."
                );
 
            // ── Operadores de asignación compuesta (+=, -=, *=, /=) ───
            case "+=":
                // Si el lado izquierdo es String y el operador es +=, es concatenación
                if (esString(typeLeft)) return "String";
                return resultadoAritmetico(typeLeft, typeRight, "+");
 
            case "-=":
            case "*=":
            case "/=":
                return resultadoAritmetico(typeLeft, typeRight, operator.substring(0, 1));
 
            default:
                throw new SemanticException(
                    "Error semántico: Operador desconocido '" + operator + "'."
                );
        }
    }
    public void checkAssignment(String tipoDestino, String tipoExpresion)
            throws SemanticException {
 
        // Mismo tipo → siempre OK
        if (tipoDestino.equals(tipoExpresion)) return;
 
        // int → double → OK (promoción implícita)
        if ("double".equals(tipoDestino) && "int".equals(tipoExpresion)) return;
 
        // null → String → OK
        if ("String".equals(tipoDestino) && "null".equals(tipoExpresion)) return;
 
        // double → int → ERROR (pérdida de precisión)
        if ("int".equals(tipoDestino) && "double".equals(tipoExpresion)) {
            throw new SemanticException(
                "Error semántico: No se puede asignar 'double' a 'int' sin conversión explícita (cast). "
                + "Usa (int) para forzar la conversión."
            );
        }
 
        // null → tipo primitivo → ERROR
        if ("null".equals(tipoExpresion) && !esReferencia(tipoDestino)) {
            throw new SemanticException(
                "Error semántico: No se puede asignar 'null' a una variable de tipo '"
                + tipoDestino + "'. Solo los tipos de referencia (String) aceptan null."
            );
        }
 
        // Mezcla de categorías completamente distintas → ERROR
        throw new SemanticException(
            "Error semántico: Tipos incompatibles en asignación. "
            + "No se puede asignar '" + tipoExpresion
            + "' a una variable de tipo '" + tipoDestino + "'."
        );
    }
    /** Retorna el tipo resultante de una operación aritmética numérica pura. */
    private String resultadoAritmetico(String typeLeft, String typeRight, String op)
            throws SemanticException {
 
        if (!esNumerico(typeLeft) || !esNumerico(typeRight)) {
            throw errorOperacion(typeLeft, typeRight, op);
        }
        // Cualquiera de los dos sea double → resultado double
        if ("double".equals(typeLeft) || "double".equals(typeRight)) return "double";
        return "int";
    }
    /** Retorna boolean si la comparación == / != tiene sentido entre los tipos dados. */
    private String checkComparacion(String typeLeft, String typeRight, String op)
            throws SemanticException {
 
        // Mismos tipos
        if (typeLeft.equals(typeRight)) return "boolean";
        // int y double
        if (esNumerico(typeLeft) && esNumerico(typeRight)) return "boolean";
        // null y String
        if (("null".equals(typeLeft) && esReferencia(typeRight))
                || (esReferencia(typeLeft) && "null".equals(typeRight))) {
            return "boolean";
        }
 
        throw new SemanticException(
            "Error semántico: No se puede comparar con '" + op
            + "' tipos incompatibles: '" + typeLeft + "' y '" + typeRight + "'."
        );
    }
    private boolean esNumerico(String tipo) {
        return "int".equals(tipo) || "double".equals(tipo);
    }
    private boolean esString(String tipo) {
        return "String".equals(tipo);
    }
    private boolean esReferencia(String tipo) {
        return "String".equals(tipo);
    }
    private SemanticException errorOperacion(String tipoIzq, String tipoDer, String op) {
        return new SemanticException(
            "Error semántico: Operación '" + op + "' no permitida entre '"
            + tipoIzq + "' y '" + tipoDer + "'."
        );
    }
    /** Convierte TokenType de operador aritmético a su símbolo String. */
    private String tokenTypeToOperador(TokenType tt) throws SemanticException {
        switch (tt) {
            case PLUS:  return "+";
            case MINUS: return "-";
            case MULT:  return "*";
            case DIV:   return "/";
            case OPERADOR_ARITMETICO: return "+"; // fallback genérico
            case OPERADOR_RELACIONAL: return "<";
            case OPERADOR_COMPARACION: return "==";
            case OPERADOR_LOGICO: return "&&";
            default:
                throw new SemanticException(
                    "Error semántico: TokenType '" + tt + "' no corresponde a un operador conocido."
                );
        }
    }
}
