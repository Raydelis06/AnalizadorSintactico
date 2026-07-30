import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;
    private SemanticAnalyzer analyzer;

    public Parser(List<Token> tokens, SemanticAnalyzer analyzer) {
        this.tokens = tokens;
        this.analyzer = analyzer;
    }

   
    public List<AST.Instruccion> parse() throws SyntaxException, SemanticException {
        List<AST.Instruccion> instrucciones = new ArrayList<>();
        while (!isAtEnd() && peek().getType() != TokenType.EOF) {
            instrucciones.add(sentencia());
        }
        return instrucciones;
    }

    private AST.Instruccion sentencia() throws SyntaxException, SemanticException {
        if (check(TokenType.PALABRA_CLAVE)) {
            String lexema = peek().getLexema().toLowerCase();
            
            if (esTipoDeDato(lexema)) {
                return parseDeclaracion();
            } else if (lexema.equals("if")) {
                return parseIf();
            } else if (lexema.equals("while")) {
                return parseWhile();
            } else if (lexema.equals("do")) {
                return parseDoWhile();
            } else if (lexema.equals("for")) {
                return parseFor();
            } else if (lexema.equals("print")) {
                return parsePrint();
            }
        }
        
        if (check(TokenType.LLAVE_ABIERTA)) {
            return parseBloque();
        }

       
        AST.Instruccion asig = parseAsignacionBasica();
        if (match(TokenType.PUNTO_Y_COMA)) {
            return asig;
        } else {
            throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba ';' al final de la instrucción.");
        }
    }

    private AST.InstruccionBloque parseBloque() throws SyntaxException, SemanticException {
        if (!match(TokenType.LLAVE_ABIERTA)) {
            throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba '{' al inicio del bloque.");
        }

        analyzer.getSymbolTable().entrarAmbito();
        List<AST.Instruccion> cuerpo = new ArrayList<>();

        while (!check(TokenType.LLAVE_CERRADA) && !isAtEnd()) {
            cuerpo.add(sentencia());
        }

        if (!match(TokenType.LLAVE_CERRADA)) {
            throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba '}' para cerrar el bloque.");
        }

        analyzer.getSymbolTable().salirAmbito();
        return new AST.InstruccionBloque(cuerpo);
    }

    private AST.Instruccion parseDeclaracion() throws SyntaxException, SemanticException {
        String tipoDestino = peek().getLexema();
        advance(); 

        if (!check(TokenType.IDENTIFIER)) {
            throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba un identificador después del tipo.");
        }
        
        String nombreVar = peek().getLexema();
        advance();

        AST.Expresion valorInicial = null;
        String tipoExpresion = tipoDestino; 

        
        if (check(TokenType.ASSIGN) && peek().getLexema().equals(":=")) {
            throw new SyntaxException("Línea " + getLineNumber() + ": El operador ':=' no es válido. Usa '=' para asignaciones.");
        }

        // Java puro: solo =
        if (match(TokenType.ASSIGN)) {
            valorInicial = expresion();
            tipoExpresion = valorInicial.tipoDatoRecuperado;
            analyzer.checkAssignment(tipoDestino, tipoExpresion);
        }

        if (!match(TokenType.PUNTO_Y_COMA)) {
            throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba ';' al final de la declaración.");
        }

        analyzer.declareVariable(nombreVar, tipoDestino);
        return new AST.InstruccionDeclaracion(tipoDestino, nombreVar, valorInicial);
    }

    private AST.InstruccionAsignacion parseAsignacionBasica() throws SyntaxException, SemanticException {
        if (!check(TokenType.IDENTIFIER)) {
            throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba IDENTIFIER.");
        }
        
        String nombreVar = peek().getLexema();
        advance();

        
        if (check(TokenType.ASSIGN) && peek().getLexema().equals(":=")) {
            throw new SyntaxException("Línea " + getLineNumber() + ": El operador ':=' no es válido. Usa '=' o compuestos (+=, -=, etc).");
        }

        
        if (!check(TokenType.ASSIGN) && !check(TokenType.OPERADOR_ARITMETICO)) {
            throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba un operador de asignación (=, +=, -=, *=, /=).");
        }
        
        String operador = peek().getLexema();
        advance();

        AST.Expresion valor = expresion();
        String tipoExistente = analyzer.checkVariable(nombreVar); 
        
        if (operador.equals("=")) {
            analyzer.checkAssignment(tipoExistente, valor.tipoDatoRecuperado);
        } else {
            
            analyzer.checkOperation(tipoExistente, valor.tipoDatoRecuperado, operador);
        }

        return new AST.InstruccionAsignacion(nombreVar, operador, valor);
    }

   

    private AST.InstruccionIf parseIf() throws SyntaxException, SemanticException {
        advance(); 
        
        if (!match(TokenType.PARENTESIS_ABIERTO)) throw new SyntaxException("Se esperaba '(' después de 'if'.");
        AST.Expresion condicion = expresion();
        if (!"boolean".equals(condicion.tipoDatoRecuperado)) {
            throw new SemanticException("Línea " + getLineNumber() + ": La condición del 'if' debe ser boolean.");
        }
        if (!match(TokenType.PARENTESIS_CERRADO)) throw new SyntaxException("Se esperaba ')' después de la condición.");
        
        AST.Instruccion ramaVerdadera = check(TokenType.LLAVE_ABIERTA) ? parseBloque() : sentencia();
        AST.Instruccion ramaFalsa = null;
        
        if (check(TokenType.PALABRA_CLAVE) && peek().getLexema().equalsIgnoreCase("else")) {
            advance();
            ramaFalsa = check(TokenType.LLAVE_ABIERTA) ? parseBloque() : sentencia();
        }
        
        return new AST.InstruccionIf(condicion, ramaVerdadera, ramaFalsa);
    }

    private AST.InstruccionWhile parseWhile() throws SyntaxException, SemanticException {
        advance(); 
        
        if (!match(TokenType.PARENTESIS_ABIERTO)) throw new SyntaxException("Se esperaba '(' después de 'while'.");
        AST.Expresion condicion = expresion();
        if (!"boolean".equals(condicion.tipoDatoRecuperado)) {
            throw new SemanticException("Línea " + getLineNumber() + ": La condición del 'while' debe ser boolean.");
        }
        if (!match(TokenType.PARENTESIS_CERRADO)) throw new SyntaxException("Se esperaba ')' después de la condición.");
        
        AST.Instruccion cuerpo = check(TokenType.LLAVE_ABIERTA) ? parseBloque() : sentencia();
        return new AST.InstruccionWhile(condicion, cuerpo);
    }

    private AST.InstruccionDoWhile parseDoWhile() throws SyntaxException, SemanticException {
        advance(); 
        
        AST.Instruccion cuerpo = check(TokenType.LLAVE_ABIERTA) ? parseBloque() : sentencia();
        
        if (!(check(TokenType.PALABRA_CLAVE) && peek().getLexema().equalsIgnoreCase("while"))) {
            throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba 'while' al final del bloque 'do'.");
        }
        advance(); 
        
        if (!match(TokenType.PARENTESIS_ABIERTO)) throw new SyntaxException("Se esperaba '('.");
        AST.Expresion condicion = expresion();
        if (!"boolean".equals(condicion.tipoDatoRecuperado)) {
            throw new SemanticException("Línea " + getLineNumber() + ": La condición del 'do-while' debe ser boolean.");
        }
        if (!match(TokenType.PARENTESIS_CERRADO)) throw new SyntaxException("Se esperaba ')'.");
        if (!match(TokenType.PUNTO_Y_COMA)) throw new SyntaxException("Se esperaba ';' al final de 'do-while'.");
        
        return new AST.InstruccionDoWhile(cuerpo, condicion);
    }

    private AST.InstruccionFor parseFor() throws SyntaxException, SemanticException {
        advance(); 
        if (!match(TokenType.PARENTESIS_ABIERTO)) throw new SyntaxException("Se esperaba '(' después de 'for'.");
        
       
        analyzer.getSymbolTable().entrarAmbito();
        
        AST.Instruccion inicio = null;
        if (!match(TokenType.PUNTO_Y_COMA)) { // Si no está vacío
            if (check(TokenType.PALABRA_CLAVE) && esTipoDeDato(peek().getLexema())) {
                inicio = parseDeclaracion(); 
            } else {
                inicio = parseAsignacionBasica();
                match(TokenType.PUNTO_Y_COMA);
            }
        }
        
        AST.Expresion condicion = null;
        if (!check(TokenType.PUNTO_Y_COMA)) {
            condicion = expresion();
            if (!"boolean".equals(condicion.tipoDatoRecuperado)) {
                throw new SemanticException("Línea " + getLineNumber() + ": La condición del 'for' debe ser boolean.");
            }
        }
        match(TokenType.PUNTO_Y_COMA);
        
        AST.Instruccion incremento = null;
        if (!check(TokenType.PARENTESIS_CERRADO)) {
            incremento = parseAsignacionBasica(); // Sin ; al final
        }
        if (!match(TokenType.PARENTESIS_CERRADO)) throw new SyntaxException("Se esperaba ')' al cerrar for.");
        
       
        AST.Instruccion cuerpo;
        if (check(TokenType.LLAVE_ABIERTA)) {
            advance();
            List<AST.Instruccion> sentenciasCuerpo = new ArrayList<>();
            while (!check(TokenType.LLAVE_CERRADA) && !isAtEnd()) {
                sentenciasCuerpo.add(sentencia());
            }
            match(TokenType.LLAVE_CERRADA);
            cuerpo = new AST.InstruccionBloque(sentenciasCuerpo);
        } else {
            cuerpo = sentencia();
        }
        
        
        analyzer.getSymbolTable().salirAmbito();
        
        return new AST.InstruccionFor(inicio, condicion, incremento, cuerpo);
    }

    private AST.InstruccionPrint parsePrint() throws SyntaxException, SemanticException {
        advance(); 
        AST.Expresion expr = expresion();
        if (!match(TokenType.PUNTO_Y_COMA)) {
            throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba ';' después de print.");
        }
        return new AST.InstruccionPrint(expr);
    }

    

    private AST.Expresion expresion() throws SyntaxException, SemanticException {
        return expresionLogica();
    }

    private AST.Expresion expresionLogica() throws SyntaxException, SemanticException {
        AST.Expresion izq = expresionRelacional();
        while (check(TokenType.OPERADOR_LOGICO)) {
            String op = peek().getLexema();
            advance();
            AST.Expresion der = expresionRelacional();
            
            AST.ExpresionBinaria bin = new AST.ExpresionBinaria(izq, op, der);
            bin.tipoDatoRecuperado = analyzer.checkOperation(izq.tipoDatoRecuperado, der.tipoDatoRecuperado, op);
            izq = bin;
        }
        return izq;
    }

    private AST.Expresion expresionRelacional() throws SyntaxException, SemanticException {
        AST.Expresion izq = termino();
        while (check(TokenType.OPERADOR_COMPARACION) || check(TokenType.OPERADOR_RELACIONAL)) {
            String op = peek().getLexema();
            advance();
            AST.Expresion der = termino();
            
            AST.ExpresionBinaria bin = new AST.ExpresionBinaria(izq, op, der);
            bin.tipoDatoRecuperado = analyzer.checkOperation(izq.tipoDatoRecuperado, der.tipoDatoRecuperado, op);
            izq = bin;
        }
        return izq;
    }

    private AST.Expresion termino() throws SyntaxException, SemanticException {
        AST.Expresion izq = factor();
        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            String op = peek().getLexema();
            advance();
            AST.Expresion der = factor();
            
            AST.ExpresionBinaria bin = new AST.ExpresionBinaria(izq, op, der);
            bin.tipoDatoRecuperado = analyzer.checkOperation(izq.tipoDatoRecuperado, der.tipoDatoRecuperado, op);
            izq = bin;
        }
        return izq;
    }

    private AST.Expresion factor() throws SyntaxException, SemanticException {
        AST.Expresion izq = primario();
        while (check(TokenType.MULT) || check(TokenType.DIV)) {
            String op = peek().getLexema();
            advance();
            AST.Expresion der = primario();
            
            AST.ExpresionBinaria bin = new AST.ExpresionBinaria(izq, op, der);
            bin.tipoDatoRecuperado = analyzer.checkOperation(izq.tipoDatoRecuperado, der.tipoDatoRecuperado, op);
            izq = bin;
        }
        return izq;
    }

    private AST.Expresion primario() throws SyntaxException, SemanticException {
        if (check(TokenType.NUMBER)) {
            String lexema = peek().getLexema();
            double val = Double.parseDouble(lexema);
            advance();
            AST.ExpresionNumero expr = new AST.ExpresionNumero(val);
            
            // Diferenciar si es un int o un double verificando el punto decimal - JuanC
            if (lexema.contains(".")) {
                expr.tipoDatoRecuperado = "double"; 
            } else {
                expr.tipoDatoRecuperado = "int";
            }
            
            return expr;
        } 
        else if (check(TokenType.IDENTIFIER)) {
            String nombreVar = peek().getLexema();
            advance();
            AST.ExpresionIdentificador expr = new AST.ExpresionIdentificador(nombreVar);
            expr.tipoDatoRecuperado = analyzer.checkVariable(nombreVar);
            return expr;
        } 
        else if (check(TokenType.BOOLEANO)) {
            boolean val = peek().getLexema().equalsIgnoreCase("true");
            advance();
            AST.ExpresionBooleana expr = new AST.ExpresionBooleana(val);
            expr.tipoDatoRecuperado = "boolean";
            return expr;
        } 
        else if (check(TokenType.STRING)) {
            String txt = peek().getLexema();
            advance();
            AST.ExpresionCadena expr = new AST.ExpresionCadena(txt);
            expr.tipoDatoRecuperado = "String";
            return expr;
        } 
        else if (match(TokenType.PARENTESIS_ABIERTO)) {
            AST.Expresion expr = expresion(); 
            if (!match(TokenType.PARENTESIS_CERRADO)) throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba ')'.");
            return expr;
        } 
        else {
            throw new SyntaxException("Línea " + getLineNumber() + ": Se esperaba un número, variable, booleano, string o '('.");
        }
    }

    // --- UTILIDADES ---

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

    private Token peek() {
        if (current < tokens.size()) return tokens.get(current);
        return tokens.get(tokens.size() - 1);
    }

    private boolean isAtEnd() {
        return current >= tokens.size() || peek().getType() == TokenType.EOF;
    }

    private int getLineNumber() {
        if (current < tokens.size()) return tokens.get(current).getLinea();
        if (!tokens.isEmpty()) return tokens.get(tokens.size() - 1).getLinea();
        return 1;
    }
}
