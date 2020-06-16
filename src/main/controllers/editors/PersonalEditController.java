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

public class PersonalEditController implements Initializable {
    @FXML
    private JFXTextField companyField;

    @FXML
    private JFXTextField phoneField;

    @FXML
    private JFXTextField addressField;

    @FXML
    private JFXTextField trnField;

    @FXML
    private JFXTextField emailField;

    @FXML
    private JFXTextField idField;

    @FXML
    private JFXComboBox<Account.PersonalType> typeComboBox;

    @FXML
    private JFXTextArea notesField;

    @FXML
    private Button deleteButton;

    public void setAccount(Account account) {
        companyField.setText(account.getCompany());
        phoneField.setText(account.getPhone());
        addressField.setText(account.getAddress());
        trnField.setText(account.getTrn());
        emailField.setText(account.getEmail());
        idField.setText(String.valueOf(account.getId()));
        typeComboBox.setValue(account.getType());
        notesField.setText(account.getNotes());
        deleteButton.setVisible(true);
    }

    @FXML
    void cancelAction(ActionEvent event) {
        DisplayInterface.confirmExit(event);
    }

    @FXML
    void saveAction(ActionEvent event) {
        try {
            makeDocument().save();
            DisplayInterface.exit(event);
        } catch (Exception e) {
            DisplayInterface.confirmDialog(DisplayInterface.ConfirmType.INCOMPLETE);
        }
    }

    @FXML
    void deleteAction(ActionEvent event) {
        if (DisplayInterface.confirmDialog(DisplayInterface.ConfirmType.DELETE)) {
            Account.delete(Integer.parseInt(idField.getText()));
            DisplayInterface.exit(event);
        }
    }

    Account makeDocument() {
        Account document = new Account();
        document.setCompany(companyField.getText());
        document.setPhone(phoneField.getText());
        document.setAddress(addressField.getText());
        document.setTrn(trnField.getText());
        document.setEmail(emailField.getText());
        document.setId(Integer.parseInt(idField.getText()));
        document.setType(typeComboBox.getValue());
        document.setDeleted(false);
        document.setNotes(notesField.getText());
        return document;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        typeComboBox.getItems().addAll(Account.PersonalType.CUSTOMER, Account.PersonalType.SUPPLIER);
        idField.setText(String.valueOf(Account.getCount() + 1));
        deleteButton.setVisible(false);
    }
}
