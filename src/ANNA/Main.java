package ANNA;

import ANNA.UI.UIController;
import ANNA.network.NeuralNetwork;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main extends Application {
    private NeuralNetwork network;
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    //Logger setup
    static {
        try {
            InputStream stream = Main.class.getClassLoader().getResourceAsStream("logger.properties");
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
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
