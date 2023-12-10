package anna.network;

import anna.math.ErrorFunctions;

import java.util.Locale;
import java.util.ResourceBundle;

public class Hyperparameters {
    //Class with all hyperparameters of neural network

    //Resource bundle with localization
    private static final ResourceBundle bundle = ResourceBundle.getBundle("fxml/bindings/Localization", Locale.getDefault());

    /**
     * Number of epochs
     * <p>
     * Affects: TODO Test what does number of epochs affect
     */
    public static int NUMBER_OF_EPOCHS = 1000;

    /**
     * Error calculation function
     * <p>
     * Affects: TODO Test what error function affect
     */
    public static ErrorFunctions.Types ERROR_FUNCTION = ErrorFunctions.Types.MSE;

    /**
     * The number of training samples used in one iteration. If equal or below 0, then will be used number of input arguments.
     * <p>
     * Affects: TODO Test what does batch size affect
     */
    public static int BATCH_SIZE = 0;

    /**
     * Use bias neurons
     * <p>
     * Affects: TODO Test what does bias neurons affect
     */
    public static boolean USE_BIAS_NEURONS = false; //FIXME Doesn't work

    /**
     * How weights will be initialized on different layers
     * <p>
     * Affects: TODO Add options for initializing weights
     */
    public static WeightsInitializationType NETWORK_WEIGHT_INITIALIZATION = WeightsInitializationType.RANDOM; //TODO Not implemented

    /**
     * How quickly a network updates its parameters.
     * <p>
     * Affects: TODO Test what does learning rate affect
     */
    public static double LEARNING_RATE = 0.015;

    /**
     * Helps to prevent oscillations and getting stuck in local minimums.
     * <p>
     * Affects: TODO Test what does momentum affect
     */
    public static double MOMENTUM = 0.3;

    /**
     * Needed for ELU and LReLU. Default is 0.01.
     * <p>
     * Affects: TODO Test what does slope affect
     */
    public static double SLOPE_IN_ACTIVATION_FUNCTIONS = 0.01;

    public enum Identificator {
        NUMBER_OF_EPOCHS(
                bundle.getString("tab.management.hyperparameters.name.numberOfEpochs"),
                bundle.getString("tab.management.hyperparameters.description.numberOfEpochs"),
                Type.POSITIVE_INT),

        ERROR_FUNCTION(
                bundle.getString("tab.management.hyperparameters.name.errorFunction"),
                bundle.getString("tab.management.hyperparameters.description.errorFunction"),
                Type.ENUM),

        BATCH_SIZE(
                bundle.getString("tab.management.hyperparameters.name.batchSize"),
                bundle.getString("tab.management.hyperparameters.description.batchSize"),
                Type.POSITIVE_INT),

        USE_BIAS_NEURONS(
                bundle.getString("tab.management.hyperparameters.name.biasNeurons"),
                bundle.getString("tab.management.hyperparameters.description.biasNeurons"),
                Type.BOOLEAN),

        NETWORK_WEIGHT_INITIALIZATION(
                bundle.getString("tab.management.hyperparameters.name.weightInit"),
                bundle.getString("tab.management.hyperparameters.description.weightInit"),
                Type.ENUM),

        LEARNING_RATE(
                bundle.getString("tab.management.hyperparameters.name.learningRate"),
                bundle.getString("tab.management.hyperparameters.description.learningRate"),
                Type.POSITIVE_DOUBLE),

        MOMENTUM(
                bundle.getString("tab.management.hyperparameters.name.momentum"),
                bundle.getString("tab.management.hyperparameters.description.momentum"),
                Type.POSITIVE_DOUBLE),

        SLOPE_IN_ACTIVATION_FUNCTIONS(
                bundle.getString("tab.management.hyperparameters.name.slopeInFunctions"),
                bundle.getString("tab.management.hyperparameters.description.slopeInFunctions"),
                Type.POSITIVE_DOUBLE);

        private final String name;
        private final String description;
        private final Type type;
        Identificator(String name, String description, Type type){
            this.name = name;
            this.description = description;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public Type getType() {
            return type;
        }
    }
    public enum Type {
        BOOLEAN, POSITIVE_DOUBLE, POSITIVE_INT, DOUBLE, ENUM
    }

    public enum WeightsInitializationType {
        RANDOM, DEFAULT_ONE, DEFAULT_HALF
    }
}
