package ANNA.Functions;

import ANNA.Network.Hyperparameters;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LearningFunctionsTest {

    @Test
    void outputDelta() {
    }

    @Test
    void hiddenDelta() {
    }

    @Test
    void gradient() {
    }

    @Test
    void derivative() {
        double[] testValues = new double[]{-1, 0, 1};

        //ReLU
        double[] ReLUValues = new double[]{0, 0, 1};
        for (int i = 0; i < testValues.length; i++) {
            assertEquals(ReLUValues[i], LearningFunctions.derivative(ActivationFunctions.types.RELU, testValues[i]));
        }
        System.out.println("ReLU Passed");

        //LReLU
        double[] LReLUValues = new double[]{Hyperparameters.SLOPE_IN_ACTIVATION_FUNCTIONS, Hyperparameters.SLOPE_IN_ACTIVATION_FUNCTIONS, 1};
        for (int i = 0; i < testValues.length; i++) {
            assertEquals(LReLUValues[i], LearningFunctions.derivative(ActivationFunctions.types.LRELU, testValues[i]));
        }
        System.out.println("LReLU Passed");

        //Sigmoid
        double[] SigmoidValues = new double[]{0.19661193324148185, 0.25, 0.19661193324148185};
        for (int i = 0; i < testValues.length; i++) {
            assertEquals(SigmoidValues[i], LearningFunctions.derivative(ActivationFunctions.types.SIGMOID, testValues[i]));
        }
        System.out.println("Sigmoid Passed");
    }

    @Test
    void deltaWeight() {
    }
}