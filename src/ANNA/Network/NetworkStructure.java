package ANNA.Network;

import ANNA.Functions.ActivationFunctions;
import ANNA.Network.neurons.BasicNeuron;
import ANNA.Network.neurons.BiasNeuron;
import ANNA.Network.neurons.Neuron;
import ANNA.UI.PopupController;

import java.util.ArrayList;
import java.util.List;

public class NetworkStructure {
    //Class, containing data about neurons and network structure

    //Signal variable. Shows if network needs to be aborted
    public boolean abortRun;

    //List of full neuron data by id
    private final ArrayList<DataTypes.NeuronData> neuronsList = new ArrayList<>();

    //Structure of neural network
    //First column contains input neurons, last contains output and between all is a hidden neurons
    private final ArrayList<ArrayList<Neuron>> structure = new ArrayList<>();


    //Initialize structure
    public NetworkStructure(int[] structure, DataTypes.NetworkData weights){
        abortRun = !setStructure(structure, weights);
    }

    /**
     * @param newStructure Array of Integers. Represents number of neurons in each layer.
     * @param weights Class WeightsData. Contains all information about initial weights.
     */
    public boolean setStructure(int[] newStructure, DataTypes.NetworkData weights){
        //Clear previous values
        structure.clear();
        neuronsList.clear();

        //Initialize new neurons
        for(int i = 0; i < newStructure.length; i++) {

            ArrayList<Neuron> layer = new ArrayList<>(newStructure.length);
            //Create new neurons in layer
            for (int j = 0; j < newStructure[i]; j++) {
                neuronTypes neuronType;
                ActivationFunctions.types activationFunction;

                if (i == 0) { //Input layer
                    neuronType = neuronTypes.INPUT;
                    activationFunction = ActivationFunctions.types.LINEAR;
                }
                else if (i == newStructure.length - 1) {//Output layer
                    neuronType = neuronTypes.OUTPUT;
                    activationFunction = ActivationFunctions.types.SIGMOID;
                }
                else { //Hidden layers
                    neuronType = neuronTypes.HIDDEN;
                    activationFunction = ActivationFunctions.types.SIGMOID;
                }

                layer.add(initializeNeuron(i, j, neuronType, activationFunction));
            }
            structure.add(layer);
        }

        //Create synapses
        //Using pre-defined weights if we can, else random
        if (weights != null){
            if(weights.getStructure() != newStructure){
                PopupController.errorMessage("ERROR", "Ошибка при загрузке весов", "", "Обнаружено расхождение текущей структуры нейронной сети и структуры загружаемых весов.");
                System.err.println("WARNING: There is a conflict between the current structure of the neural network and the structure of the weights to be loaded.");
                return false;
            }
            if (weights.getWeightsList() == null)
                return false;
            for (int i = 0; i < weights.getWeightsList().size(); i++) {
                //Set input connections
                neuronsList.get(i).setInputConnections(weights.getWeights(i).inputConnections());
                //Set output connections
                neuronsList.get(i).setOutputConnections(weights.getWeights(i).outputConnections());
            }
            return true;
        }

        for (int i = 0; i < structure.size() - 1; i++) {
            for (int j = 0; j < structure.get(i).size(); j++) {
                //Getting next rows neurons
                for (int k = 0; k < structure.get(i + 1).size(); k++) {
                    double weight;
                    int firstID = getIDByPosition(i, j);
                    int secondID = getIDByPosition(i + 1, k);

                    if (getNeuronByID(secondID) instanceof BiasNeuron)
                        continue; //Skip bias neurons

                    //weight = 0.5;
                    weight = Math.random() * 2 - 1;
                    createSynapse(firstID, secondID, weight);
                }
            }
        }

        return true;
    }

    public void createSynapse(int firstID, int secondID, double initialWeight){
        neuronsList.get(firstID).getOutputConnections().add(new DataTypes.Synapse(secondID, initialWeight, 0));
        neuronsList.get(secondID).getInputConnections().add(new DataTypes.Synapse(firstID, initialWeight, 0));
    }

    //Initialize single neuron and add it to the arrays
    private Neuron initializeNeuron(int positionY, int positionX, neuronTypes type, ActivationFunctions.types activationFunction){
        //Create new neuron and set it ID
        Neuron neuron = new BasicNeuron(positionX, positionY, neuronsList.size(), activationFunction, type);
        //Add to the array
        neuronsList.add(new DataTypes.NeuronData(neuron));
        //neuronsArray.add(value);
        return neuron;
    }

    //Getters
    public int getIDByPosition(int x, int y){
        return getNeuronByPosition(x, y).getId();
    }

    public Neuron getNeuronByID(int id){
        return neuronsList.get(id).getNeuron();
    }
    public Neuron getNeuronByPosition(int x, int y){
        return structure.get(x).get(y);
    }

    public ArrayList<DataTypes.Synapse> getInputConnections(int neuronID){
        return neuronsList.get(neuronID).getInputConnections();
    }
    public ArrayList<DataTypes.Synapse> getOutputConnections(int neuronID){
        return neuronsList.get(neuronID).getOutputConnections();
    }

    public int getLayersAmount(){
        return structure.size();
    }
    public int getNeuronsAmountInLayer(int layer){
        return structure.get(layer).size();
    }

    public void printWeights(boolean delta){
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < neuronsList.size(); i++) {
            str.append("Neuron ").append(i).append(" (").append(neuronsList.get(i).getNeuron().toString()).append(")\t");
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

    public List<DataTypes.NeuronData> getRawWeightsData(){
        return neuronsList;
    }

    public enum neuronTypes{
        INPUT, HIDDEN, OUTPUT
    }
}
