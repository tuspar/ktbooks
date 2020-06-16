package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("resources/fxml/login.fxml"))));
        stage.getIcons().add(new Image(getClass().getResourceAsStream("resources/images/icon.png")));
        stage.resizableProperty().set(false);
        stage.setTitle("Kanchans Traders");
        stage.show();
    }

}
