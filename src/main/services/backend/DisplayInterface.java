package main.services.backend;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Main;
import main.controllers.editors.*;
import main.controllers.ui.ObjectViewerController;
import main.services.objects.Account;
import main.services.objects.Expense;
import main.services.objects.Receipt;
import main.services.objects.SalesDocument;

import java.io.IOException;
import java.util.Optional;

public class DisplayInterface {
    private static Stage stage;

    public static void main(String[] args) {
        Main.main(args);
    }

    public static void newSalesDocument(SalesDocument.SalesDocumentType type) {
        SalesDocumentEditController controller = (SalesDocumentEditController) load("salesdocumentedit", "New " + type.toString(), true);
        controller.setMode(type);
        controller.newDocument();
        stage.showAndWait();
    }

    public static void loadSalesDocument(SalesDocument document) {
        SalesDocumentEditController controller = (SalesDocumentEditController) load("salesdocumentedit", "Load " + document.getType().toString(), true);
        controller.setDocument(document);
        stage.showAndWait();
    }

    public static void newPersonalAccount() {
        load("personaledit", "New Account", false);
        stage.showAndWait();
    }

    public static void editPersonalAccount(Account account) {
        PersonalEditController controller = (PersonalEditController) load("personaledit", "Edit Account", false);
        controller.setAccount(account);
        stage.showAndWait();
    }

    public static void newNominalAccount() {
        load("nominaledit", "New Account", false);
        stage.showAndWait();
    }

    public static void editNominalAccount(Account account) {
        NominalEditController controller = (NominalEditController) load("nominaledit", "Edit Account", false);
        controller.setAccount(account);
        stage.showAndWait();
    }

    public static void newReceipt() {
        load("receiptedit", "New Receipt", false);
        stage.showAndWait();
    }

    public static void loadReceipt(Receipt receipt) {
        ReceiptEditController controller = (ReceiptEditController) load("receiptedit", "View Receipt", false);
        controller.setReceipt(receipt);
        stage.showAndWait();
    }

    public static Object selectObject(ObjectViewerController.ViewerMode defaultType, ObjectViewerController.ViewerMode... mode) {
        ObjectViewerController controller = (ObjectViewerController) load("objectviewer", "Select", false);
        controller.setMode(mode);
        controller.setType(defaultType);
        stage.showAndWait();
        return controller.getSelected();
    }

    public static void newSOA() {
        load("soa", "New Statement of Accounts", false);
        stage.showAndWait();
    }

    public static void newExpense() {
        load("expenseedit", "New Expense", false);
        stage.showAndWait();
    }

    public static void loadExpense(Expense expense) {
        ExpenseEditController controller = (ExpenseEditController) load("expenseedit", "View Expense", false);
        controller.setExpense(expense);
        stage.showAndWait();
    }

    private static Object load(String fxml, String title, boolean maximized) {
        stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("resources/fxml/" + fxml + ".fxml"));
        try {
            stage.setScene(new Scene(fxmlLoader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.getIcons().add(new Image(Main.class.getResourceAsStream("resources/images/icon.png")));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.resizableProperty().set(false);
        stage.setTitle(title);
        stage.setMaximized(maximized);
        return fxmlLoader.getController();
    }


    //Notification Functions
    public static boolean confirmDialog(ConfirmType type) {
        return confirmDialog(type.getTitle(), type.getMessage());
    }

    public static boolean confirmDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            return true;
        } else {
            return false;
        }
    }

    public static void confirmExit(ActionEvent event) {
        if (confirmDialog(ConfirmType.EXIT)) {
            exit(event);
        }
    }

    public static void exit(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    public enum ConfirmType {
        EXIT("Unsaved Data", "Are you sure you want to exit"),
        DELETE("Confirm Delete", "Are you sure you want to delete?\nThis action cannot be undone."),
        LOGIN("Incorrect Password", "Incorrect Password!\nPlease try again."),
        INCOMPLETE("Error", "Please fill in all fields!");

        private String title;
        private String message;

        ConfirmType(String title, String message) {
            this.title = title;
            this.message = message;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }
    }
}
