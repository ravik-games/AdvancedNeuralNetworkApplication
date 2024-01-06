package anna.ui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PopupController {
    //Method for showing errors to user

    private static final ResourceBundle bundle = ResourceBundle.getBundle("fxml/bindings/Localization", Locale.getDefault()); // Get current localization
    public static void errorMessage(String rawType, String title, String headerMessage, String infoMessage, boolean close){
        Platform.runLater(() -> {
            Alert.AlertType type = switch (rawType.toUpperCase()) {
                case "ERROR" -> Alert.AlertType.ERROR;
                case "INFORMATION" -> Alert.AlertType.INFORMATION;
                default -> Alert.AlertType.WARNING;
            };
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(headerMessage);
            alert.setContentText(infoMessage);
            alert.showAndWait();

            if (close)
                System.exit(1);
        });
    }

    public static void errorMessage(String rawType, String headerMessage, String infoMessage){
        errorMessage(rawType, bundle.getString("logger." + switch (rawType.toUpperCase()) {
            case "ERROR" -> "error";
            case "INFORMATION" -> "info";
            default -> "warning";
        }), headerMessage, infoMessage, false);
    }

    //Automatic label with localization
    public static void errorMessage(String rawType, String headerMessage, String infoMessage, boolean close){
        errorMessage(rawType, bundle.getString("logger." + switch (rawType.toUpperCase()) {
            case "ERROR" -> "error";
            case "INFORMATION" -> "info";
            default -> "warning";
        }), headerMessage, infoMessage, close);
    }

    //Open window and select file
    public static String openExplorer(){
        FileChooser open = new FileChooser();
        File file;
        open.setTitle(bundle.getString("general.selectData"));
        String currentDir = new File(System.getProperty("user.dir") + "\\src\\resources\\datasets").isDirectory() ? System.getProperty("user.dir") + "\\src\\resources\\datasets" : System.getProperty("user.dir");
        open.setInitialDirectory(new File(currentDir));
        open.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(".csv files", "*.csv"));
        file = open.showOpenDialog(null);
        if(file == null) {
            Logger.getLogger(PopupController.class.getName()).log(Level.WARNING, "Selected file is not valid");
            errorMessage("WARNING", "", bundle.getString("logger.warning.fileNotValid"));
            return null;
        }
        return file.getAbsolutePath();
    }

    public static void openURI(String uri){
        try {
            Desktop.getDesktop().browse(new URI(uri));
        } catch (Exception e) {
            Logger.getLogger(PopupController.class.getName()).log(Level.WARNING, "Couldn't open the link.");
            errorMessage("WARNING", "", e.getMessage());
        }
    }
}
