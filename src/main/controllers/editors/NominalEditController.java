package main.controllers.editors;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import main.services.backend.DisplayInterface;
import main.services.objects.Account;

import java.net.URL;
import java.util.ResourceBundle;

public class NominalEditController implements Initializable {
    @FXML
    private JFXTextField nameField;

    @FXML
    private JFXTextArea notesField;

    @FXML
    private JFXTextField idField;

    @FXML
    private JFXComboBox<Account.PersonalType> typeComboBox;

    @FXML
    private Button deleteButton;

    public void setAccount(Account account) {
        nameField.setText(account.getCompany());
        idField.setText(String.valueOf(account.getId()));
        notesField.setText(account.getNotes());
        typeComboBox.setValue(account.getType());
        deleteButton.setVisible(true);
    }

    @FXML
    void cancelAction(ActionEvent event) {
        DisplayInterface.confirmExit(event);
    }

    @FXML
    void deleteAction(ActionEvent event) {
        if (DisplayInterface.confirmDialog(DisplayInterface.ConfirmType.DELETE)) {
            Account.delete(Integer.parseInt(idField.getText()));
            DisplayInterface.exit(event);
        }
    }

    @FXML
    void saveAction(ActionEvent event) {
        try {
            Account account = new Account();
            account.setCompany(nameField.getText());
            account.setNotes(notesField.getText());
            account.setId(Integer.parseInt(idField.getText()));
            account.setType(typeComboBox.getValue());
            account.setDeleted(false);
            account.save();
            DisplayInterface.exit(event);
        } catch (Exception e) {
            DisplayInterface.confirmDialog(DisplayInterface.ConfirmType.INCOMPLETE);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        typeComboBox.getItems().setAll(Account.PersonalType.values());
        typeComboBox.getItems().removeAll(Account.PersonalType.CUSTOMER, Account.PersonalType.SUPPLIER);
        idField.setText(String.valueOf(Account.getCount() + 1));
        deleteButton.setVisible(false);
    }
}
