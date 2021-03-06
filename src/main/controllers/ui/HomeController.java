package main.controllers.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import main.services.backend.DisplayInterface;
import main.services.objects.Account;
import main.services.objects.Expense;
import main.services.objects.Receipt;
import main.services.objects.SalesDocument;

import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML
    private Text homeLabel;

    @FXML
    void settingsAction() {
        DisplayInterface.settings();
    }

    @FXML
    void exitAction() {
        System.exit(0);
    }

    @FXML
    void newPersonalAction() {
        DisplayInterface.newPersonalAccount();
    }

    @FXML
    void editPersonalAction() {
        Account account = (Account) DisplayInterface.selectObject(ObjectViewerController.ViewerMode.PERSONAL_ACCOUNTS, ObjectViewerController.ViewerMode.PERSONAL_ACCOUNTS);
        if (account != null) {
            DisplayInterface.editPersonalAccount(account);
        }

    }

    @FXML
    void newNominalAction() {
        DisplayInterface.newNominalAccount();
    }

    @FXML
    void editNominalAction() {
        Account account = (Account) DisplayInterface.selectObject(ObjectViewerController.ViewerMode.NOMINAL_ACCOUNTS, ObjectViewerController.ViewerMode.NOMINAL_ACCOUNTS);
        if (account != null) {
            DisplayInterface.editNominalAccount(account);
        }
    }

    @FXML
    void newInvoiceAction() {
        DisplayInterface.newSalesDocument(SalesDocument.SalesDocumentType.INVOICE);
    }

    @FXML
    void editInvoiceAction() {
        SalesDocument document = (SalesDocument) DisplayInterface.selectObject(ObjectViewerController.ViewerMode.INVOICE, ObjectViewerController.ViewerMode.INVOICE);
        if (document != null) {
            DisplayInterface.loadSalesDocument(document);
        }
    }

    @FXML
    void newQuotationAction() {
        DisplayInterface.newSalesDocument(SalesDocument.SalesDocumentType.QUOTATION);
    }

    @FXML
    void editQuotationAction() {
        SalesDocument document = (SalesDocument) DisplayInterface.selectObject(ObjectViewerController.ViewerMode.QUOTATION, ObjectViewerController.ViewerMode.QUOTATION);
        if (document != null) {
            DisplayInterface.loadSalesDocument(document);
        }
    }

    @FXML
    void newDOAction() {
        DisplayInterface.newSalesDocument(SalesDocument.SalesDocumentType.DO);
    }

    @FXML
    void editDOAction() {
        SalesDocument document = (SalesDocument) DisplayInterface.selectObject(ObjectViewerController.ViewerMode.DO, ObjectViewerController.ViewerMode.DO);
        if (document != null) {
            DisplayInterface.loadSalesDocument(document);
        }
    }

    @FXML
    void newReceipt() {
        DisplayInterface.newReceipt();
    }

    @FXML
    void editReceiptAction() {
        Receipt receipt = (Receipt) DisplayInterface.selectObject(ObjectViewerController.ViewerMode.ALL_ACCOUNTS, ObjectViewerController.ViewerMode.RECEIPT);
        if (receipt != null) {
            DisplayInterface.loadReceipt(receipt);
        }
    }

    @FXML
    void newSOA() {
        DisplayInterface.newSOA();
    }

    @FXML
    void newExpenseAction() {
        DisplayInterface.newExpense();
    }

    @FXML
    void editExpenseAction() {
        Expense expense = (Expense) DisplayInterface.selectObject(ObjectViewerController.ViewerMode.ALL_ACCOUNTS, ObjectViewerController.ViewerMode.BILL);
        if (expense != null) {
            DisplayInterface.loadExpense(expense);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
