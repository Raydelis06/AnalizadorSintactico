import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class GestorArchivos {

    private File archivoActual;

    public String abrirArchivo(Stage stage){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Abrir archivo java");
        fileChooser.getExtensionFilters().add(
            new ExtensionFilter("Archivos Java", "*.java")
        );
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
                System.out.println("Archivo seleccionado: " + file.getAbsolutePath());
                archivoActual = file;
            try{
                return Files.readString(file.toPath()).toString(); 
            }catch(IOException IOex){
                System.out.println("Ha ocurrido un error al intentar leer el archivo \n" + IOex.getMessage());
            }
        } else {
            System.out.println("No se seleccionó ningún archivo.");
        }
        return null;
    }
    public void guardarArchivo(Stage stage, String contenido){

        if (archivoActual != null) {
            System.out.println("Archivo a guardar: " + archivoActual.getAbsolutePath());
            try {
                Files.writeString(archivoActual.toPath(), contenido);
                System.out.println("Archivo guardado correctamente");
            } catch (IOException e) {
                System.out.println("Ha ocurrido un error al intentar guardar el archivo \n" + e.getMessage());
            }
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar archivo java");
            fileChooser.getExtensionFilters().add(
                new ExtensionFilter("Archivos Java", "*.java")
            );
            File file = fileChooser.showSaveDialog(stage);

            try {
                Files.writeString(file.toPath(), contenido);
                System.out.println("Archivo guardado correctamente");
            } catch (IOException e) {
                System.out.println("Ha ocurrido un error al intentar guardar el archivo \n" + e.getMessage());
            }
        }
    }
    public String getNombreArchivo() {
        return archivoActual.getName();
    }

}
