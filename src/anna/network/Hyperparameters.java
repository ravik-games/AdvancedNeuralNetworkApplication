package anna.network;

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
    public static int NETWORK_WEIGHT_INITIALIZATION = 0; //TODO Not implemented

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

    public static String getValueByID(Identificator id){
        switch(id){
            default -> {
                return null;
            }
            case NUMBER_OF_EPOCHS -> {
                return String.valueOf(NUMBER_OF_EPOCHS);
            }
            case BATCH_SIZE -> {
                return String.valueOf(BATCH_SIZE);
            }
            case USE_BIAS_NEURONS -> {
                return String.valueOf(USE_BIAS_NEURONS);
            }
            case NETWORK_WEIGHT_INITIALIZATION -> {
                return String.valueOf(NETWORK_WEIGHT_INITIALIZATION);
            }
            case LEARNING_RATE -> {
                return String.valueOf(LEARNING_RATE);
            }
            case MOMENTUM -> {
                return String.valueOf(MOMENTUM);
            }
            case SLOPE_IN_ACTIVATION_FUNCTIONS -> {
                return String.valueOf(SLOPE_IN_ACTIVATION_FUNCTIONS);
            }
        }
    }

    public static void setValueByID(Identificator id, String value){
        //TODO Add check for value
        switch(id){
            case NUMBER_OF_EPOCHS -> NUMBER_OF_EPOCHS = Integer.parseInt(value);

            case BATCH_SIZE -> BATCH_SIZE = Integer.parseInt(value);

            case USE_BIAS_NEURONS -> USE_BIAS_NEURONS = Boolean.parseBoolean(value);

            case NETWORK_WEIGHT_INITIALIZATION -> NETWORK_WEIGHT_INITIALIZATION = Integer.parseInt(value);

            case LEARNING_RATE -> LEARNING_RATE = Double.parseDouble(value);

            case MOMENTUM -> MOMENTUM = Double.parseDouble(value);

            case SLOPE_IN_ACTIVATION_FUNCTIONS -> SLOPE_IN_ACTIVATION_FUNCTIONS = Double.parseDouble(value);
        }
    }

    public enum Identificator {
        NUMBER_OF_EPOCHS(
                bundle.getString("tab.management.hyperparameters.name.numberOfEpochs"),
                bundle.getString("tab.management.hyperparameters.description.numberOfEpochs"),
                Type.POSITIVE_INT),

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
                Type.POSITIVE_INT),

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
        BOOLEAN, POSITIVE_DOUBLE, POSITIVE_INT, DOUBLE
    }
}