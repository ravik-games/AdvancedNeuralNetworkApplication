package ANNA.UI;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.net.URI;

public class PopupController {
    //Method for showing errors to user
    public static void errorMessage(String rawType, String title, String headerMessage, String infoMessage){
        Alert.AlertType type = switch (rawType) {
            case "ERROR" -> Alert.AlertType.ERROR;
            case "INFORMATION" -> Alert.AlertType.INFORMATION;
            default -> Alert.AlertType.WARNING;
        };
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(headerMessage);
        alert.setContentText(infoMessage);
        alert.showAndWait();
    }

    //Open window and select file
    public static String openExplorer(){
        FileChooser open = new FileChooser();
        File file;
        open.setTitle("Выберите данные");
        String currentDir = new File(System.getProperty("user.dir") + "\\src\\resources\\Train").isDirectory() ? System.getProperty("user.dir") + "\\src\\resources\\Train" : System.getProperty("user.dir");
        open.setInitialDirectory(new File(currentDir));
        open.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(".csv files", "*.csv"));
        file = open.showOpenDialog(null);
        if(file == null) {
            System.err.println("ERROR: Selected file is not valid");
            errorMessage("WARNING", "Ошибка", "", "Выбранный файл не действителен.");
            return null;
        }
        return file.getAbsolutePath();
    }

    public static void openURI(String uri){
        try {
            Desktop.getDesktop().browse(new URI(uri));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
