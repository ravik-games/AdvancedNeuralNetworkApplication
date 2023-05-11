package ANNA.Network;

import ANNA.Functions.ActivationFunctions;
import ANNA.Network.neurons.BasicNeuron;
import ANNA.Network.neurons.BiasNeuron;
import ANNA.Network.neurons.Neuron;
import ANNA.UI.PopupController;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public NetworkStructure(DataTypes.NetworkData networkData){
        abortRun = !setStructure(networkData);
    }

    /**
     * @param networkData Class WeightsData. Contains all information about initial weights and network structure (represents number of neurons in each layer).
     */
    public boolean setStructure(DataTypes.NetworkData networkData){
        //Clear previous values
        structure.clear();
        neuronsList.clear();

        if (networkData == null){
            PopupController.errorMessage("ERROR", "Произошла ошибка при инициализации структуры", "", "Произошла ошибка при инициализации структуры нейронной сети. Отсутствует информация о строении сети.");
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "An error occurred during initialization of the neural network structure. There is no information about the network structure.");
            return false;
        }

        //Initialize new neurons
        for(int i = 0; i < networkData.getStructure().length; i++) {

            ArrayList<Neuron> layer = new ArrayList<>(networkData.getStructure().length);
            //Create new neurons in layer
            for (int j = 0; j < networkData.getStructure()[i]; j++) {
                neuronTypes neuronType;
                ActivationFunctions.types activationFunction;

                if (i == 0) { //Input layer
                    neuronType = neuronTypes.INPUT;
                    activationFunction = ActivationFunctions.types.LINEAR;
                }
                else if (i == networkData.getStructure().length - 1) {//Output layer
                    neuronType = neuronTypes.OUTPUT;
                    activationFunction = ActivationFunctions.types.SIGMOID;
                }
                else { //Hidden layers
                    neuronType = neuronTypes.HIDDEN;
                    activationFunction = ActivationFunctions.types.SIGMOID;
                }

                layer.add(initializeNeuron(neuronType, activationFunction));
            }
            structure.add(layer);
        }

        //Create synapses
        //Using pre-defined weights if we can, else random
        if (networkData.getWeightsList() != null && networkData.getWeightsList().size() != 0){
            for (int i = 0; i < networkData.getWeightsList().size(); i++) {
                //Set input connections
                neuronsList.get(i).setInputConnections(networkData.getWeights(i).inputConnections());
                //Set output connections
                neuronsList.get(i).setOutputConnections(networkData.getWeights(i).outputConnections());
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
    private Neuron initializeNeuron(neuronTypes type, ActivationFunctions.types activationFunction){
        //Create new neuron and set it ID
        Neuron neuron = new BasicNeuron(neuronsList.size(), activationFunction, type);
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

    public enum neuronTypes{
        INPUT, HIDDEN, OUTPUT
    }
}
