// AdminController.java
package com.group18.controller.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.Node;
import java.io.IOException;

public class AdminController {
    @FXML
    private BorderPane root;

    @FXML
    private Node content;

    @FXML
    private AdminSidebarController sidebarController;

    @FXML
    private void initialize() {
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