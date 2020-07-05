package main.controllers.editors;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXSpinner;
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
    private JFXTextField receiverIdField;

    @FXML
    private JFXTextField recieverField;

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

    @FXML
    private Button cancelButton;

    @FXML
    private JFXSpinner loadingDocument;

    @FXML
    private JFXSpinner postingDocument;

    public void setReceipt(Receipt receipt) {
        DisplayInterface.loading(() -> {
            postButton.setDisable(true);
            cancelButton.setDisable(true);
            idField.setText(String.valueOf(receipt.getId()));
            dateField.setValue(receipt.getDate());
            descriptionField.setText(receipt.getDescription());
            customerIdField.setText(String.valueOf(receipt.getCredit().getId()));
            companyField.setText(receipt.getCredit().getCompany());
            selectionTable.getItems().clear();
            selectedTable.getItems().clear();
            selectionTable.getItems().addAll(Receipt.getUnpaidBills(receipt.getCredit()));
            selectedTable.getItems().addAll(receipt.getPaid());
            receiverIdField.setText(String.valueOf(receipt.getDebit().getId()));
            recieverField.setText(receipt.getDebit().getCompany());
            paidField.setText(receipt.getTotal());
            if (receipt.getId() <= Receipt.getCount()) {
                deleteButton.setVisible(true);
            }
            postButton.setDisable(false);
            cancelButton.setDisable(false);
        });
        update();
    }

    @FXML
    void addCustomerAction() {
        Account account = (Account) DisplayInterface.selectObject(ObjectViewerController.ViewerMode.CUSTOMER, ObjectViewerController.ViewerMode.ALL_ACCOUNTS);
        if (account != null) {
            DisplayInterface.loading(() -> {
                customerIdField.setText(String.valueOf(account.getId()));
                companyField.setText(account.getCompany());
                selectionTable.getItems().clear();
                selectedTable.getItems().clear();
                selectionTable.getItems().addAll(Receipt.getUnpaidBills(account));
            });
        }
        update();
    }

    @FXML
    void addReceiverAction() {
        Account account = (Account) DisplayInterface.selectObject(ObjectViewerController.ViewerMode.ASSET, ObjectViewerController.ViewerMode.ALL_ACCOUNTS);
        if (account != null) {
            DisplayInterface.loading(() -> {
                receiverIdField.setText(String.valueOf(account.getId()));
                recieverField.setText(account.getCompany());
            });
        }
        update();
    }

    @FXML
    void cancelAction(ActionEvent event) {
        DisplayInterface.confirmExit(event);
    }

    @FXML
    void postReceipt() {
        update();
        boolean valid = customerIdField.validate() && receiverIdField.validate() && paidField.validate();
        if (valid && dateField.getValue() != null) {
            if (Double.parseDouble(paidField.getText()) != 0) {
                if (Double.parseDouble(paidField.getText()) == Double.parseDouble(selectedField.getText())) {
                    DisplayInterface.loading(() -> {
                        cancelButton.setDisable(true);
                        for (SalesDocument doc : selectedTable.getItems()) {
                            doc.setPaid(true);
                            doc.save();
                        }

                        for (SalesDocument doc : selectionTable.getItems()) {
                            doc.setPaid(false);
                            doc.save();
                        }
                        makeReceipt().save();
                        cancelButton.setDisable(false);
                    }, postingDocument);
                    DisplayInterface.notification("Posted Receipt", "Receipt for " + companyField.getText() + " posted");
                } else {
                    DisplayInterface.confirmDialog("Error", "Payment value and invoice value must match");
                }
            } else {
                DisplayInterface.confirmDialog("Error", "Receipt value must be greater than 0");
            }
        }
    }

    @FXML
    void removeAll() {
        selectionTable.getItems().addAll(selectedTable.getItems());
        selectedTable.getItems().clear();
        update();
    }

    @FXML
    void removeOne() {
        if (selectedTable.getSelectionModel().getSelectedIndex() != -1) {
            selectionTable.getItems().add(selectedTable.getSelectionModel().getSelectedItem());
            selectedTable.getItems().remove(selectedTable.getSelectionModel().getSelectedIndex());
            update();
        } else {
            DisplayInterface.confirmDialog(DisplayInterface.ConfirmType.SELECT_ROW);
        }
    }

    @FXML
    void saveAsAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Receipt");
        fileChooser.setInitialFileName("Receipt" + idField.getText());
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Word Document", "*.docx"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = fileChooser.showSaveDialog(selectedTable.getScene().getWindow());
        if (file != null) {
            DisplayInterface.loading(() -> {
                if (file.getName().endsWith(".pdf")) {
                    try {
                        makeReceipt().generateReceipt(file.getAbsolutePath(), FileFormat.PDF);
                        Desktop.getDesktop().open(file);
                    } catch (IOException e) {
                        new ExceptionDialog(e).showAndWait();
                    }
                } else if (file.getName().endsWith(".docx")) {
                    try {
                        makeReceipt().generateReceipt(file.getAbsolutePath(), FileFormat.Docx);
                        Desktop.getDesktop().open(file);
                    } catch (IOException e) {
                        new ExceptionDialog(e).showAndWait();
                    }
                }
            }, loadingDocument);
            Notifications.create().title("Saved").text(file.getName() + " was Saved!").show();
        }
    }

    @FXML
    void selectAll() {
        selectedTable.getItems().addAll(selectionTable.getItems());
        selectionTable.getItems().clear();
        update();
    }

    @FXML
    void selectOne() {
        if (selectionTable.getSelectionModel().getSelectedIndex() != -1) {
            selectedTable.getItems().add(selectionTable.getSelectionModel().getSelectedItem());
            selectionTable.getItems().remove(selectionTable.getSelectionModel().getSelectedIndex());
            update();
        } else {
            DisplayInterface.confirmDialog(DisplayInterface.ConfirmType.SELECT_ROW);
        }
    }

    @FXML
    void viewPDFAction() {
        update();
        DisplayInterface.loading(() -> makeReceipt().view(FileFormat.PDF), loadingDocument);
    }

    @FXML
    void viewWordAction() {
        update();
        DisplayInterface.loading(() -> makeReceipt().view(FileFormat.Docx), loadingDocument);
    }

    @FXML
    void amountChangeAction() {
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

        customerIdField.getValidators().add(DisplayInterface.getRequiredValidator());
        receiverIdField.getValidators().add(DisplayInterface.getRequiredValidator());
        paidField.getValidators().add(DisplayInterface.getRequiredValidator());
    }

    private void update() {
        double total = 0;
        for (SalesDocument document : selectedTable.getItems()) {
            total += Double.parseDouble(document.getTotal());
        }
        selectedField.setText(String.valueOf(total));
    }

    private Receipt makeReceipt() {
        Receipt receipt = new Receipt();
        receipt.setId(Integer.parseInt(idField.getText()));
        receipt.setDate(dateField.getValue());
        receipt.setDescription(descriptionField.getText());
        receipt.setCredit(Account.load(Integer.parseInt(customerIdField.getText())));
        receipt.setDebit(Account.load(Integer.parseInt(receiverIdField.getText())));
        receipt.setDeleted(false);

        ArrayList<SalesDocument> list = new ArrayList<>(selectedTable.getItems());
        receipt.setPaid(list);
        return receipt;
    }
}
