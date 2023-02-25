package ANNA.Functions;

import ANNA.Network.Hyperparameters;

public class ActivationFunctions {

    /**
     * Rectified Linear Unit activation function. Returns max value between 0 and x. Fast and efficient, but can't be used with gradient descent.
     * @param input double x
     * @return max between 0 and x
     */
    public static double ReLU(double input){
        return Math.max(0, input);
    }

    /**
     * Leaky Rectified Linear Unit activation function. Returns:
     * <p>
     * x --- if x >= 0
     * <p>
     * x * slope --- if x < 0
     * <p>
     * Less efficient than ReLU and not always worth using, but it's solving 'dying ReLU' problem.
     * @param input double x
     * @return Below 0 --- slope * x; <p> At 0 and above --- x
     */
    public static double LeakyReLU(double input){
        return Math.max(input * Hyperparameters.SLOPE_IN_ACTIVATION_FUNCTIONS, input);
    }

    /**
     * Standard activation function. Returns value between 0 and 1. Always a great option, but has problem of 'vanishing gradients' and some performance cost.
     * @param input double x
     * @return double value between 0 and 1
     */
    public static double sigmoid(double input){
        return 1 / (1 + Math.exp(-input));
    }

    /**
     * Standard activation function. Returns value between -1 and 1. Better gradient, than sigmoid and almost always preferred option. Has problem of 'vanishing gradients' and high performance cost.
     * @param input double x
     * @return double value between -1 and 1
     */
    public static double tanh(double input){
        return Math.tanh(input);
    }

    /**
     * Exponential Linear Unit activation function. Returns:
     * <p>
     * x --- if x >= 0
     * <p>
     * (<i>e</i><sup>{@code x}</sup> - 1) * slope --- if x < 0
     * <p>
     * Strong alternative to ReLU, can produce negative outputs.
     * @param input double x
     * @return Below 0 --- (<i>e</i><sup>{@code x}</sup> - 1) * slope; <p> At 0 and above --- x
     */
    public static double ExponentialLU(double input){
        //return input >= 0 ? input : slope * (Math.exp(input) - 1);
        return Math.max(Hyperparameters.SLOPE_IN_ACTIVATION_FUNCTIONS * (Math.exp(input) - 1), input);
    }

    public enum types{
        RELU, LRELU, SIGMOID, TANH, ELU
    }
}
