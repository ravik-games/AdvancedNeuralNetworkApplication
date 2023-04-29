package ANNA.Network;

import ANNA.Functions.ActivationFunctions;
import ANNA.Network.neurons.BasicNeuron;
import ANNA.Network.neurons.Neuron;

import java.util.ArrayList;

public class NetworkStructure {
    //Class, containing data about neurons and network structure

    //List of full neuron data by id
    private final ArrayList<NeuronData> neuronsList = new ArrayList<>();

    //Structure of neural network
    //First column contains input neurons, last contains output and between all is a hidden neurons
    private final ArrayList<ArrayList<Neuron>> structure = new ArrayList<>();


    //Initialize structure
    public NetworkStructure(int[] structure, double[][] weights){
        setStructure(structure, weights);
    }

    /**
     * @param newStructure Array of Integers. Represents number of neurons in each layer.
     */
    public void setStructure(int[] newStructure, double[][] weights){
        //Clear previous values
        structure.clear();
        neuronsList.clear();

        //Initialize new neurons
        for(int i = 0; i < newStructure.length; i++) {

            ArrayList<Neuron> layer = new ArrayList<>(newStructure.length);
            //Create new neurons in layer
            for (int j = 0; j < newStructure[i]; j++) {
                if(i == 0) //Input layer
                    layer.add(initializeNeuron(i, j, neuronTypes.INPUT, ActivationFunctions.types.LINEAR));
                else if(i == newStructure.length - 1) //Output layer
                    layer.add(initializeNeuron(i, j, neuronTypes.OUTPUT, ActivationFunctions.types.SIGMOID));
                else //Hidden layers
                    layer.add(initializeNeuron(i, j, neuronTypes.HIDDEN, ActivationFunctions.types.SIGMOID));
            }
            structure.add(layer);
        }

        //Create synapses
        for (int i = 0; i < structure.size() - 1; i++) {
            for (int j = 0; j < structure.get(i).size(); j++) {
                //Getting next rows neurons
                for (int k = 0; k < structure.get(i + 1).size(); k++) {
                    double weight;
                    int firstID = getIDByPosition(i, j);
                    int secondID = getIDByPosition(i + 1, k);
                    //Using pre-defined weights if we can, else random
                    if(weights != null)
                        weight = weights[firstID][secondID];
                    else
                        //weight = 0.5;
                        weight = Math.random() * 2 - 1;
                    createSynapse(firstID, secondID, weight);
                }
            }
        }
    }

    public void createSynapse(int firstID, int secondID, double initialWeight){
        neuronsList.get(firstID).getOutputConnections().add(new Synapse(secondID, initialWeight, 0));
        neuronsList.get(secondID).getInputConnections().add(new Synapse(firstID, initialWeight, 0));
    }

    //Initialize single neuron and add it to the arrays
    private Neuron initializeNeuron(int positionY, int positionX, neuronTypes type, ActivationFunctions.types activationFunction){
        //Create new neuron and set it ID
        Neuron neuron = new BasicNeuron(positionX, positionY, neuronsList.size(), activationFunction, type);
        //Add to the array
        neuronsList.add(new NeuronData(neuron));
        //neuronsArray.add(value);
        return neuron;
    }

    //Getters
    public int getIDByPosition(int x, int y){
        return getNeuronByPosition(x, y).getId();
    }

    public Neuron getNeuronByID(int id){
        return neuronsList.get(id).neuron;
    }
    public Neuron getNeuronByPosition(int x, int y){
        return structure.get(x).get(y);
    }

    public ArrayList<Synapse> getInputConnections(int neuronID){
        return neuronsList.get(neuronID).getInputConnections();
    }
    public ArrayList<Synapse> getOutputConnections(int neuronID){
        return neuronsList.get(neuronID).getOutputConnections();
    }

    public int getLayersAmount(){
        return structure.size();
    }
    public int getNeuronsAmountInLayer(int layer){
        return structure.get(layer).size();
    }

    public int getLastID(){
        return neuronsList.size() - 1;
    }

    public void printWeights(boolean delta){
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < neuronsList.size(); i++) {
            str.append("Neuron ").append(i).append(" (").append(neuronsList.get(i).neuron.toString()).append(")\t");
            str.append("Input: ");
            for (int j = 0; j < neuronsList.get(i).getInputConnections().size(); j++) {
                str.append("(").append(neuronsList.get(i).getInputConnections().get(j).getNeuronID()).append("; ");
                if(delta)
                    str.append(neuronsList.get(i).getInputConnections().get(j).getDeltaWeight());
                else
                    str.append(neuronsList.get(i).getInputConnections().get(j).getWeight());
                str.append(")");
                if(j != neuronsList.get(i).getInputConnections().size() - 1)
                    str.append(", ");
                else
                    str.append("\t");
            }
            str.append("Output: ");
            for (int j = 0; j < neuronsList.get(i).getOutputConnections().size(); j++) {
                str.append("(").append(neuronsList.get(i).getOutputConnections().get(j).getNeuronID()).append("; ");
                if(delta)
                    str.append(neuronsList.get(i).getOutputConnections().get(j).getDeltaWeight());
                else
                    str.append(neuronsList.get(i).getOutputConnections().get(j).getWeight());
                str.append(")");
                if(j != neuronsList.get(i).getOutputConnections().size() - 1)
                    str.append(", ");
                else
                    str.append("\t");
            }
            str.append("\n");
        }
        System.out.println(str);
    }

    //Information about one neuron
    private static class NeuronData {
        private Neuron neuron;
        private ArrayList<Synapse> inputConnections = new ArrayList<>();
        private ArrayList<Synapse> outputConnections = new ArrayList<>();

        public NeuronData(Neuron neuron, ArrayList<Synapse> inputConnections, ArrayList<Synapse> outputConnections){
            setNeuron(neuron);
            setInputConnections(inputConnections);
            setOutputConnections(outputConnections);
        }

        public NeuronData(Neuron neuron){
            setNeuron(neuron);
        }

        public Neuron getNeuron() {
            return neuron;
        }
        public void setNeuron(Neuron neuron) {
            this.neuron = neuron;
        }

        public ArrayList<Synapse> getInputConnections() {
            return inputConnections;
        }
        public void setInputConnections(ArrayList<Synapse> inputConnections) {
            this.inputConnections = inputConnections;
        }

        public ArrayList<Synapse> getOutputConnections() {
            return outputConnections;
        }
        public void setOutputConnections(ArrayList<Synapse> outputConnections) {
            this.outputConnections = outputConnections;
        }
    }

    //Information about one synapse (connection) between neurons
    public static class Synapse{
        private double weight, deltaWeight;

        private final int neuronID;

        public int getNeuronID() {
            return neuronID;
        }

        public double getWeight() {
            return weight;
        }
        public void setWeight(double weight) {
            this.weight = weight;
        }

        public double getDeltaWeight() {
            return deltaWeight;
        }
        public void setDeltaWeight(double deltaWeight) {
            this.deltaWeight = deltaWeight;
        }

        public Synapse(int neuronID, double weight, double deltaWeight){
            this.deltaWeight = deltaWeight;
            this.weight = weight;
            this.neuronID = neuronID;
        }
    }

    public enum neuronTypes{
        INPUT, HIDDEN, OUTPUT
    }
}
