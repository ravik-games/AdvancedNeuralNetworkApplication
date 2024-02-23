package anna.network;

import anna.math.ActivationFunctions;
import anna.network.data.DataTypes;
import anna.network.data.Hyperparameters;
import anna.network.neurons.BasicNeuron;
import anna.network.neurons.BiasNeuron;
import anna.network.neurons.Neuron;
import anna.ui.PopupController;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetworkStructure {
    // Class, containing data about neurons and network structure

    private static final ResourceBundle bundle = ResourceBundle.getBundle("fxml/bindings/Localization", Locale.getDefault()); // Get current localization

    // Signal variable. Shows if network needs to be aborted
    public boolean abortRun;

    // List of full neuron data by id
    private final ArrayList<DataTypes.NeuronData> neuronsList = new ArrayList<>();

    // Structure of neural network
    // First column contains input neurons, last contains output and between all is a hidden neurons
    private final ArrayList<ArrayList<Neuron>> structure = new ArrayList<>();


    // Initialize structure
    public NetworkStructure(DataTypes.NetworkData networkData){
        abortRun = !setStructure(networkData);
    }

    /**
     * @param networkData Class WeightsData. Contains all information about initial weights and network structure (represents number of neurons in each layer).
     */
    public boolean setStructure(DataTypes.NetworkData networkData){
        // Clear previous values
        structure.clear();
        neuronsList.clear();

        if (networkData == null){
            PopupController.errorMessage("ERROR", "", bundle.getString("logger.error.structureInitialization"));
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "An error occurred during initialization of the neural network structure. There is no information about the network structure.");
            return false;
        }

        // Initialize new neurons
        for(int i = 0; i < networkData.getStructure().size(); i++) {

            ArrayList<Neuron> layer = new ArrayList<>(networkData.getStructure().size());
            //Create new neurons in layer

            LayerTypes layerType = networkData.getStructure().get(i).type();
            ActivationFunctions.Types activationFunction = networkData.getStructure().get(i).activationFunction();

            for (int j = 0; j < networkData.getStructure().get(i).neuronNumber(); j++) {
                // Initialize and add neurons
                layer.add(initializeNeuron(layerType, activationFunction, false));
            }

            // Add bias neuron if this hyperparameter selected
            if (Hyperparameters.USE_BIAS_NEURONS)
                // Limit bias to some layers only
                switch (layerType) {
                    case INPUT, HIDDEN -> layer.add(initializeNeuron(layerType, activationFunction, true));
                }
            structure.add(layer);
        }

        // Create synapses
        // Using pre-defined weights if we can, else random
        if (networkData.getWeightsList() != null && !networkData.getWeightsList().isEmpty()){
            for (int i = 0; i < networkData.getWeightsList().size(); i++) {
                // Set input connections
                neuronsList.get(i).setInputConnections(networkData.getWeights(i).inputConnections());
                // Set output connections
                neuronsList.get(i).setOutputConnections(networkData.getWeights(i).outputConnections());
            }
            return true;
        }

        for (int i = 0; i < structure.size() - 1; i++) {
            for (int j = 0; j < structure.get(i).size(); j++) {
                // Getting next row of neurons
                for (int k = 0; k < structure.get(i + 1).size(); k++) {
                    double weight;
                    int firstID = getIDByPosition(i, j);
                    int secondID = getIDByPosition(i + 1, k);

                    if (getNeuronByID(secondID) instanceof BiasNeuron)
                        continue; //Skip bias neurons

                    weight = switch (Hyperparameters.NETWORK_WEIGHT_INITIALIZATION) {
                        case RANDOM -> Math.random() * 2 - 1;
                        case ALL_ONES -> 1;
                        case ALL_HALVES -> 0.5;
                    };
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

    // Initialize single neuron and add it to the arrays
    private Neuron initializeNeuron(LayerTypes layerType, ActivationFunctions.Types activationFunction, boolean isBias){
        // Create new neuron and set it ID
        Neuron neuron;
        if (isBias)
            neuron = new BiasNeuron(neuronsList.size(), activationFunction, layerType);
        else {
            neuron = switch (layerType) {
                case INPUT, HIDDEN, OUTPUT -> new BasicNeuron(neuronsList.size(), activationFunction, layerType);
            };
        }

        // Add to the array
        neuronsList.add(new DataTypes.NeuronData(neuron));
        return neuron;
    }

    // Getters
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

    public long getOverallNeuronsAmount() { return structure.stream().mapToLong(List::size).sum();}

    public void printWeights(boolean delta){
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < neuronsList.size(); i++) {
            DataTypes.NeuronData currentNeuron = neuronsList.get(i);
            str.append("Neuron ").append(i).append(" (").append(currentNeuron.getNeuron().toString()).append(", ")
                    .append(currentNeuron.getNeuron().getType().toString())
                    .append(currentNeuron.getNeuron().isBias() ? ", Bias" : "").append(")\t");
            str.append("Input: ");
            for (int j = 0; j < currentNeuron.getInputConnections().size(); j++) {
                str.append("(").append(currentNeuron.getInputConnections().get(j).getNeuronID()).append("; ");
                if(delta)
                    str.append(currentNeuron.getInputConnections().get(j).getDeltaWeight());
                else
                    str.append(currentNeuron.getInputConnections().get(j).getWeight());
                str.append(")");
                if(j != currentNeuron.getInputConnections().size() - 1)
                    str.append(", ");
                else
                    str.append("\t");
            }
            str.append("Output: ");
            for (int j = 0; j < currentNeuron.getOutputConnections().size(); j++) {
                str.append("(").append(currentNeuron.getOutputConnections().get(j).getNeuronID()).append("; ");
                if(delta)
                    str.append(currentNeuron.getOutputConnections().get(j).getDeltaWeight());
                else
                    str.append(currentNeuron.getOutputConnections().get(j).getWeight());
                str.append(")");
                if(j != currentNeuron.getOutputConnections().size() - 1)
                    str.append(", ");
                else
                    str.append("\t");
            }
            str.append("\n");
        }
        System.out.println(str);
    }

    public enum LayerTypes {
        INPUT, HIDDEN, OUTPUT
    }
}
