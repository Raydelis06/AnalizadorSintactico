import java.util.List;

public class AST {

   
    public static abstract class Instruccion { }

    public static class InstruccionBloque extends Instruccion {
        public final List<Instruccion> instrucciones;
        public InstruccionBloque(List<Instruccion> instrucciones) {
            this.instrucciones = instrucciones;
        }
    }

    public static class InstruccionDeclaracion extends Instruccion {
        public final String tipo;
        public final String nombre;
        public final Expresion valorInicial; 

        public InstruccionDeclaracion(String tipo, String nombre, Expresion valorInicial) {
            this.tipo = tipo;
            this.nombre = nombre;
            this.valorInicial = valorInicial;
        }
    }

    public static class InstruccionAsignacion extends Instruccion {
        public final String nombre;
        public final String operador;
        public final Expresion valor;

        public InstruccionAsignacion(String nombre, String operador, Expresion valor) {
            this.nombre = nombre;
            this.operador = operador;
            this.valor = valor;
        }
    }

    public static class InstruccionIf extends Instruccion {
        public final Expresion condicion;
        public final Instruccion ramaVerdadera;
        public final Instruccion ramaFalsa; 
        public InstruccionIf(Expresion condicion, Instruccion ramaVerdadera, Instruccion ramaFalsa) {
            this.condicion = condicion;
            this.ramaVerdadera = ramaVerdadera;
            this.ramaFalsa = ramaFalsa;
        }
    }

    public static class InstruccionWhile extends Instruccion {
        public final Expresion condicion;
        public final Instruccion cuerpo;

        public InstruccionWhile(Expresion condicion, Instruccion cuerpo) {
            this.condicion = condicion;
            this.cuerpo = cuerpo;
        }
    }

    public static class InstruccionDoWhile extends Instruccion {
        public final Instruccion cuerpo;
        public final Expresion condicion;

        public InstruccionDoWhile(Instruccion cuerpo, Expresion condicion) {
            this.cuerpo = cuerpo;
            this.condicion = condicion;
        }
    }

    public static class InstruccionFor extends Instruccion {
        public final Instruccion inicio;

        public final Expresion condicion;

        public final Instruccion incremento;

        public final Instruccion cuerpo;

        public InstruccionFor(Instruccion inicio, Expresion condicion, Instruccion incremento, Instruccion cuerpo) {
            this.inicio = inicio;

            this.condicion = condicion;

            this.incremento = incremento;
            
            this.cuerpo = cuerpo;
        }
    }

    public static class InstruccionPrint extends Instruccion {
        public final Expresion expresion;

        public InstruccionPrint(Expresion expresion) {
            this.expresion = expresion;
        }
    }

    
    public static abstract class Expresion { 
       
        public String tipoDatoRecuperado; 
    }

    public static class ExpresionNumero extends Expresion {
        public final double valor;
        public ExpresionNumero(double valor) { this.valor = valor; }
    }

    public static class ExpresionCadena extends Expresion {
        public final String valor;
        public ExpresionCadena(String valor) { this.valor = valor; }
    }

    public static class ExpresionBooleana extends Expresion {
        public final boolean valor;
        public ExpresionBooleana(boolean valor) { this.valor = valor; }
    }

    public static class ExpresionIdentificador extends Expresion {
        public final String nombre;
        public ExpresionIdentificador(String nombre) { this.nombre = nombre; }
    }

    public static class ExpresionBinaria extends Expresion {
        public final Expresion izquierdo;
        public final String operador;
        public final Expresion derecho;

        public ExpresionBinaria(Expresion izquierdo, String operador, Expresion derecho) {
            this.izquierdo = izquierdo;
            this.operador = operador;
            this.derecho = derecho;
        }
    }

    public static class ExpresionUnaria extends Expresion {
        public final String operador;
        public final Expresion operando;

        public ExpresionUnaria(String operador, Expresion operando) {
            this.operador = operador;
            this.operando = operando;
        }
    }

    public static class InstruccionClase extends Instruccion {
        public final List<String> modificadores;
        public final String nombre;
        public final InstruccionBloque cuerpo;

        public InstruccionClase(List<String> modificadores, String nombre, InstruccionBloque cuerpo) {
            this.modificadores = modificadores;
            this.nombre = nombre;
            this.cuerpo = cuerpo;
        }
    }

    public static class InstruccionFuncion extends Instruccion {
        public final List<String> modificadores;
        public final String tipoRetorno;
        public final String nombre;
        public final List<Parametro> parametros;
        public final InstruccionBloque cuerpo;

        public InstruccionFuncion(List<String> modificadores, String tipoRetorno, String nombre,
                                List<Parametro> parametros, InstruccionBloque cuerpo) {
            this.modificadores = modificadores;
            this.tipoRetorno = tipoRetorno;
            this.nombre = nombre;
            this.parametros = parametros;
            this.cuerpo = cuerpo;
        }
    }

    public static class Parametro {
        public final String tipo;
        public final String nombre;

        public Parametro(String tipo, String nombre) {
            this.tipo = tipo;
            this.nombre = nombre;
        }
    }
    public static class InstruccionImport extends Instruccion {
        public final String paquete;
        public InstruccionImport(String paquete) {
            this.paquete = paquete;
        }
    }

}