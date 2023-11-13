package anna.ui.tabs.modules;

import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

//Class for connection between ConfusionMatrixSingle.fxml and other UIControllers
public class UIClassMatrixController {
    public Label truePositiveLabel, trueNegativeLabel, falsePositiveLabel, falseNegativeLabel;
    public ChoiceBox<String> matrixClassSelector;
    public UIClassMatrixController(){}

    public void clear(){
        truePositiveLabel.setText("...");
        trueNegativeLabel.setText("...");
        falsePositiveLabel.setText("...");
        falseNegativeLabel.setText("...");
        matrixClassSelector.getItems().clear();
    }
}
