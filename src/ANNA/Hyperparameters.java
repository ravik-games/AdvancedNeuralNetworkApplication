package ANNA;

public class Hyperparameters {
    //Class with all hyperparameters of neural network

    /**
     * Number of epochs
     * <p>
     * Affects: TODO Test what does number of epochs affect
     */
    public static final int NUMBER_OF_EPOCHS = 1000;

    /**
     * The number of training samples used in one iteration. If equal or below 0, then will be used number of input arguments.
     * <p>
     * Affects: TODO Test what does batch size affect
     */
    public static final int BATCH_SIZE = 0;

    /**
     * Use bias neurons
     * <p>
     * Affects: TODO Test what does bias neurons affect
     */
    public static final boolean USE_BIAS_NEURONS = false; //FIXME Doesn't work

    /**
     * How weights will be initialized on different layers
     * <p>
     * Affects: TODO Add options for initializing weights
     */
    public static final int NETWORK_WEIGHT_INITIALIZATION = 0; //TODO Not implemented

    /**
     * How quickly a network updates its parameters.
     * <p>
     * Affects: TODO Test what does learning rate affect
     */
    public static final double LEARNING_RATE = 0.9;

    /**
     * Helps to prevent oscillations and getting stuck in local minimums.
     * <p>
     * Affects: TODO Test what does momentum affect
     */
    public static final double MOMENTUM = 0.7;

    /**
     * Needed for ELU and LReLU. Default is 0.01.
     * <p>
     * Affects: TODO Test what does slope affect
     */
    public static final double SLOPE_IN_ACTIVATION_FUNCTIONS = 0.01;
}
