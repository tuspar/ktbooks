package main.controllers.editors;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import main.controllers.ui.ObjectViewerController;
import main.services.backend.DisplayInterface;
import main.services.objects.Account;
import main.services.objects.Expense;

import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ExpenseEditController implements Initializable {
    private static final String decimalFormat = "#0.00";
    @FXML
    private JFXTextField supplierIDField;
    @FXML
    private JFXTextField supplierField;
    @FXML
    private JFXTextArea noteField;
    @FXML
    private JFXTextField subtotalField;
    @FXML
    private JFXTextField vatField;
    @FXML
    private JFXTextField totalField;
    @FXML
    private Button deleteButton;
    @FXML
    private JFXDatePicker dateField;
    @FXML
    private JFXTextField idField;

    public void setExpense(Expense expense) {
        supplierField.setText(expense.getAccount().getCompany());
        supplierIDField.setText(String.valueOf(expense.getAccount().getId()));
        dateField.setValue(expense.getDate());
        noteField.setText(expense.getDescription());
        idField.setText(String.valueOf(expense.getId()));
        totalField.setText(expense.getAmount());
        totalChanged(null);
        deleteButton.setVisible(true);
    }

    @FXML
    void addSupplierAction(ActionEvent event) {
        Account account = (Account) DisplayInterface.selectObject(ObjectViewerController.ViewerMode.SUPPLIER, ObjectViewerController.ViewerMode.ALL_ACCOUNTS);

        if (account != null) {
            supplierIDField.setText(String.valueOf(account.getId()));
            supplierField.setText(account.getCompany());
        }

    }

    @FXML
    void cancelAction(ActionEvent event) {
        DisplayInterface.confirmExit(event);
    }

    @FXML
    void deleteAction(ActionEvent event) {
        if (DisplayInterface.confirmDialog(DisplayInterface.ConfirmType.DELETE)) {
            Expense.delete(Integer.parseInt(idField.getText()));
            DisplayInterface.exit(event);
        }
    }

    @FXML
    void saveAction(ActionEvent event) {
        Expense expense = new Expense();
        expense.setAccount(Account.load(Integer.parseInt(supplierIDField.getText())));
        expense.setDeleted(false);
        expense.setDate(dateField.getValue());
        expense.setDescription(noteField.getText());
        expense.setAmount(totalField.getText());
        expense.setId(Integer.parseInt(idField.getText()));
        expense.save();

    }

    @FXML
    void subtotalChanged(KeyEvent event) {
        if (!subtotalField.getText().isEmpty()) {
            double subtotal = Double.parseDouble(subtotalField.getText());
            vatField.setText(new DecimalFormat(decimalFormat).format(subtotal * 0.05));
            totalField.setText(new DecimalFormat(decimalFormat).format(subtotal * 1.05));
        } else {
            vatField.setText("");
            totalField.setText("");
        }
    }

    @FXML
    void totalChanged(KeyEvent event) {
        if (!totalField.getText().isEmpty()) {
            double total = Double.parseDouble(totalField.getText());
            subtotalField.setText(new DecimalFormat(decimalFormat).format(total / 1.05));
            vatField.setText(new DecimalFormat(decimalFormat).format((total / 1.05) * 0.05));
        } else {
            subtotalField.setText("");
            totalField.setText("");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idField.setText(String.valueOf(Expense.getCount() + 1));
        dateField.setValue(LocalDate.now());
        deleteButton.setVisible(false);
    }
}
