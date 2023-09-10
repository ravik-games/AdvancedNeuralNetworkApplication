package ANNA.UI.tabs.modules;

import javafx.scene.layout.GridPane;

//Class for connection between ConfusionMatrixFull.fxml and other UIControllers
public class UIFullMatrixController {
    public GridPane matrixGrid;
    public UIFullMatrixController(){}

    public void clear(){
        matrixGrid.getChildren().clear();
        matrixGrid.getRowConstraints().clear();
        matrixGrid.getColumnConstraints().clear();
    }
}
