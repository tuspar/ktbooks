package main.controllers.ui;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import main.services.objects.Account;
import main.services.objects.Expense;
import main.services.objects.Receipt;
import main.services.objects.SalesDocument;

public class ObjectViewerController {
    private final String[] personalColumns = new String[]{"ID", "Company", "Type", "Notes"};
    private final String[] salesDocumentColumns = new String[]{"ID", "Type", "Company", "Total"};
    private final String[] receiptColumns = new String[]{"ID", "Date", "Company", "Description", "Total"};
    private final String[] expenseColumns = new String[]{"ID", "Date", "Company", "Description", "Amount"};
    @FXML
    private TableView<Object> table;
    @FXML
    private JFXComboBox<String> typeSelector;
    @FXML
    private JFXTextField searchField;
    private ObservableList<Object> data = FXCollections.observableArrayList();
    private ObservableList<Object> typeSorted = FXCollections.observableArrayList();
    private ViewerMode[] mode;
    private Object selected = null;

    public Object getSelected() {
        return selected;
    }

    public void setMode(ViewerMode... allModes) {
        this.mode = allModes;

        String[] columnNames = new String[]{};
        switch (mode[0]) {
            case INVOICE:
            case QUOTATION:
            case DO:
            case ALL_SALE_DOCUMENTS:
                columnNames = salesDocumentColumns;
                break;

            case CUSTOMER:
            case SUPPLIER:
            case PERSONAL_ACCOUNTS:
            case EXPENSE:
            case INCOME:
            case ASSET:
            case LIABILITY:
            case NOMINAL_ACCOUNTS:
            case ALL_ACCOUNTS:
                columnNames = personalColumns;
                break;

            case RECEIPT:
                columnNames = receiptColumns;
                break;

            case BILL:
                columnNames = expenseColumns;
                break;
        }

        for (String columnName : columnNames) {
            TableColumn<Object, String> column = new TableColumn<>(columnName);
            column.setCellValueFactory(new PropertyValueFactory<>(columnName.toLowerCase()));
            table.getColumns().add(column);
        }
        for (ViewerMode mode : allModes) {
            switch (mode) {
                case INVOICE:
                    salesDocumentLoader(SalesDocument.SalesDocumentType.INVOICE);
                    break;
                case QUOTATION:
                    salesDocumentLoader(SalesDocument.SalesDocumentType.QUOTATION);
                    break;
                case DO:
                    salesDocumentLoader(SalesDocument.SalesDocumentType.DO);
                    break;
                case ALL_SALE_DOCUMENTS:
                    salesDocumentLoader(SalesDocument.SalesDocumentType.INVOICE);
                    salesDocumentLoader(SalesDocument.SalesDocumentType.QUOTATION);
                    salesDocumentLoader(SalesDocument.SalesDocumentType.DO);
                    typeSelector.getItems().add("All");
                    typeSelector.setValue("All");
                    break;
                case CUSTOMER:
                    personalLoader(Account.PersonalType.CUSTOMER);
                    break;
                case SUPPLIER:
                    personalLoader(Account.PersonalType.SUPPLIER);
                    break;
                case EXPENSE:
                    personalLoader(Account.PersonalType.EXPENSE);
                    break;
                case INCOME:
                    personalLoader(Account.PersonalType.INCOME);
                    break;
                case ASSET:
                    personalLoader(Account.PersonalType.ASSET);
                    break;
                case LIABILITY:
                    personalLoader(Account.PersonalType.LIABILITY);
                    break;
                case PERSONAL_ACCOUNTS:
                    personalLoader(Account.PersonalType.CUSTOMER);
                    personalLoader(Account.PersonalType.SUPPLIER);
                    typeSelector.getItems().add("All");
                    typeSelector.setValue("All");
                    break;
                case NOMINAL_ACCOUNTS:
                    personalLoader(Account.PersonalType.EXPENSE);
                    personalLoader(Account.PersonalType.INCOME);
                    personalLoader(Account.PersonalType.ASSET);
                    personalLoader(Account.PersonalType.LIABILITY);
                    typeSelector.getItems().add("All");
                    typeSelector.setValue("All");
                    break;
                case ALL_ACCOUNTS:
                    personalLoader(Account.PersonalType.CUSTOMER);
                    personalLoader(Account.PersonalType.SUPPLIER);
                    personalLoader(Account.PersonalType.EXPENSE);
                    personalLoader(Account.PersonalType.INCOME);
                    personalLoader(Account.PersonalType.ASSET);
                    personalLoader(Account.PersonalType.LIABILITY);
                    typeSelector.getItems().add("All");
                    typeSelector.setValue("All");
                    break;
                case RECEIPT:
                    receiptLoader();
                    break;
                case BILL:
                    purchaseLoader();
                    break;

            }
        }
        if (mode.length > 1 && !typeSelector.getItems().contains("All")) {
            typeSelector.getItems().add("All");
            typeSelector.setValue("All");
        }
        data = table.getItems();
    }

    public void setType(ViewerMode type) {
        typeSelector.setValue(type.toString());
        objectTypeListener();
    }

    public
    @FXML
    void objectTypeListener() {
        table.setItems(data);
        String type = typeSelector.getValue();
        if (!type.isEmpty() && !type.equals("All")) {
            ObservableList<Object> sorted = FXCollections.observableArrayList();
            for (ViewerMode mode : this.mode) {
                for (Object object : data) {
                    switch (mode) {
                        case INVOICE:
                        case QUOTATION:
                        case DO:
                        case ALL_SALE_DOCUMENTS:
                            SalesDocument document = (SalesDocument) object;
                            if (document.getType().toString().equals(type)) {
                                sorted.add(object);
                            }
                            break;
                        case CUSTOMER:
                        case SUPPLIER:
                        case EXPENSE:
                        case INCOME:
                        case ASSET:
                        case LIABILITY:
                        case PERSONAL_ACCOUNTS:
                        case NOMINAL_ACCOUNTS:
                        case ALL_ACCOUNTS:
                            Account account = (Account) object;
                            if (account.getType().toString().equals(type)) {
                                sorted.add(object);
                            }
                            break;
                    }
                }
            }
            table.setItems(sorted);
            typeSorted = sorted;
        } else {
            typeSorted = data;
        }
    }

    @FXML
    void searchAction(KeyEvent event) {
        table.setItems(typeSorted);
        if (event.getCode() == KeyCode.ENTER) {
            if (table.getSelectionModel().getSelectedIndex() == -1) {
                table.getSelectionModel().select(0);
            } else {
                selectButtonAction();
            }
        } else {
            String search = searchField.getText().toLowerCase();
            if (!search.isEmpty()) {
                ObservableList<Object> sorted = FXCollections.observableArrayList();
                for (ViewerMode mode : this.mode) {
                    for (Object object : typeSorted) {
                        switch (mode) {
                            case INVOICE:
                            case QUOTATION:
                            case DO:
                            case ALL_SALE_DOCUMENTS:
                                SalesDocument document = (SalesDocument) object;
                                if (document.getAccount().getCompany().toLowerCase().startsWith(search) ||
                                        String.valueOf(document.getId()).equals(search)) {
                                    sorted.add(object);
                                }
                                break;
                            case CUSTOMER:
                            case SUPPLIER:
                            case EXPENSE:
                            case INCOME:
                            case ASSET:
                            case LIABILITY:
                            case PERSONAL_ACCOUNTS:
                            case NOMINAL_ACCOUNTS:
                            case ALL_ACCOUNTS:
                                Account account = (Account) object;
                                if (account.getCompany().toLowerCase().startsWith(search) ||
                                        String.valueOf(account.getId()).equals(search)) {
                                    sorted.add(object);
                                }
                                break;
                            case BILL:
                                Expense expense = (Expense) object;
                                if (expense.getAccount().getCompany().toLowerCase().startsWith(search) ||
                                        String.valueOf(expense.getId()).equals(search)) {
                                    sorted.add(object);
                                }
                                break;
                            case RECEIPT:
                                Receipt receipt = (Receipt) object;
                                if (receipt.getAccount().getCompany().toLowerCase().startsWith(search) ||
                                        String.valueOf(receipt.getId()).equals(search)) {
                                    sorted.add(object);
                                }
                        }
                    }
                }
                table.setItems(sorted);
            }
        }
    }

    @FXML
    void selectButtonAction() {
        if (table.getSelectionModel().getSelectedIndex() == -1) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Please select an row!");
            alert.showAndWait();
        } else {
            selected = table.getSelectionModel().getSelectedItem();
            Stage stage = (Stage) table.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    void tableKeyTyped(KeyEvent event) {
        searchField.requestFocus();
        searchField.setText(searchField.getText() + event.getCharacter());
        searchField.positionCaret((searchField.getText() + event.getCharacter()).length());
    }

    private void personalLoader(Account.PersonalType type) {
        for (int i = 100; i <= Account.getCount(); i++) {
            Account account = Account.load(i);
            if (!account.isDeleted() && account.getType().equals(type)) {
                table.getItems().add(account);
            }
        }
        typeSelector.getItems().add(type.toString());
        typeSelector.setValue(type.toString());
    }

    private void salesDocumentLoader(SalesDocument.SalesDocumentType type) {
        int typeCount = SalesDocument.getCount(type);
        for (int i = 100; i <= typeCount; i++) {
            SalesDocument account = SalesDocument.load(i, type);
            if (!account.isDeleted()) {
                table.getItems().add(account);
            }
        }
        typeSelector.getItems().add(type.toString());
        typeSelector.setValue(type.toString());
    }

    private void receiptLoader() {
        int typeCount = Receipt.getCount();
        for (int i = 100; i <= typeCount; i++) {
            Receipt receipt = Receipt.load(i);
            if (!receipt.isDeleted()) {
                table.getItems().add(receipt);
            }
        }
        typeSelector.setDisable(true);
    }

    private void purchaseLoader() {
        int typeCount = Expense.getCount();
        for (int i = 100; i <= typeCount; i++) {
            Expense expense = Expense.load(i);
            if (!expense.isDeleted()) {
                table.getItems().add(expense);
            }
        }
        typeSelector.setDisable(true);
    }

    public enum ViewerMode {
        CUSTOMER("Customer"),
        SUPPLIER("Supplier"),
        PERSONAL_ACCOUNTS("All"),
        EXPENSE("Expense"),
        INCOME("Income"),
        ASSET("Asset"),
        LIABILITY("Liability"),
        NOMINAL_ACCOUNTS("All"),
        ALL_ACCOUNTS("All"),
        INVOICE("Invoice"),
        QUOTATION("Quotation"),
        DO("Delivery Order"),
        ALL_SALE_DOCUMENTS("All"),
        RECEIPT("Receipt"),
        BILL("Purchase");

        private String label;

        ViewerMode(String label) {
            this.label = label;
        }

        public String toString() {
            return label;
        }
    }
}
