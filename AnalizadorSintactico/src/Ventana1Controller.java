import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

public class Ventana1Controller {

    @FXML
    private Button btnAnalizar;

    @FXML
    private ListView<String> listTokens;

    @FXML
    private TextArea txtConsola;

    @FXML
    private TextArea txtInput;

    @FXML
    void analizarCodigo(ActionEvent event) {
        listTokens.getItems().clear();
        txtConsola.setText("");
        
        String codigo = txtInput.getText();

        if (codigo == null || codigo.trim().isEmpty()) {
            txtConsola.setText("Por favor, ingresa codigo para analizar.");
            return;
        }

        try {
            Lexer lexer = new Lexer(codigo);
            List<Token> tokens = lexer.tokenize();
            
            for (Token t : tokens) {
                listTokens.getItems().add(t.toString());
                if (t.getType() == TokenType.ERROR) {
                    txtConsola.setText("ERROR LEXICO: Simbolo no reconocido -> " + t.getLexema());
                }
            }

            SemanticAnalyzer analyzer = new SemanticAnalyzer();

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
    }
}
