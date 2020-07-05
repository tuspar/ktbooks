package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Arrays;

public class Main extends Application {

    public static void main(String[] args) {
        System.out.println(Arrays.toString(args));
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("status.fxml"))));
        stage.resizableProperty().set(false);
        stage.setTitle("Updating");
        stage.show();
    }
}
