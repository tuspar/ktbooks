package main.controllers.editors;

import com.jfoenix.controls.*;
import com.spire.doc.FileFormat;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import main.controllers.ui.ObjectViewerController;
import main.services.backend.DisplayInterface;
import main.services.objects.Account;
import main.services.objects.SOA;
import org.controlsfx.control.Notifications;
import org.controlsfx.dialog.ExceptionDialog;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Random;
import java.util.ResourceBundle;

public class SOAController implements Initializable {

    @FXML
    private Text typeText;

    @FXML
    private JFXComboBox<String> typeSelector;

    @FXML
    private Button addButton;

    @FXML
    private JFXTextField idField;

    @FXML
    private JFXTextField nameField;

    @FXML
    private JFXDatePicker startDate;

    @FXML
    private JFXDatePicker endDate;

    @FXML
    private CheckBox unpaidOnlySelector;

    @FXML
    private CheckBox balanceSelected;

    @FXML
    private JFXTextArea noteField;

    @FXML
    private JFXSpinner loading;

    @FXML
    void cancelAction(ActionEvent event) {
        DisplayInterface.exit(event);
    }

    @FXML
    void unpaidAction() {
        boolean state = unpaidOnlySelector.isSelected();
        balanceSelected.setDisable(state);
        balanceSelected.setSelected(!state);
        if (state) {
            startDate.setValue(LocalDate.of(2018, 1, 1));
            endDate.setValue(LocalDate.now());
        } else {
            endDate.setValue(LocalDate.now());
        }
        startDate.setDisable(state);
        endDate.setDisable(state);
    }

    @FXML
    void saveAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Statement");
        fileChooser.setInitialFileName("SOA " + nameField.getText());
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Word Document", "*.docx"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
        if (file != null) {
            DisplayInterface.loading(() -> {
                if (file.getName().endsWith(".pdf")) {
                    try {
                        save(file.getAbsolutePath(), FileFormat.PDF);
                        Desktop.getDesktop().open(file);
                    } catch (IOException e) {
                        new ExceptionDialog(e).showAndWait();
                    }
                } else {
                    try {
                        save(file.getAbsolutePath(), FileFormat.Docx);
                        Desktop.getDesktop().open(file);
                    } catch (IOException e) {
                        new ExceptionDialog(e).showAndWait();
                    }
                }
            }, loading);
            Notifications.create().title("Saved").text(file.getName() + " was Saved!").show();
        }
    }

    @FXML
    void viewPDFAction() {
        DisplayInterface.loading(() -> {
            String path = new Random().nextInt() + "." + FileFormat.PDF.toString().toLowerCase();
            save(path, FileFormat.PDF);
            try {
                Desktop.getDesktop().open(new File(path));
            } catch (IOException e) {
                new ExceptionDialog(e).showAndWait();
            }
            new File(path).deleteOnExit();
        }, loading);
    }

    @FXML
    void viewWordAction() {
        DisplayInterface.loading(() -> {
            String path = new Random().nextInt() + "." + FileFormat.Docx.toString().toLowerCase();
            save(path, FileFormat.Docx);
            try {
                Desktop.getDesktop().open(new File(path));
            } catch (IOException e) {
                new ExceptionDialog(e).showAndWait();
            }
            new File(path).deleteOnExit();
        }, loading);
    }

    @FXML
    void typeChange() {
        String type = typeSelector.getValue();
        switch (type) {
            case "Account":
                addButton.setDisable(false);
                unpaidOnlySelector.setDisable(false);
                break;
            case "Sales":
            case "Purchases":
                addButton.setDisable(true);
                unpaidOnlySelector.setDisable(true);
                break;
        }
        idField.setText("");
        nameField.setText("");
    }

    @FXML
    void addAction() {
        String type = typeSelector.getValue();
        if (type.equals("Account")) {
            Account account = (Account) DisplayInterface.selectObject(ObjectViewerController.
                    ViewerMode.ALL_ACCOUNTS, ObjectViewerController.ViewerMode.ALL_ACCOUNTS);
            if (account != null) {
                idField.setText(String.valueOf(account.getId()));
                nameField.setText(account.getType() + ": " + account.getCompany());
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        typeSelector.getItems().addAll("Account", "Sales", "Purchases");
        startDate.setValue(LocalDate.of(2018, 1, 1));
        endDate.setValue(LocalDate.now());
    }

    private void save(String path, FileFormat ext) {
        switch (typeSelector.getValue()) {
            case "Account":
                if (!idField.getText().isEmpty()) {
                    SOA.accountReport(Account.load(Integer.parseInt(idField.getText())), unpaidOnlySelector.isSelected(),
                            balanceSelected.isSelected(), noteField.getText(), startDate.getValue(), endDate.getValue(), path, ext);
                } else {
                    DisplayInterface.confirmDialog(DisplayInterface.ConfirmType.INCOMPLETE);
                }
                break;
            case "Sales":
                SOA.salesReport(balanceSelected.isSelected(), noteField.getText(), startDate.getValue(), endDate.getValue(), path, ext);
                break;
            case "Purchases":
                SOA.purchaseReport(balanceSelected.isSelected(), noteField.getText(), startDate.getValue(), endDate.getValue(), path, ext);
                break;
        }
    }

}
