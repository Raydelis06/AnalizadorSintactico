import java.util.ArrayList;
import java.util.List;

// Archivo y clase creados desde cero para manejar la interpretación - JuanC
public class Evaluador {

    // Campos internos núcleo de la ejecución - JuanC
    private SymbolTable memoria; 
    private List<String> salidaConsola; 
    private List<String> erroresEjecucion; 
    private boolean detenerEjecucion; // Bandera para frenar recursividad y bucles limpiamente en error - JuanC

    public Evaluador() {
        this.salidaConsola = new ArrayList<>();
        this.erroresEjecucion = new ArrayList<>();
    }

    public List<String> getSalidaConsola() {
        return salidaConsola;
    }

    public List<String> getErroresEjecucion() {
        return erroresEjecucion;
    }

    // Método principal para ejecutar el AST completo - JuanC
    public void ejecutar(List<AST.Instruccion> programa) {
        // Limpia la salida y errores al inicio de cada ejecución - JuanC
        this.salidaConsola.clear();
        this.erroresEjecucion.clear();
        this.memoria = new SymbolTable(); // Se instancia separada de la del Parser - JuanC
        this.detenerEjecucion = false;

        for (AST.Instruccion inst : programa) {
            if (detenerEjecucion) break;
            ejecutarInstruccion(inst);
        }
    }

    // Manejador centralizado para atrapar errores y detener ejecución limpiamente sin crashes - JuanC
    private void reportarError(String mensaje) {
        erroresEjecucion.add(mensaje);
        detenerEjecucion = true;
    }

    // Despachador de instrucciones para cada subclase de Instruccion - JuanC
    private void ejecutarInstruccion(AST.Instruccion inst) {
        if (detenerEjecucion || inst == null) return;

        if (inst instanceof AST.InstruccionDeclaracion) {
            AST.InstruccionDeclaracion decl = (AST.InstruccionDeclaracion) inst;
            Object valorInicialEval = null;
            
            if (decl.valorInicial != null) {
                valorInicialEval = evaluarExpresion(decl.valorInicial);
            }
            
            try {
                memoria.addSymbol(decl.nombre, decl.tipo);
                memoria.setValor(decl.nombre, valorInicialEval);
            } catch (SemanticException e) {
                reportarError(e.getMessage());
            }
        } 
        else if (inst instanceof AST.InstruccionAsignacion) {
            AST.InstruccionAsignacion asig = (AST.InstruccionAsignacion) inst;
            Object valorEvaluado = evaluarExpresion(asig.valor);
            if (detenerEjecucion) return;

            try {
                if (asig.operador.equals("=")) {
                    memoria.setValor(asig.nombre, valorEvaluado);
                } else {
                    // Operadores compuestos: recuperar valor actual antes de operar - JuanC
                    Symbol sym = memoria.getSymbol(asig.nombre);
                    Object valorActual = sym.getValor();
                    if (valorActual == null) {
                        reportarError("Variable no inicializada: " + asig.nombre);
                        return;
                    }
                    String operadorBase = asig.operador.substring(0, asig.operador.length() - 1); // += -> +
                    Object nuevoValor = calcularOperacion(valorActual, operadorBase, valorEvaluado);
                    memoria.setValor(asig.nombre, nuevoValor);
                }
            } catch (SemanticException e) {
                reportarError(e.getMessage());
            }
        } 
        else if (inst instanceof AST.InstruccionBloque) {
            AST.InstruccionBloque bloque = (AST.InstruccionBloque) inst;
            memoria.entrarAmbito();
            for (AST.Instruccion hijo : bloque.instrucciones) {
                if (detenerEjecucion) break;
                ejecutarInstruccion(hijo);
            }
            memoria.salirAmbito();
        } 
        else if (inst instanceof AST.InstruccionIf) {
            AST.InstruccionIf ifInst = (AST.InstruccionIf) inst;
            Object cond = evaluarExpresion(ifInst.condicion);
            
            if (cond instanceof Boolean) {
                if ((Boolean) cond) {
                    ejecutarInstruccion(ifInst.ramaVerdadera);
                } else if (ifInst.ramaFalsa != null) {
                    ejecutarInstruccion(ifInst.ramaFalsa);
                }
            } else {
                reportarError("Tipo incorrecto en condición: If requiere una expresión booleana.");
            }
        } 
        else if (inst instanceof AST.InstruccionWhile) {
            AST.InstruccionWhile whileInst = (AST.InstruccionWhile) inst;
            int iteraciones = 0;
            
            while (true) {
                Object cond = evaluarExpresion(whileInst.condicion);
                if (detenerEjecucion) break;
                
                if (!(cond instanceof Boolean)) {
                    reportarError("Tipo incorrecto en condición: While requiere expresión booleana.");
                    break;
                }
                
                if (!(Boolean) cond) break;
                
                ejecutarInstruccion(whileInst.cuerpo);
                if (detenerEjecucion) break;
                
                iteraciones++;
                if (iteraciones >= 10000) {
                    reportarError("Bucle infinito detectado (superó el límite de 10,000 iteraciones).");
                    break;
                }
            }
        } 
        else if (inst instanceof AST.InstruccionDoWhile) {
            AST.InstruccionDoWhile doWhileInst = (AST.InstruccionDoWhile) inst;
            int iteraciones = 0;
            
            do {
                // Ejecuta el cuerpo al menos una vez antes de evaluar la condición - JuanC
                ejecutarInstruccion(doWhileInst.cuerpo);
                if (detenerEjecucion) break;
                
                Object cond = evaluarExpresion(doWhileInst.condicion);
                if (detenerEjecucion) break;
                
                if (!(cond instanceof Boolean)) {
                    reportarError("Tipo incorrecto en condición: Do-While requiere expresión booleana.");
                    break;
                }
                
                if (!(Boolean) cond) break;
                
                iteraciones++;
                if (iteraciones >= 10000) {
                    reportarError("Bucle infinito detectado (superó el límite de 10,000 iteraciones).");
                    break;
                }
            } while (true);
        } 
        else if (inst instanceof AST.InstruccionFor) {
            AST.InstruccionFor forInst = (AST.InstruccionFor) inst;
            
            // Abriendo ámbito propio para las declaraciones locales del For - JuanC
            memoria.entrarAmbito();
            
            if (forInst.inicio != null) {
                ejecutarInstruccion(forInst.inicio);
            }
            
            int iteraciones = 0;
            while (true) {
                if (forInst.condicion != null) {
                    Object cond = evaluarExpresion(forInst.condicion);
                    if (detenerEjecucion) break;
                    
                    if (!(cond instanceof Boolean)) {
                        reportarError("Tipo incorrecto en condición: For requiere expresión booleana.");
                        break;
                    }
                    if (!(Boolean) cond) break;
                }
                
                ejecutarInstruccion(forInst.cuerpo);
                if (detenerEjecucion) break;
                
                if (forInst.incremento != null) {
                    ejecutarInstruccion(forInst.incremento);
                }
                if (detenerEjecucion) break;
                
                iteraciones++;
                if (iteraciones >= 10000) {
                    reportarError("Bucle infinito detectado (superó el límite de 10,000 iteraciones).");
                    break;
                }
            }
            
            memoria.salirAmbito();
        } 
        else if (inst instanceof AST.InstruccionPrint) {
            AST.InstruccionPrint printInst = (AST.InstruccionPrint) inst;
            Object resultado = evaluarExpresion(printInst.expresion);
            if (!detenerEjecucion) {
                salidaConsola.add(String.valueOf(resultado));
            }
        }
    }

    // Evaluador de nodos de expresiones - JuanC
    private Object evaluarExpresion(AST.Expresion expr) {
        if (expr instanceof AST.ExpresionNumero) {
            double v = ((AST.ExpresionNumero) expr).valor;
            // Para permitir la lógica int/int = int requerida, devolvemos Integer si no hay fracción - JuanC
            if (v == (long) v) return (int) v; 
            return v;
        } 
        else if (expr instanceof AST.ExpresionCadena) {
            return ((AST.ExpresionCadena) expr).valor;
        } 
        else if (expr instanceof AST.ExpresionBooleana) {
            return ((AST.ExpresionBooleana) expr).valor;
        } 
        else if (expr instanceof AST.ExpresionIdentificador) {
            String nombre = ((AST.ExpresionIdentificador) expr).nombre;
            try {
                Symbol sym = memoria.getSymbol(nombre);
                if (sym.getValor() == null) {
                    reportarError("Variable no inicializada al momento de usarla: " + nombre);
                    return null;
                }
                return sym.getValor();
            } catch (SemanticException e) {
                reportarError(e.getMessage());
                return null;
            }
        } 
        else if (expr instanceof AST.ExpresionBinaria) {
            AST.ExpresionBinaria bin = (AST.ExpresionBinaria) expr;
            Object izq = evaluarExpresion(bin.izquierdo);
            Object der = evaluarExpresion(bin.derecho);
            if (detenerEjecucion) return null;
            return calcularOperacion(izq, bin.operador, der);
        }
        return null;
    }

    // Motor de operaciones unificado con manejo de tipos y errores matemáticos - JuanC
    private Object calcularOperacion(Object izq, String op, Object der) {
        // Numéricos (+, -, *, /) y Relacionales - JuanC
        if (izq instanceof Number && der instanceof Number) {
            double v1 = ((Number) izq).doubleValue();
            double v2 = ((Number) der).doubleValue();
            
            // Si ambos valores entraron como Integer, el resultado prioriza Integer - JuanC
            boolean sonEnteros = (izq instanceof Integer) && (der instanceof Integer);

            switch (op) {
                case "+": return sonEnteros ? (int)(v1 + v2) : (v1 + v2);
                case "-": return sonEnteros ? (int)(v1 - v2) : (v1 - v2);
                case "*": return sonEnteros ? (int)(v1 * v2) : (v1 * v2);
                case "/":
                    if (v2 == 0) { 
                        reportarError("Error matemático: División por cero."); 
                        return null; 
                    }
                    return sonEnteros ? (int)(v1 / v2) : (v1 / v2);
                case "<": return v1 < v2;
                case ">": return v1 > v2;
                case "<=": return v1 <= v2;
                case ">=": return v1 >= v2;
                case "==": return v1 == v2;
                case "!=": return v1 != v2;
            }
        }

        // Operaciones de String: Solo permitir "+" para concatenación - JuanC
        if (izq instanceof String || der instanceof String) {
            if (op.equals("+")) {
                return String.valueOf(izq) + String.valueOf(der);
            }
            if (op.equals("==")) {
                return String.valueOf(izq).equals(String.valueOf(der));
            }
            if (op.equals("!=")) {
                return !String.valueOf(izq).equals(String.valueOf(der));
            }
            reportarError("Error de tipos: El operador '" + op + "' no está permitido para Strings. Solo '+' como concatenación.");
            return null;
        }

        // Operaciones Lógicas (&&, ||) y Comparaciones exactas - JuanC
        if (izq instanceof Boolean && der instanceof Boolean) {
            boolean b1 = (Boolean) izq;
            boolean b2 = (Boolean) der;
            switch (op) {
                case "&&": return b1 && b2;
                case "||": return b1 || b2;
                case "==": return b1 == b2;
                case "!=": return b1 != b2;
            }
        }

        // Comparaciones generales o fallback - JuanC
        if (op.equals("==")) {
            return izq != null && izq.equals(der);
        }
        if (op.equals("!=")) {
            return izq != null && !izq.equals(der);
        }

        reportarError("Operación no reconocida o incompatible entre los operandos: " + izq + " " + op + " " + der);
        return null;
    }
}