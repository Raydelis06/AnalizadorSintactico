public class Symbol {
    private String name;
    private String type;
    
    // Nuevo campo para soportar valores en ejecución dinámicos (Object) - JuanC
    private Object valor;

    public Symbol(String name, String type) {
        this.name = name;
        this.type = type;
        this.valor = null; // Inicializado en null por defecto - JuanC
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    // Getter para obtener el valor dinámico en tiempo de ejecución - JuanC
    public Object getValor() {
        return valor;
    }

    // Setter para actualizar el valor dinámico en tiempo de ejecución - JuanC
    public void setValor(Object valor) {
        this.valor = valor;
    }
}
