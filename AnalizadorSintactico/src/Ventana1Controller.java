import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Ventana1Controller implements Initializable{

    @FXML
    private Button btnAnalizar;

    @FXML
    private ListView<String> listTokens;

    @FXML 
    private ListView<String> listEstructura;

    @FXML
    private TextArea txtConsola;

    @FXML
    private TextArea txtInput;

    @FXML
    private ListView<Integer> listNumeros;
    
    private ObservableList<Integer> numeros;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        //estilo del listview
        listNumeros.setCellFactory(lv -> new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setTextFill(Color.web("#5e6372")); 
                    setFont(Font.font("Consolas", 13)); 
                    setPadding(new Insets(2.3,0,0,6)); // espacio entre ítems
                    setBackground(new Background(new BackgroundFill(
                        Color.web("transparent"), CornerRadii.EMPTY, Insets.EMPTY
                    )));
                }
            }
        });

        numeros = FXCollections.observableArrayList();
        listNumeros.setItems(numeros);
        //Listener del textArea
        txtInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
                numeros.clear();
                String[] lineas = txtInput.getText().split("\n", -1); 
                for(int i = 0; i < lineas.length; i++){
                    numeros.add(i + 1);
                }
            }
        });
        //Sincronizar scroll
        txtInput.scrollTopProperty().addListener((observable, oldValue, newValue) -> {
            listNumeros.scrollTo(newValue.intValue());
        });
    }
    
    @FXML
    void analizarCodigo(ActionEvent event) {
        listTokens.getItems().clear();
        listEstructura.getItems().clear();
        txtConsola.setText("");
        
        String codigo = txtInput.getText();

        if (codigo == null || codigo.trim().isEmpty()) {
            txtConsola.setText("Por favor, ingresa codigo para analizar.");
            return;
        }

        //Anlisis lexico
        Lexer lexer = new Lexer(codigo);
        List<Token> tokens = lexer.tokenize();
        
        boolean hayErrorLexico = false;
        for (Token t : tokens) {
            listTokens.getItems().add(t.toString());
            if (t.getType() == TokenType.ERROR) {
                hayErrorLexico = true;
                txtConsola.appendText(
                    "✗ ERROR LÉXICO  [Línea " + t.getLinea() + "]: "
                    + "Símbolo no reconocido → '" + t.getLexema() + "'\n"
                );
            }
        }
        if (hayErrorLexico) {
            txtConsola.appendText("\nAnálisis detenido: corrige los errores léxicos primero.\n");
            mostrarResumenEstructura("Análisis léxico fallido. Sin estructura disponible.");
            return;
        }

        txtConsola.appendText("LÉXICO: Sin errores.\n\n");

        //Analisis semantico
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        // Resetear la tabla de símbolos antes de cada análisis
        analyzer.getSymbolTable().reset();
        try {
            //Analisis sintactico
            Parser parser = new Parser(tokens, analyzer);
            parser.parse(); 
            
            txtConsola.appendText("=== SINTAXIS ===\nCorrecta\n\n");
            txtConsola.appendText("=== SEMANTICA ===\nCorrecta\n");

        } catch (SyntaxException ex) {
            txtConsola.setText(" ERROR SINTACTICO \n" + ex.getMessage());
        } catch (SemanticException ex) {
            txtConsola.setText(" ERROR SEMANTICO \n" + ex.getMessage());
        } catch (Exception ex) {
            txtConsola.setText(" ERROR INESPERADO \n" + ex.getMessage());
        }
        //Mostrar estructura
        mostrarEstructura(tokens, analyzer);
    }
    //Metodo para mostrar la estructura
    private void mostrarEstructura(List<Token> tokens, SemanticAnalyzer analyzer) {
        listEstructura.getItems().add("══════════════════════════════════════");
        listEstructura.getItems().add("  ESTRUCTURA DEL PROGRAMA");
        listEstructura.getItems().add("══════════════════════════════════════");
 
        // Sentencias detectadas (reconstrucción desde tokens)
        listEstructura.getItems().add("");
        listEstructura.getItems().add("--- SENTENCIAS RECONOCIDAS: ----------");
 
        int numSentencia = 1;
        StringBuilder sentenciaActual = new StringBuilder();
        int profundidad = 0;
 
        for (Token t : tokens) {
            if (t.getType() == TokenType.EOF) break;
 
            String lexema = t.getLexema();
            TokenType tipo = t.getType();
 
            if ("{".equals(lexema)) {
                profundidad++;
                if (sentenciaActual.length() > 0) {
                    agregarSentencia(sentenciaActual.toString().trim(), numSentencia++, 0);
                    sentenciaActual.setLength(0);
                }
                listEstructura.getItems().add(
                    "  " + indentar(profundidad - 1) + "{ —> Inicio de bloque"
                );
                continue;
            }
 
            if ("}".equals(lexema)) {
                if (sentenciaActual.length() > 0) {
                    agregarSentencia(sentenciaActual.toString().trim(), numSentencia++, profundidad);
                    sentenciaActual.setLength(0);
                }
                profundidad = Math.max(0, profundidad - 1);
                listEstructura.getItems().add(
                    "  " + indentar(profundidad) + "} —> Fin de bloque"
                );
                continue;
            }
 
            if (";".equals(lexema)) {
                if (sentenciaActual.length() > 0) {
                    agregarSentencia(sentenciaActual.toString().trim(), numSentencia++, profundidad);
                    sentenciaActual.setLength(0);
                }
                continue;
            }
 
            // Filtrar comentarios de la vista de estructura (los mostramos como nota)
            if (tipo == TokenType.COMENTARIO_DE_LINEA || tipo == TokenType.COMENTARIO_DE_VARIAS_LINEAS) {
                listEstructura.getItems().add(
                    "  " + indentar(profundidad) + "// [COMENTARIO] Línea " + t.getLinea()
                );
                continue;
            }
 
            sentenciaActual.append(lexema).append(" ");
        }
 
        // Última sentencia sin ';' al final (si la hay)
        if (sentenciaActual.length() > 0) {
            agregarSentencia(sentenciaActual.toString().trim(), numSentencia++, profundidad);
        }
 
        // ── Tabla de símbolos ───────────────────────────────────────────
        listEstructura.getItems().add("");
        listEstructura.getItems().add("--- TABLA DE SÍMBOLOS: ---------------");
        listEstructura.getItems().add(
            "  " + padDerecha("Nombre", 15)
                 + padDerecha("Tipo", 12)
                 + "Ámbito"
        );
        listEstructura.getItems().add(
            "  " + "─".repeat(42)
        );
 
        List<Symbol> simbolos = analyzer.getSymbolTable().getSimbolosVisibles();
        if (simbolos.isEmpty()) {
            listEstructura.getItems().add("  (ninguna variable declarada)");
        } else {
            for (Symbol s : simbolos) {
                listEstructura.getItems().add(
                    "  " + padDerecha(s.getName(), 15)
                         + padDerecha(s.getType(), 12)
                         + "global"
                );
            }
        }
 
        listEstructura.getItems().add("");
        listEstructura.getItems().add("══════════════════════════════════════");
    }
    /** Agrega una sentencia formateada al tab de estructura */
    private void agregarSentencia(String texto, int numero, int profundidad) {
        if (texto.isEmpty()) return;
        String tipo = clasificarSentencia(texto);
        listEstructura.getItems().add(
            "  " + indentar(profundidad)
            + "[" + numero + "] " + tipo + " → " + texto
        );
    }
    /** Clasifica superficialmente el tipo de sentencia para mostrarlo en la GUI */
    private String clasificarSentencia(String texto) {
        String lower = texto.toLowerCase();
        if (lower.startsWith("if"))         return "Selectiva (if)";
        if (lower.startsWith("else"))       return "Alternativa (else)";
        if (lower.startsWith("switch"))     return "Selectiva (switch)";
        if (lower.startsWith("while"))      return "Repetitiva (while)";
        if (lower.startsWith("for"))        return "Repetitiva (for)";
        if (lower.startsWith("do"))         return "Repetitiva (do-while)";
        if (lower.startsWith("return"))     return "Retorno";
        if (lower.startsWith("int ")    || lower.startsWith("double ")
         || lower.startsWith("boolean ") || lower.startsWith("string ")
         || lower.startsWith("char "))      return "Declaración";
        if (texto.contains(":=") || (texto.contains("=") && !texto.contains("==")))
                                            return "Asignación";
        if (lower.startsWith("system.out") || lower.startsWith("print"))
                                            return "Salida";
        return "Expresión";
    }
    /** Muestra un mensaje simple en la lista de estructura */
    private void mostrarResumenEstructura(String mensaje) {
        listEstructura.getItems().add("── " + mensaje);
    }
    /** Retorna una cadena de espacios para simular indentación */
    private String indentar(int nivel) {
        return "  ".repeat(Math.max(0, nivel));
    }
    /** Rellena un String con espacios hasta el ancho indicado */
    private String padDerecha(String texto, int ancho) {
        if (texto.length() >= ancho) return texto.substring(0, ancho);
        return texto + " ".repeat(ancho - texto.length());
    }
    
}
