package main.controllers.editors;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.spire.doc.FileFormat;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import main.controllers.ui.ObjectViewerController;
import main.services.backend.DisplayInterface;
import main.services.objects.Account;
import main.services.objects.Receipt;
import main.services.objects.SalesDocument;
import org.controlsfx.control.Notifications;
import org.controlsfx.dialog.ExceptionDialog;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ReceiptEditController implements Initializable {
    @FXML
    private JFXTextField customerIdField;

    @FXML
    private JFXTextField idField;

    @FXML
    private JFXTextField companyField;

    @FXML
    private JFXTextArea descriptionField;

    @FXML
    private JFXDatePicker dateField;

    @FXML
    private JFXTextField paidField;

    @FXML
    private JFXTextField selectedField;

    @FXML
    private TableView<SalesDocument> selectionTable;

    @FXML
    private TableColumn<String, SalesDocument> idSelectColumn;

    @FXML
    private TableColumn<String, SalesDocument> dateSelectColumn;

    @FXML
    private TableColumn<String, SalesDocument> amountSelectColumn;

    @FXML
    private TableView<SalesDocument> selectedTable;

    @FXML
    private TableColumn<String, SalesDocument> idSelectedColumn;

    @FXML
    private TableColumn<String, SalesDocument> dateSelectedColumn;

    @FXML
    private TableColumn<String, SalesDocument> amountSelectedColumn;

    @FXML
    private Button postButton;

    @FXML
    private Button deleteButton;

    public void setReceipt(Receipt receipt) {
        idField.setText(String.valueOf(receipt.getId()));
        dateField.setValue(receipt.getDate());
        descriptionField.setText(receipt.getDescription());
        customerIdField.setText(String.valueOf(receipt.getAccount().getId()));
        companyField.setText(receipt.getAccount().getCompany());
        paidField.setText(receipt.getTotal());
        selectionTable.getItems().clear();
        selectedTable.getItems().clear();
        selectedTable.getItems().addAll(receipt.getPaid());
        selectionTable.getItems().addAll(Receipt.getUnpaidBills(receipt.getAccount()));
        update();
    }

    @FXML
    void addCustomerAction(ActionEvent event) {
        Account account = (Account) DisplayInterface.selectObject(ObjectViewerController.ViewerMode.CUSTOMER, ObjectViewerController.ViewerMode.ALL_ACCOUNTS);
        if (account != null) {
            customerIdField.setText(String.valueOf(account.getId()));
            companyField.setText(account.getCompany());
        }
        selectionTable.getItems().clear();
        selectedTable.getItems().clear();
        selectionTable.getItems().addAll(Receipt.getUnpaidBills(account));
    }

    @FXML
    void cancelAction(ActionEvent event) {
        DisplayInterface.confirmExit(event);
    }

    @FXML
    void postReceipt(ActionEvent event) {
        for (SalesDocument doc : selectedTable.getItems()) {
            doc.setPaid(true);
            doc.save();
        }

        for (SalesDocument doc : selectionTable.getItems()) {
            doc.setPaid(false);
            doc.save();
        }

        makeReceipt().save();
    }

    @FXML
    void removeAll(ActionEvent event) {
        selectionTable.getItems().addAll(selectedTable.getItems());
        selectedTable.getItems().clear();
        update();
    }

    @FXML
    void removeOne(ActionEvent event) {
        if (selectedTable.getSelectionModel().getSelectedIndex() != -1) {
            selectionTable.getItems().add(selectedTable.getSelectionModel().getSelectedItem());
            selectedTable.getItems().remove(selectedTable.getSelectionModel().getSelectedIndex());
            update();
        }
    }

    @FXML
    void saveAsAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Receipt");
        fileChooser.setInitialFileName("Receipt" + idField.getText());
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Word Document", "*.docx"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = fileChooser.showSaveDialog(selectedTable.getScene().getWindow());
        if (file.getName().endsWith(".pdf")) {
            try {
                makeReceipt().generateReceipt(file.getAbsolutePath(), FileFormat.PDF);
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                new ExceptionDialog(e).showAndWait();
            }
        } else {
            try {
                makeReceipt().generateReceipt(file.getAbsolutePath(), FileFormat.Docx);
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                new ExceptionDialog(e).showAndWait();
            }
        }
        Notifications.create().title("Saved").text(file.getName() + " was Saved!").show();

    }

    @FXML
    void selectAll(ActionEvent event) {
        selectedTable.getItems().addAll(selectionTable.getItems());
        selectionTable.getItems().clear();
        update();
    }

    @FXML
    void selectOne(ActionEvent event) {
        if (selectionTable.getSelectionModel().getSelectedIndex() != -1) {
            selectedTable.getItems().add(selectionTable.getSelectionModel().getSelectedItem());
            selectionTable.getItems().remove(selectionTable.getSelectionModel().getSelectedIndex());
            update();
        }
    }

    @FXML
    void viewPDFAction(ActionEvent event) {
        update();
        makeReceipt().view(FileFormat.PDF);
    }

    @FXML
    void viewWordAction(ActionEvent event) {
        update();
        makeReceipt().view(FileFormat.Docx);
    }

    @FXML
    void amountChangeAction(ActionEvent event) {
        update();
    }

    @FXML
    void deleteAction(ActionEvent event) {
        if (DisplayInterface.confirmDialog(DisplayInterface.ConfirmType.DELETE)) {
            Receipt.delete(Integer.parseInt(idField.getText()));
            DisplayInterface.exit(event);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idSelectColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateSelectColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        amountSelectColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        idSelectedColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateSelectedColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        amountSelectedColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

        idField.setText(String.valueOf(Receipt.getCount() + 1));
        dateField.setValue(LocalDate.now());
    }

    private void update() {
        double total = 0;
        for (SalesDocument document : selectedTable.getItems()) {
            total += Double.parseDouble(document.getTotal());
        }
        selectedField.setText(String.valueOf(total));
        postButton.setDisable(!(Double.parseDouble(paidField.getText()) == Double.parseDouble(selectedField.getText())));
    }

    private Receipt makeReceipt() {
        Receipt receipt = new Receipt();
        receipt.setId(Integer.parseInt(idField.getText()));
        receipt.setDate(dateField.getValue());
        receipt.setDescription(descriptionField.getText());
        receipt.setAccount(Account.load(Integer.parseInt(customerIdField.getText())));
        receipt.setDeleted(false);

        ArrayList<SalesDocument> list = new ArrayList<>();
        for (SalesDocument invoice : selectedTable.getItems()) {
            list.add(invoice);
        }
        receipt.setPaid(list);
        return receipt;
    }

}
