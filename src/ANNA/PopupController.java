package ANNA;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.File;

public class PopupController {
    //Method for showing errors to user
    public static void errorMessage(String rawType, String title, String headerMessage, String infoMessage){
        Alert.AlertType type = switch (rawType) {
            case "ERROR" -> Alert.AlertType.ERROR;
            case "INFORMATION" -> Alert.AlertType.INFORMATION;
            default -> Alert.AlertType.WARNING;
        };
        Alert alert= new Alert(type);
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
        String currentDir = System.getProperty("user.dir") + "//src//resources";
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
}
