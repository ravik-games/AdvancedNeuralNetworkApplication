package ANNA;

import ANNA.Network.NeuralNetwork;
import ANNA.UI.UIController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private NeuralNetwork network;

    public static void main(String[] args) {
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
        network.run(arguments);
    }

    public void runSimulation(double[] inputs){
        if(network == null)
            return;
        network.simulation(inputs);
    }
}
