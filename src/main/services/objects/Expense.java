package main.services.objects;

import main.services.backend.Database;
import org.controlsfx.control.Notifications;
import org.dizitart.no2.Document;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;
import org.dizitart.no2.objects.ObjectRepository;

import java.time.LocalDate;

import static org.dizitart.no2.objects.filters.ObjectFilters.eq;

@Indices({
        @Index(value = "id", type = IndexType.Unique)
})
public class Expense implements Mappable {
    @Id
    private int id;

    private Account account;
    private String amount;
    private String description;
    private LocalDate date;
    private boolean deleted;

    public static Expense load(int id) {
        Nitrite db = Database.getInstance();
        ObjectRepository<Expense> repo = db.getRepository(Expense.class);
        return repo.find(eq("id", id)).firstOrDefault();
    }

    public static int getCount() {
        for (int count = 100; true; count++) {
            if (load(count) == null) {
                return count - 1;
            }
        }
    }

    public static void delete(int id) {
        Expense expense = Expense.load(id);
        expense.setDeleted(true);
        expense.save();
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

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
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

    public String getCompany() {
        return account.getCompany();
    }

    public void save() {
        Nitrite db = Database.getInstance();
        ObjectRepository<Expense> repo = db.getRepository(Expense.class);
        if (id > getCount()) {
            repo.insert(this);
            Notifications.create().title("Saved Expense")
                    .text("Expense " + id + " for " + account.getCompany() + " was successfully saved!").showInformation();
        } else {
            repo.update(eq("id", id), this);
            Notifications.create().title("Updated Expense")
                    .text("Expense " + id + " for " + account.getCompany() + " was successfully updated!").showInformation();
        }
    }

    @Override
    public Document write(NitriteMapper mapper) {
        Document document = new Document();
        document.put("id", getId());
        document.put("account", getAccount().write(mapper));
        document.put("description", getDescription());
        document.put("date", getDate());
        document.put("deleted", isDeleted());
        document.put("amount", getAmount());

        return document;
    }

    @Override
    public void read(NitriteMapper nitriteMapper, Document document) {
        setId((Integer) document.get("id"));

        Account account = new Account();
        account.read(nitriteMapper, (Document) document.get("account"));
        setAccount(account);

        setDescription((String) document.get("description"));
        setDate((LocalDate) document.get("date"));
        setDeleted((Boolean) document.get("deleted"));
        setAmount((String) document.get("amount"));
    }
}
