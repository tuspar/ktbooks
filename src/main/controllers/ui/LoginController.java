package main.controllers.ui;


import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import main.Main;
import main.services.backend.Database;
import main.services.backend.DisplayInterface;

import java.io.IOException;

public class LoginController {
    @FXML
    private JFXTextField usernameField;

    @FXML
    private JFXPasswordField passwordField;

    @FXML
    void cancelAction(ActionEvent event) {
        DisplayInterface.confirmExit(event);
    }

    @FXML
    void loginAction(ActionEvent event) throws IOException {
        if (usernameField.getText().equals("") && passwordField.getText().equals("")) {
            Database.getInstance();

            Stage stage = new Stage();
            stage.setScene(new Scene(FXMLLoader.load(Main.class.getResource("resources/fxml/home.fxml"))));
            stage.getIcons().add(new Image(Main.class.getResource("resources/images/icon.png").toString()));
            stage.setMaximized(true);
            stage.setTitle("Kanchans Traders");
            stage.show();
            DisplayInterface.exit(event);
        } else {
            if (!DisplayInterface.confirmDialog(DisplayInterface.ConfirmType.LOGIN)) {
                DisplayInterface.exit(event);
            }
        }
    }

}
