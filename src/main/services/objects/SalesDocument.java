package main.services.objects;

import com.spire.doc.FileFormat;
import com.spire.doc.Table;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.services.backend.Database;
import main.services.backend.Settings;
import org.controlsfx.control.Notifications;
import org.controlsfx.dialog.ExceptionDialog;
import org.dizitart.no2.Document;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;
import org.dizitart.no2.objects.ObjectRepository;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;

import static org.dizitart.no2.objects.filters.ObjectFilters.and;
import static org.dizitart.no2.objects.filters.ObjectFilters.eq;

@Indices({
        @Index(value = "id", type = IndexType.Unique),
        @Index(value = "type", type = IndexType.NonUnique)
})
public class SalesDocument implements Mappable {
    private final DecimalFormat numberFormat = Settings.getInstance().getNumberFormat();

    private ObservableList<SalesDocument.Item> items = FXCollections.observableArrayList();
    private double discount;
    private SalesDocumentType type;
    @Id
    private int id;
    private Account account;
    private LocalDate date;
    private boolean paid, deleted;

    private String ref1;
    private String ref2;

    public static SalesDocument load(int id, SalesDocumentType type) {
        Nitrite db = Database.getInstance();
        ObjectRepository<SalesDocument> repo = db.getRepository(SalesDocument.class);
        return repo.find(and(eq("type", type.toString()), eq("id", type.toString().charAt(0) + String.valueOf(id)))).firstOrDefault();
    }

    public static void delete(int id, SalesDocumentType type) {
        SalesDocument document = SalesDocument.load(id, type);
        document.setDeleted(true);
        document.save();
        String typeString = document.getType().toString();
        Notifications.create().title("Deleted " + typeString).text(typeString + " " + document.getId()
                + " was successfully deleted!").showInformation();
    }

    public static int getCount(SalesDocumentType type) {
        ObjectRepository repo = Database.getInstance().getRepository(SalesDocument.class);
        return 99 + repo.find(eq("type", type.toString())).size();
    }

    public static String[] getRefs(SalesDocumentType type) {
        switch (type) {
            case INVOICE:
                return new String[]{"LPO Number", "DO Number"};
            case QUOTATION:
                return new String[]{"Attention", "Reference Number"};
            case DO:
                return new String[]{"LPO Number", "LPO Date"};
        }
        return new String[]{"", ""};
    }

    //Getters & Setters
    public ObservableList<Item> getItems() {
        return items;
    }

    public void setItems(ObservableList<Item> items) {
        this.items = items;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getRef1() {
        return ref1;
    }

    public void setRef1(String ref1) {
        this.ref1 = ref1;
    }

    public String getRef2() {
        return ref2;
    }

    public void setRef2(String ref2) {
        this.ref2 = ref2;
    }

    public SalesDocumentType getType() {
        return type;
    }

    public void setType(SalesDocumentType type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    //Must remove
    public String getCompany() {
        return this.account.getCompany();
    }

    //Totaling
    public String getSubTotal() {
        double total = 0;
        for (SalesDocument.Item item : items) {
            total += Double.parseDouble(item.getTotal());
        }
        return numberFormat.format(total);
    }

    public String getDiscountTotal() {
        return numberFormat.format(Double.parseDouble(getSubTotal()) * (discount * 0.01));
    }

    public String getVATTotal() {
        return numberFormat.format((Double.parseDouble(getSubTotal()) - Double.parseDouble(getDiscountTotal())) * 0.05);
    }

    public String getTotal() {
        return numberFormat.format((Double.parseDouble(getSubTotal()) - Double.parseDouble(getDiscountTotal())) + Double.parseDouble(getVATTotal()));
    }

    //Database Actions
    public void save() {
        Nitrite db = Database.getInstance();
        ObjectRepository<SalesDocument> repo = db.getRepository(SalesDocument.class);
        if (id > getCount(type)) {
            repo.insert(this);
        } else {
            repo.update(and(eq("type", type.toString()), eq("id", type.toString().charAt(0) + String.valueOf(id))), this);
        }
    }

    @Override
    public Document write(NitriteMapper mapper) {
        Document document = new Document();
        document.put("id", getType().toString().charAt(0) + String.valueOf(getId()));
        document.put("account_id", getAccount().getId());
        document.put("discount", getDiscount());
        document.put("ref1", getRef1());
        document.put("ref2", getRef2());
        document.put("type", getType().toString());
        document.put("date", getDate());
        document.put("paid", isPaid());
        document.put("deleted", isDeleted());

        ArrayList<Document> itemArrayList = new ArrayList<>();
        for (Item item : items) {
            itemArrayList.add(item.write(mapper));
        }
        document.put("items", itemArrayList);
        return document;
    }

    @Override
    public void read(NitriteMapper nitriteMapper, Document document) {
        setId(Integer.parseInt(((String) document.get("id")).substring(1)));
        setAccount(Account.load((Integer) document.get("account_id")));

        setDiscount((Double) document.get("discount"));
        setRef1((String) document.get("ref1"));
        setRef2((String) document.get("ref2"));
        setType(SalesDocumentType.valueOf(((String) document.get("type")).toUpperCase()));
        setDate((LocalDate) document.get("date"));
        setPaid((Boolean) document.get("paid"));
        setDeleted((Boolean) document.get("deleted"));

        ArrayList<Document> itemArrayList = (ArrayList<Document>) document.get("items");
        for (Document itemDocument : itemArrayList) {
            Item item = new Item("", "", "1", "", "0", null);
            item.read(nitriteMapper, itemDocument);
            items.add(item);
        }
    }

    //Report Generation
    public void generateSalesDocument(String path, FileFormat ext) {
        com.spire.doc.Document format = new com.spire.doc.Document();
        switch (getType()) {
            case QUOTATION:
                format.loadFromFile(Settings.Template.QUOTATION.getPath());
                break;
            case INVOICE:
                format.loadFromFile(Settings.Template.INVOICE.getPath());
                break;
            case DO:
                format.loadFromFile(Settings.Template.INVOICE.getPath());
                break;
        }
        format.replace("#type", getType().toString(), true, true);
        format.replace("#company_name", getAccount().getCompany(), true, true);
        format.replace("#company_address", getAccount().getAddress(), true, true);
        format.replace("#company_tel", getAccount().getPhone(), true, true);
        format.replace("#company_trn", getAccount().getTrn(), true, true);
        format.replace("#type_no", getType().toString() + " No:", true, true);
        format.replace("#doc_id", String.valueOf(getId()), true, true);
        format.replace("#doc_date", getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), true, true);
        format.replace("#ref1", SalesDocument.getRefs(getType())[0], true, true);
        format.replace("#ref2", SalesDocument.getRefs(getType())[1], true, true);
        format.replace("#ref1data", getRef1(), true, true);
        format.replace("#ref2data", getRef2(), true, true);

        if (!getType().equals(SalesDocumentType.DO)) {
            format.replace("#subtotal", String.valueOf(getSubTotal()), true, true);
            format.replace("#discount_percent", "Discount(" + getDiscount() + "%):", true, true);
            format.replace("#discount", String.valueOf(getDiscountTotal()), true, true);
            format.replace("#vat", String.valueOf(getVATTotal()), true, true);
            format.replace("#total", getTotal(), true, true);
        } else {
            format.replace("#subtotal", "", true, true);
            format.replace("#discount_percent", "Discount(" + getDiscount() + "%):", true, true);
            format.replace("#discount", "", true, true);
            format.replace("#vat", "", true, true);
            format.replace("#total", "", true, true);
        }

        Table table = format.getSections().get(0).getTables().get(0);
        for (int i = 0; i < getItems().size() - 1; i++) {
            table.getRows().insert(6 + i, table.getRows().get(6).deepClone()); //2,1
        }
        for (int r = 0; r < getItems().size(); r++) {
            SalesDocument.Item item = getItems().get(r);
            table.getRows().get(r + 6).getCells().get(0).getParagraphs().get(0).setText(item.getNo());
            table.getRows().get(r + 6).getCells().get(1).getParagraphs().get(0).setText(item.getDescription());
            table.getRows().get(r + 6).getCells().get(2).getParagraphs().get(0).setText(item.getQuantity());
            table.getRows().get(r + 6).getCells().get(3).getParagraphs().get(0).setText(item.getUom());
            if (!getType().equals(SalesDocument.SalesDocumentType.DO)) {
                table.getRows().get(r + 6).getCells().get(4).getParagraphs().get(0).setText(item.getPrice());
                table.getRows().get(r + 6).getCells().get(5).getParagraphs().get(0).setText(item.getTotal());
            }

        }
        format.saveToFile(path, ext);
    }

    public void viewSalesDocument(FileFormat ext) {
        String path = new Random().nextInt() + "." + ext.toString().toLowerCase();
        generateSalesDocument(path, ext);
        try {
            Desktop.getDesktop().open(new File(path));
        } catch (IOException e) {
            new ExceptionDialog(e).showAndWait();
        }
        new File(path).deleteOnExit();
    }

    public enum SalesDocumentType {
        QUOTATION("Quotation"),
        INVOICE("Invoice"),
        DO("DO");

        private String label;

        SalesDocumentType(String label) {
            this.label = label;
        }

        public String toString() {
            return label;
        }
    }

    //Item Subclass
    public static class Item implements Mappable {
        private StringProperty no;
        private StringProperty description, uom;
        private StringProperty quantity;
        private StringProperty price;
        private StringProperty total;

        public Item(String no, String description, String quantity, String uom, String price, String total) {
            this.no = new SimpleStringProperty(String.valueOf(no));
            this.description = new SimpleStringProperty(description);
            this.uom = new SimpleStringProperty(uom);
            this.quantity = new SimpleStringProperty(String.valueOf(quantity));
            this.price = new SimpleStringProperty(String.valueOf(price));
            this.total = new SimpleStringProperty(Settings.getInstance().getNumberFormat().format(Double.parseDouble(quantity) * Double.parseDouble(price)));
        }

        public String getNo() {
            return no.get();
        }

        public void setNo(String no) {
            this.no.set(no);
        }

        public StringProperty noProperty() {
            return no;
        }

        public String getDescription() {
            return description.get();
        }

        public void setDescription(String description) {
            this.description.set(description);
        }

        public StringProperty descriptionProperty() {
            return description;
        }

        public String getUom() {
            return uom.get();
        }

        public void setUom(String uom) {
            this.uom.set(uom);
        }

        public StringProperty uomProperty() {
            return uom;
        }

        public String getQuantity() {
            return quantity.get();
        }

        public void setQuantity(String quantity) {
            this.quantity.set(quantity);
        }

        public StringProperty quantityProperty() {
            return quantity;
        }

        public String getPrice() {
            return Settings.getInstance().getNumberFormat().format(Double.parseDouble(price.get()));
        }

        public void setPrice(String price) {
            this.price.set(Settings.getInstance().getNumberFormat().format(Double.parseDouble(price)));
        }

        public StringProperty priceProperty() {
            return price;
        }

        public String getTotal() {
            return Settings.getInstance().getNumberFormat().format(Double.parseDouble(quantity.get()) * Double.parseDouble(price.get()));
        }

        public void setTotal() {
            this.total.set(Settings.getInstance().getNumberFormat().format(Double.parseDouble(quantity.get()) * Double.parseDouble(price.get())));
        }

        public StringProperty totalProperty() {
            return new SimpleStringProperty(Settings.getInstance().getNumberFormat().format(Double.parseDouble(quantity.get()) * Double.parseDouble(price.get())));
        }

        @Override
        public Document write(NitriteMapper nitriteMapper) {
            Document document = new Document();
            document.put("no", getNo());
            document.put("description", getDescription());
            document.put("uom", getUom());
            document.put("quantity", getQuantity());
            document.put("price", getPrice());
            return document;
        }

        @Override
        public void read(NitriteMapper nitriteMapper, Document document) {
            if (document != null) {
                setNo((String) document.get("no"));
                setDescription((String) document.get("description"));
                setQuantity((String) document.get("quantity"));
                setPrice((String) document.get("price"));
                setUom((String) document.get("uom"));
                setTotal();
            }
        }
    }
}
