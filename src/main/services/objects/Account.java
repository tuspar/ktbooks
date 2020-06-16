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

import static org.dizitart.no2.objects.filters.ObjectFilters.eq;

@Indices({
        @Index(value = "id", type = IndexType.Unique),
})
public class Account implements Mappable {
    @Id
    private int id;
    private PersonalType type;
    private String company;
    private String phone;
    private String address;
    private String email;
    private String trn;
    private String notes;
    private boolean deleted;

    public static void delete(int id) {
        Account account = Account.load(id);
        account.setDeleted(true);
        account.save();
        Notifications.create().title("Deleted Account").text(account.getCompany()
                + " was successfully deleted!").showInformation();
    }

    public static Account load(int id) {
        Nitrite db = Database.getInstance();
        ObjectRepository<Account> repo = db.getRepository(Account.class);
        return repo.find(eq("id", id)).firstOrDefault();
    }

    public static int getCount() {
        for (int count = 100; true; count++) {
            if (load(count) == null) {
                return count - 1;
            }
        }
    }

    //Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PersonalType getType() {
        return type;
    }

    public void setType(PersonalType type) {
        this.type = type;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTrn() {
        return trn;
    }

    public void setTrn(String trn) {
        this.trn = trn;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", type=" + type +
                ", company=" + company +
                ", phone=" + phone +
                ", address=" + address +
                ", email=" + email +
                ", trn=" + trn +
                ", deleted=" + deleted +
                '}';
    }

    //Database Action
    public void save() {
        Nitrite db = Database.getInstance();
        ObjectRepository<Account> repo = db.getRepository(Account.class);
        if (id > getCount()) {
            repo.insert(this);
            Notifications.create().title("Saved Account").text(getCompany()
                    + " was successfully saved!").showInformation();
        } else {
            repo.update(eq("id", id), this);
            Notifications.create().title("Updated Account").text(getCompany()
                    + " was successfully updated!").showInformation();
        }
    }

    //Database Mapper
    @Override
    public Document write(NitriteMapper nitriteMapper) {
        Document document = new Document();
        document.put("id", getId());
        document.put("type", getType().toString());
        document.put("company", getCompany());
        document.put("phone", getPhone());
        document.put("address", getAddress());
        document.put("email", getEmail());
        document.put("trn", getTrn());
        document.put("notes", getNotes());
        document.put("deleted", isDeleted());
        return document;
    }

    @Override
    public void read(NitriteMapper nitriteMapper, Document document) {
        if (document != null) {
            setId((int) document.get("id"));
            setType(PersonalType.valueOf(((String) document.get("type")).toUpperCase()));
            setCompany((String) document.get("company"));
            setPhone((String) document.get("phone"));
            setAddress((String) document.get("address"));
            setEmail((String) document.get("email"));
            setTrn((String) document.get("trn"));
            setNotes((String) document.get("notes"));
            setDeleted((Boolean) document.get("deleted"));
        }
    }

    //Account Account Types
    public enum PersonalType {
        CUSTOMER("Customer"),
        SUPPLIER("Supplier"),
        EXPENSE("Expense"),
        INCOME("Income"),
        ASSET("Asset"),
        LIABILITY("Liability");

        private String label;

        PersonalType(String label) {
            this.label = label;
        }

        public String toString() {
            return label;
        }
    }
}
