package main.controllers.editors;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import com.spire.doc.FileFormat;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class SalesDocumentEditController implements Initializable {
    @FXML
    private MenuItem newDocumentButton;

    @FXML
    private MenuItem markPaid;

    @FXML
    private MenuItem exitButton;

    @FXML
    private MenuItem verifyButton;

    @FXML
    private MenuItem verifyPostButton;

    @FXML
    private JFXTextField companyField;

    @FXML
    private Button addCustomerButton;

    @FXML
    private JFXTextField customerIDField;

    @FXML
    private JFXTextField idField;

    @FXML
    private JFXTextField ref1Field;

    @FXML
    private JFXTextField ref2Field;

    @FXML
    private JFXDatePicker dateField;

    @FXML
    private JFXToggleButton discountCheck;

    @FXML
    private Spinner<Double> discountField;

    @FXML
    private TableView<SalesDocument.Item> table;

    @FXML
    private TableColumn<SalesDocument.Item, String> noColumn;

    @FXML
    private TableColumn<SalesDocument.Item, String> descriptionColumn;

    @FXML
    private TableColumn<SalesDocument.Item, String> quantityColumn;

    @FXML
    private TableColumn<SalesDocument.Item, String> uomColumn;

    @FXML
    private TableColumn<SalesDocument.Item, String> priceColumn;

    @FXML
    private TableColumn<SalesDocument.Item, String> totalColumn;

    @FXML
    private JFXTextField subTotalField;

    @FXML
    private JFXTextField discountTotalField;

    @FXML
    private JFXTextField vatTotalField;

    @FXML
    private JFXTextField netTotalField;

    @FXML
    private JFXSpinner loadingBar;

    @FXML
    private Button updateButton;

    @FXML
    private Button addItem;

    @FXML
    private Button removeItem;

    @FXML
    private Button duplicateItem;

    @FXML
    private Text typeText;

    private SalesDocument document = new SalesDocument();

    private SalesDocument.SalesDocumentType mode;

    public void setMode(SalesDocument.SalesDocumentType mode) {
        this.mode = mode;
        typeText.setText(mode.toString());
        if (mode.equals(SalesDocument.SalesDocumentType.INVOICE)) {
            markPaid.setDisable(false);
        }
        String[] refs = SalesDocument.getRefs(mode);
        ref1Field.setPromptText(refs[0]);
        ref2Field.setPromptText(refs[1]);
    }

    public void setDocument(SalesDocument document) {
        setMode(document.getType());
        newDocumentButton.setVisible(false);
        table.getItems().addAll(document.getItems());
        customerIDField.setText(String.valueOf(document.getAccount().getId()));
        idField.setText(String.valueOf(document.getId()));
        ref1Field.setText(document.getRef1());
        ref2Field.setText(document.getRef2());
        dateField.setValue(document.getDate());
        markPaid.setDisable(document.isPaid());
        if (document.getDiscount() > 0.0) {
            discountCheck.setSelected(true);
            discountField.setDisable(false);
            discountField.getValueFactory().setValue(document.getDiscount());
        }
        updateTable();
    }

    @FXML
    public void newDocument() {
        idField.setText(String.valueOf(SalesDocument.getCount(mode) + 1));
    }

    @FXML
    private void importItemsAction() {
        SalesDocument document = (SalesDocument) DisplayInterface.selectObject(ObjectViewerController.ViewerMode
                .valueOf(mode.toString().toUpperCase()), ObjectViewerController.ViewerMode.ALL_SALE_DOCUMENTS);
        if (document != null) {
            table.getItems().addAll(document.getItems());
            updateTable();
        }
    }

    @FXML
    private void exitAction(ActionEvent event) {
        //DisplayInterface.confirmExit(event);
    }

    @FXML
    private boolean verifyAction(ActionEvent actionEvent) {
        return true;
    }

    @FXML
    private void verifyAndPostAction(ActionEvent actionEvent) {
        if (verifyAction(actionEvent)) {
            SalesDocument document = makeDocument();
            document.save();
            if (document.getAccount().getCompany().toLowerCase().contains("cash") &&
                    DisplayInterface.confirmDialog("Mark as Paid", "Invoice filed with account ")) {
            }
        }
    }

    @FXML
    private void viewWordAction() {
        updateTable();
        loadingBar.setVisible(true);
        new Thread(() -> {
            makeDocument().viewSalesDocument(FileFormat.Docx);
            loadingBar.setVisible(false);
        }).start();
    }

    @FXML
    private void viewPDFAction() {
        updateTable();
        loadingBar.setVisible(true);
        new Thread(() -> {
            makeDocument().viewSalesDocument(FileFormat.PDF);
            loadingBar.setVisible(false);
        }).start();
    }

    @FXML
    private void saveAsAction(ActionEvent actionEvent) {
        updateTable();
        loadingBar.setVisible(true);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save " + mode);
        fileChooser.setInitialFileName(mode + " " + idField.getText());
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Word Document", "*.docx"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = fileChooser.showSaveDialog(table.getScene().getWindow());
        new Thread(() -> {
            if (file.getName().endsWith(".pdf")) {
                try {
                    makeDocument().generateSalesDocument(file.getAbsolutePath(), FileFormat.PDF);
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    new ExceptionDialog(e).showAndWait();
                }
            } else {
                try {
                    makeDocument().generateSalesDocument(file.getAbsolutePath(), FileFormat.Docx);
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    new ExceptionDialog(e).showAndWait();
                }
            }
        }).start();
        Notifications.create().title("Saved").text(file.getName() + " was Saved!").show();
        loadingBar.setVisible(false);
    }

    @FXML
    private void addCustomerAction(ActionEvent actionEvent) {
        Account account = (Account) DisplayInterface.selectObject(ObjectViewerController.ViewerMode.CUSTOMER, ObjectViewerController.ViewerMode.ALL_ACCOUNTS);
        if (account != null) {
            customerIDField.setText(String.valueOf(account.getId()));
            companyField.setText(account.getCompany());
        }
    }

    @FXML
    private void addDiscountAction(ActionEvent actionEvent) {
        discountField.setDisable(!discountCheck.isSelected());
        updateTable();
    }

    @FXML
    private void editDescriptionAction(TableColumn.CellEditEvent<SalesDocument.Item, String> t) {
        t.getTableView().getItems().get(t.getTablePosition().getRow()).setDescription(t.getNewValue());
        new Thread(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                new ExceptionDialog(e).showAndWait();
            }
            Platform.runLater(() -> {
                table.getSelectionModel().select(t.getTablePosition().getRow());
                table.getSelectionModel().focus(t.getTablePosition().getRow());
                table.edit(t.getTablePosition().getRow(), quantityColumn);
                updateTable();
            });
        }).start();
    }

    @FXML
    private void editQuantityAction(TableColumn.CellEditEvent<SalesDocument.Item, String> t) {
        t.getTableView().getItems().get(t.getTablePosition().getRow()).setQuantity(t.getNewValue());
        new Thread(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                new ExceptionDialog(e).showAndWait();
            }
            Platform.runLater(() -> {
                table.getSelectionModel().select(t.getTablePosition().getRow());
                table.getSelectionModel().focus(t.getTablePosition().getRow());
                table.edit(t.getTablePosition().getRow(), uomColumn);
                updateTable();
            });
        }).start();
    }

    @FXML
    private void editUOM(TableColumn.CellEditEvent<SalesDocument.Item, String> t) {
        t.getTableView().getItems().get(t.getTablePosition().getRow()).setUom(t.getNewValue());
        new Thread(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                new ExceptionDialog(e).showAndWait();
            }
            Platform.runLater(() -> {
                table.getSelectionModel().select(t.getTablePosition().getRow());
                table.getSelectionModel().select(t.getTablePosition().getRow());
                table.edit(t.getTablePosition().getRow(), priceColumn);
                updateTable();
            });
        }).start();
    }

    @FXML
    private void editPrice(TableColumn.CellEditEvent<SalesDocument.Item, String> t) {
        t.getTableView().getItems().get(t.getTablePosition().getRow()).setPrice(t.getNewValue());
        new Thread(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                new ExceptionDialog(e).showAndWait();
            }
            Platform.runLater(() -> {
                table.getSelectionModel().select(t.getTablePosition().getRow() + 1);
                table.getSelectionModel().select(t.getTablePosition().getRow() + 1);
                table.edit(t.getTablePosition().getRow() + 1, descriptionColumn);
                updateTable();
            });
        }).start();
    }

    @FXML
    private void updateAction(ActionEvent actionEvent) {
        updateTable();
    }

    @FXML
    private void addItemAction(ActionEvent actionEvent) {
        table.getItems().add(new SalesDocument.Item("", "", "1", "", "0", null));
        updateTable();
    }

    @FXML
    private void removeItemAction(ActionEvent actionEvent) {
        if (table.getSelectionModel().getSelectedIndex() != -1) {
            table.getItems().remove(table.getSelectionModel().getSelectedIndex());
            updateTable();
        }
    }

    @FXML
    private void duplicateItemAction(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Duplicate Item");
        dialog.setHeaderText("Enter number of copies needed:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            for (int i = 0; i < Integer.parseInt(result.get()); i++) {
                SalesDocument.Item selectedItem = table.getSelectionModel().getSelectedItem();
                SalesDocument.Item duplicateItem = new SalesDocument.Item("0", selectedItem.getDescription(),
                        selectedItem.getQuantity(), selectedItem.getUom(), selectedItem.getPrice(), null);
                table.getItems().add(duplicateItem);
                updateTable();
            }
        }
    }


    //change
    @FXML
    private void markPaidAction() {
        if (Integer.parseInt(idField.getText()) <= SalesDocument.getCount(mode) &&
                DisplayInterface.confirmDialog("Mark Paid",
                        "This will automatically generate a receipt for the following invoice")) {
            SalesDocument document = makeDocument();
            ArrayList<SalesDocument> paid = new ArrayList<>();
            paid.add(document);
            Receipt receipt = new Receipt();
            receipt.setId(Receipt.getCount() + 1);
            receipt.setDeleted(false);
            receipt.setAccount(document.getAccount());
            receipt.setDate(document.getDate());
            receipt.setDescription("Auto generated sales receipt for Sales Invoice " + idField.getText());
            receipt.setPaid(paid);
            receipt.save();
            document.setPaid(true);
            document.save();
            markPaid.setDisable(true);
        } else {
            DisplayInterface.confirmDialog("Unsaved Invoice", "Save the invoice before marking as paid");
        }
    }


    private void updateTable() {
        SalesDocument document = makeDocument();
        try {
            loadingBar.setVisible(true);
            document.setItems(table.getItems());
            for (int i = 0; i < table.getItems().size(); i++) {
                table.getItems().get(i).setNo(String.valueOf(i + 1));
                table.getItems().get(i).setTotal();
            }
            subTotalField.setText(String.valueOf(document.getSubTotal()));
            discountTotalField.setText(String.valueOf(document.getDiscountTotal()));
            vatTotalField.setText(String.valueOf(document.getVATTotal()));
            netTotalField.setText(String.valueOf(document.getTotal()));
            loadingBar.setVisible(false);
        } catch (Exception ignored) {
        }
        try {
            customerIDField.setText(String.valueOf(document.getAccount().getId()));
            companyField.setText(document.getAccount().getCompany());
        } catch (Exception ignored) {
        }
    }

    private SalesDocument makeDocument() {
        SalesDocument document = new SalesDocument();
        document.setItems(table.getItems());
        document.setId(Integer.parseInt(idField.getText()));
        if (discountCheck.isSelected()) {
            document.setDiscount(discountField.getValue());
        } else {
            document.setDiscount(0.00);
        }
        document.setRef1(ref1Field.getText());
        document.setRef2(ref2Field.getText());
        document.setDeleted(false);
        try {
            if (!customerIDField.getText().equals("")) {
                Account customer = Account.load(Integer.parseInt(customerIDField.getText()));
                document.setAccount(customer);
            }
        } catch (Exception e) {
            new ExceptionDialog(e).showAndWait();
        }
        document.setDate(dateField.getValue());
        document.setPaid(markPaid.isDisable());
        document.setType(mode);
        return document;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        table.setItems(document.getItems());

        //Actual
        noColumn.setCellValueFactory(new PropertyValueFactory<>("no"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        uomColumn.setCellValueFactory(new PropertyValueFactory<>("uom"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

        descriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        uomColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        priceColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        discountField.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100, 0));

        dateField.setConverter(new StringConverter<>() {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });
        dateField.setValue(LocalDate.now());
    }
}
