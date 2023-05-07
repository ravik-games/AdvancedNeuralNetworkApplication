package ANNA.Network;

import ANNA.Network.neurons.Neuron;

import java.util.ArrayList;
import java.util.List;

public class DataTypes {
    //Class, storing information about network in short form (for save/load)
    public static class NetworkData {
        private final int[] structure;
        private final List<NeuronWeightData> weights;

        public NetworkData(int[] structure, List<NeuronData> neuronData){
            this.structure = structure;
            weights = new ArrayList<>();
            for (NeuronData singleNeuronData : neuronData) {
                weights.add(new NeuronWeightData(singleNeuronData.getInputConnections(), singleNeuronData.getOutputConnections()));
            }
        }

        public List<NeuronWeightData> getWeightsList(){
            return weights;
        }

        public NeuronWeightData getWeights(int id){
            return weights.get(id);
        }

        public int[] getStructure() {
            return structure;
        }

        public record NeuronWeightData(ArrayList<Synapse> inputConnections, ArrayList<Synapse> outputConnections){}
    }

    //Information about one neuron
    public static class NeuronData {
        private Neuron neuron;
        private ArrayList<DataTypes.Synapse> inputConnections = new ArrayList<>();
        private ArrayList<DataTypes.Synapse> outputConnections = new ArrayList<>();

        public NeuronData(Neuron neuron, ArrayList<DataTypes.Synapse> inputConnections, ArrayList<DataTypes.Synapse> outputConnections){
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

        public ArrayList<DataTypes.Synapse> getInputConnections() {
            return inputConnections;
        }
        public void setInputConnections(ArrayList<DataTypes.Synapse> inputConnections) {
            this.inputConnections = inputConnections;
        }

        public ArrayList<DataTypes.Synapse> getOutputConnections() {
            return outputConnections;
        }
        public void setOutputConnections(ArrayList<DataTypes.Synapse> outputConnections) {
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
}
