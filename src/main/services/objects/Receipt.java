package main.services.objects;

import com.spire.doc.FileFormat;
import com.spire.doc.Table;
import main.services.backend.Database;
import main.services.backend.Settings;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringJoiner;

import static org.dizitart.no2.objects.filters.ObjectFilters.eq;

@Indices({
        @Index(value = "id", type = IndexType.Unique)
})
public class Receipt implements Mappable {
    @Id
    private int id;
    private Account credit, debit;
    private ArrayList<SalesDocument> paid = new ArrayList<>();
    private String description;
    private LocalDate date;
    private boolean deleted;

    public static ArrayList<SalesDocument> getUnpaidBills(Account account) {
        ArrayList<SalesDocument> unpaid = new ArrayList<>();
        int count = SalesDocument.getCount(SalesDocument.SalesDocumentType.INVOICE);
        for (int i = 100; i < count + 1; i++) {
            SalesDocument document = SalesDocument.load(i, SalesDocument.SalesDocumentType.INVOICE);
            if (document.getAccount().getId() == account.getId() && !document.isPaid() && !document.isDeleted()) {
                unpaid.add(document);
            }
        }
        return unpaid;
    }

    //Database Actions
    public static Receipt load(int id) {
        Nitrite db = Database.getInstance();
        ObjectRepository<Receipt> repo = db.getRepository(Receipt.class);
        return repo.find(eq("id", id)).firstOrDefault();
    }

    public static int getCount() {
        return Settings.getInstance().getCount(Receipt.class);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static void delete(int id) {
        Receipt receipt = Receipt.load(id);
        for (SalesDocument doc : receipt.getPaid()) {
            doc.setPaid(false);
        }
        receipt.setDeleted(true);
        receipt.save();
    }

    public Account getCredit() {
        return credit;
    }

    public ArrayList<SalesDocument> getPaid() {
        return paid;
    }

    public void setPaid(ArrayList<SalesDocument> paid) {
        this.paid = paid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setCredit(Account credit) {
        this.credit = credit;
    }

    public Account getDebit() {
        return debit;
    }

    public void setDebit(Account debit) {
        this.debit = debit;
    }

    public void save() {
        Nitrite db = Database.getInstance();
        ObjectRepository<Receipt> repo = db.getRepository(Receipt.class);
        if (id > getCount()) {
            repo.insert(this);
        } else {
            repo.update(eq("id", id), this);
        }
    }

    @Override
    public Document write(NitriteMapper nitriteMapper) {
        Document document = new Document();
        document.put("id", getId());
        document.put("credit_id", getCredit().getId());
        document.put("debit_id", getDebit().getId());
        document.put("description", getDescription());
        document.put("date", getDate());
        document.put("deleted", isDeleted());

        ArrayList<Document> paidInvoiceList = new ArrayList<>();
        for (SalesDocument invoice : paid) {
            paidInvoiceList.add(invoice.write(nitriteMapper));
        }
        document.put("paid", paidInvoiceList);
        return document;
    }

    @Override
    public void read(NitriteMapper nitriteMapper, Document document) {
        setId((Integer) document.get("id"));

        setCredit(Account.load((Integer) document.get("credit_id")));
        setDebit(Account.load((Integer) document.get("debit_id")));
        setDescription((String) document.get("description"));
        setDate((LocalDate) document.get("date"));
        setDeleted((Boolean) document.get("deleted"));

        ArrayList<Document> paidInvoiceList = (ArrayList<Document>) document.get("paid");
        for (Document invoiceDoc : paidInvoiceList) {
            SalesDocument invoice = new SalesDocument();
            invoice.read(nitriteMapper, invoiceDoc);
            paid.add(invoice);
        }
    }

    public void generateReceipt(String path, FileFormat ext) {
        com.spire.doc.Document format = new com.spire.doc.Document();
        format.loadFromFile(Settings.Template.RECEIPT.getPath());
        format.replace("#company_name", getCredit().getCompany(), true, true);
        format.replace("#company_address", getCredit().getAddress(), true, true);
        format.replace("#company_tel", getCredit().getPhone(), true, true);
        format.replace("#company_trn", getCredit().getTrn(), true, true);
        format.replace("#doc_id", String.valueOf(getId()), true, true);
        format.replace("#doc_date", getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), true, true);
        format.replace("#description", getDescription(), true, true);
        format.replace("#total", getTotal(), true, true);

        Table table = format.getSections().get(0).getTables().get(0);
        for (int i = 0; i < getPaid().size() - 1; i++) {
            table.getRows().insert(6 + i, table.getRows().get(6).deepClone()); //2,1
        }
        for (int r = 0; r < getPaid().size(); r++) {
            SalesDocument item = getPaid().get(r);
            table.getRows().get(r + 6).getCells().get(0).getParagraphs().get(0).setText(item.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            table.getRows().get(r + 6).getCells().get(1).getParagraphs().get(0).setText("Invoice " + item.getId());
            table.getRows().get(r + 6).getCells().get(2).getParagraphs().get(0).setText(item.getRef1());
            table.getRows().get(r + 6).getCells().get(3).getParagraphs().get(0).setText(item.getRef2());
            table.getRows().get(r + 6).getCells().get(4).getParagraphs().get(0).setText(item.getVATTotal());
            table.getRows().get(r + 6).getCells().get(5).getParagraphs().get(0).setText(item.getTotal());
        }
        format.saveToFile(path, ext);
    }

    public void view(FileFormat ext) {
        String path = new Random().nextInt() + "." + ext.toString().toLowerCase();
        this.generateReceipt(path, ext);
        try {
            Desktop.getDesktop().open(new File(path));
        } catch (IOException e) {
            new ExceptionDialog(e).showAndWait();
        }
        new File(path).deleteOnExit();
    }

    public String getCompany() {
        return credit.getCompany();
    }

    public String getTotal() {
        double total = 0;
        for (SalesDocument invoice : paid) {
            total += Double.parseDouble(invoice.getTotal());
        }
        return Settings.getInstance().getNumberFormat().format(total);
    }

    public String getInvoices() {
        StringJoiner invoices = new StringJoiner(", ");
        for (SalesDocument invoice : this.paid) {
            invoices.add(String.valueOf(invoice.getId()));
        }
        return invoices.toString();
    }
}
