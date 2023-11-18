package anna;

import anna.ui.UIController;
import anna.network.NeuralNetwork;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Application extends javafx.application.Application {
    private NeuralNetwork network;
    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

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

    @Override
    public void start(Stage primaryStage) throws Exception{
        ResourceBundle bundle = ResourceBundle.getBundle("fxml/bindings/Localization", Locale.getDefault()); // Get current localization
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"), bundle);
        Parent root = loader.load();
        primaryStage.setTitle("Advanced Neural Network Application");
        primaryStage.setScene(new Scene(root, 1280, 720));
        primaryStage.resizableProperty().setValue(false);
        primaryStage.show();

        UIController controller = loader.getController();
        controller.setMain(this);
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
}
