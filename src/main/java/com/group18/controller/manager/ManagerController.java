// ManagerController.java
package com.group18.controller.manager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.Node;
import java.io.IOException;

public class ManagerController {
    @FXML
    private BorderPane root;

    @FXML
    private Node content;

    @FXML
    private ManagerSidebarController sidebarController; // Match the fx:id from FXML

    @FXML
    private void initialize() {
        // Just set the main controller directly
        sidebarController.setMainController(this);
    }

    public void switchContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node newContent = loader.load();
            root.setCenter(newContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}