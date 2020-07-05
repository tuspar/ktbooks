package main.controllers.editors;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import main.controllers.ui.ObjectViewerController;
import main.services.backend.DisplayInterface;
import main.services.backend.Settings;
import main.services.objects.Account;
import main.services.objects.Expense;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ExpenseEditController implements Initializable {
    @FXML
    private JFXTextField expenseIdField;

    @FXML
    private JFXTextField expenseField;

    @FXML
    private JFXTextField paymentIdField;

    @FXML
    private JFXTextField paymentField;

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
        expenseField.setText(expense.getDebit().getCompany());
        expenseIdField.setText(String.valueOf(expense.getDebit().getId()));
        paymentField.setText(expense.getCredit().getCompany());
        paymentField.setText(String.valueOf(expense.getCredit().getId()));
        dateField.setValue(expense.getDate());
        noteField.setText(expense.getDescription());
        idField.setText(String.valueOf(expense.getId()));
        totalField.setText(expense.getAmount());
        totalChanged();
        if (expense.getId() <= Expense.getCount()) {
            deleteButton.setVisible(true);
        }
    }

    @FXML
    void addExpenseAction(ActionEvent event) {
        Account account = (Account) DisplayInterface.selectObject(ObjectViewerController.ViewerMode.EXPENSE, ObjectViewerController.ViewerMode.ALL_ACCOUNTS);
        if (account != null) {
            expenseIdField.setText(String.valueOf(account.getId()));
            expenseField.setText(account.getCompany());
        }
    }

    @FXML
    void addPaymentAction(ActionEvent event) {
        Account account = (Account) DisplayInterface.selectObject(ObjectViewerController.ViewerMode.ASSET, ObjectViewerController.ViewerMode.ALL_ACCOUNTS);
        if (account != null) {
            paymentIdField.setText(String.valueOf(account.getId()));
            paymentField.setText(account.getCompany());
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
        expense.setDebit(Account.load(Integer.parseInt(expenseIdField.getText())));
        expense.setCredit(Account.load(Integer.parseInt(paymentField.getText())));
        expense.setDeleted(false);
        expense.setDate(dateField.getValue());
        expense.setDescription(noteField.getText());
        expense.setAmount(totalField.getText());
        expense.setId(Integer.parseInt(idField.getText()));
        expense.save();

    }

    @FXML
    void subtotalChanged() {
        if (!subtotalField.getText().isEmpty()) {
            double subtotal = Double.parseDouble(subtotalField.getText());
            vatField.setText(Settings.getInstance().getNumberFormat().format(subtotal * 0.05));
            totalField.setText(Settings.getInstance().getNumberFormat().format(subtotal * 1.05));
        } else {
            vatField.setText("");
            totalField.setText("");
        }
    }

    @FXML
    void totalChanged() {
        if (!totalField.getText().isEmpty()) {
            double total = Double.parseDouble(totalField.getText());
            subtotalField.setText(Settings.getInstance().getNumberFormat().format(total / 1.05));
            vatField.setText(Settings.getInstance().getNumberFormat().format((total / 1.05) * 0.05));
        } else {
            subtotalField.setText("");
            totalField.setText("");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idField.setText(String.valueOf(Expense.getCount() + 1));
        dateField.setValue(LocalDate.now());
    }
}
