package anna;

import anna.network.NeuralNetwork;
import anna.ui.DefaultUIController;
import anna.ui.DefaultUIMenuController;
import anna.ui.PopupController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Application extends javafx.application.Application {
    // Main application class, which connects UI and network

    protected NeuralNetwork network;
    protected Scene mainScene;
    protected DataMaster dataMaster;

    protected static ResourceBundle bundle;
    protected static final Logger LOGGER = Logger.getLogger(Application.class.getName());

    //Logger setup
    static {
        try {
            InputStream stream = Application.class.getClassLoader().getResourceAsStream("logger.properties");
            LogManager.getLogManager().readConfiguration(stream);
            //LOGGER.addHandler(new FileHandler("/logs/latest.log"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        LOGGER.info("Starting application...");
        launch(args);
    }

    // Start application and load main menu
    @Override
    public void start(Stage primaryStage) {
        Locale.setDefault(DataMaster.loadProgramData().locale());
        dataMaster = new DataMaster(); // Initialize DataMaster class for working with dataset

        mainScene = new Scene(new Pane(), 1280, 720);
        mainScene.setFill(Color.WHITE); // Set default scene fill. For dark theme should be changed to BLACK or similar
        primaryStage.setTitle("Educational Learning Engine for Neural Networks Application");
        primaryStage.setScene(mainScene);
        primaryStage.resizableProperty().setValue(true);

        loadMainMenu();

        primaryStage.show();
    }

    // Load main menu
    public void loadMainMenu() {
        try {
            bundle = ResourceBundle.getBundle("fxml/bindings/Localization", Locale.getDefault()); // Get current localization
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainMenu.fxml"), bundle);
            mainScene.setRoot(loader.load());

            DefaultUIMenuController controller = loader.getController();
            controller.setMain(this);
        } catch (IOException e) {
            PopupController.errorMessage("ERROR", "", bundle.getString("logger.error.fileLoadingError"));
            Logger.getLogger(getClass().getName()).log(Level.SEVERE ,"Couldn't load .fxml files. See stack trace:\n" + e.getMessage());
        }
    }

    // Load network editor
    public void startNetworkEditor(boolean advancedMode) {
        try {
            bundle = ResourceBundle.getBundle("fxml/bindings/Localization", Locale.getDefault()); // Get current localization
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditorAdvanced.fxml"), bundle);
            mainScene.setRoot(loader.load());

            DefaultUIController controller = loader.getController();
            controller.setMain(this);
        } catch (Exception e) {
            PopupController.errorMessage("ERROR", "", bundle.getString("logger.error.fileLoadingError"), true);
            Logger.getLogger(getClass().getName()).log(Level.SEVERE ,"Couldn't load .fxml files. See stack trace:\n" + e);
        }
    }

    public void runNeuralNetwork(NeuralNetwork.NetworkArguments arguments){
        if(network == null)
            network = new NeuralNetwork();
        network.start(arguments);
    }

    public void runSimulation(double[] inputs){
        if(network == null)
            return;
        network.simulation(inputs);
    }

    public void stop(){
        // Save program data before closing
        DataMaster.saveProgramData(new DataMaster.ProgramData(Locale.getDefault()));
    }

    // Getters for inner fields
    public DataMaster getDataMaster() {
        return dataMaster;
    }
}
