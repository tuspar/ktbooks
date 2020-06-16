package main.services.objects;

import com.spire.doc.FileFormat;
import com.spire.doc.Table;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class SOA {

    private static final String SOA_PERSONAL_DOCX = "assets/soaPersonal.docx";
    private static final String SOA_NOMINAL_DOCX = "assets/soaNominal.docx";
    private static final String decimalFormat = "#0.00";

    public static void accountReport(Account account, boolean unpaidOnly, boolean balance, String notes, LocalDate start, LocalDate end, String path, FileFormat ext) {
        ArrayList<SOAItem> items = new ArrayList<>();

        int invoiceCount = SalesDocument.getCount(SalesDocument.SalesDocumentType.INVOICE) + 1;
        for (int i = 100; i < invoiceCount; i++) {
            SalesDocument document = SalesDocument.load(i, SalesDocument.SalesDocumentType.INVOICE);
            if (document.getAccount().getId() == account.getId() && !document.isDeleted() && (!document.isPaid() || !unpaidOnly)) {
                items.add(new SOAItem(document.getDate(), "Invoice " + document.getId(), document.getTotal(), ""));
            }
        }
        int receiptCount = Receipt.getCount() + 1;
        for (int i = 100; i < receiptCount; i++) {
            Receipt receipt = Receipt.load(i);
            if (receipt.getAccount().getId() == account.getId() && !receipt.isDeleted() && !unpaidOnly) {
                items.add(new SOAItem(receipt.getDate(), "Receipt " + receipt.getId(), "", receipt.getTotal()));
            }
        }
        int purchaseCount = Expense.getCount() + 1;
        for (int i = 100; i < purchaseCount; i++) {
            Expense expense = Expense.load(i);
            if (expense.getAccount().getId() == account.getId() && !expense.isDeleted() && !unpaidOnly) {
                items.add(new SOAItem(expense.getDate(), "Expense " + expense.getId(), expense.getAmount(), expense.getAmount()));
            }
        }

        ArrayList<SOAItem> sorted = cleanItems(items, start, end, balance);

        com.spire.doc.Document format = new com.spire.doc.Document();

        if (account.getType().equals(Account.PersonalType.CUSTOMER) || account.getType().equals(Account.PersonalType.SUPPLIER)) {
            format.loadFromFile(SOA_PERSONAL_DOCX);
            format.replace("#company_name", account.getCompany(), true, true);
            format.replace("#company_address", account.getAddress(), true, true);
            format.replace("#company_tel", account.getPhone(), true, true);
            format.replace("#company_trn", account.getTrn(), true, true);
            format.replace("#start_date", start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), true, true);
            format.replace("#end_date", end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), true, true);
            format.replace("#notes", notes, true, true);
        } else {
            format.loadFromFile(SOA_NOMINAL_DOCX);
            format.replace("#account_name", account.getCompany(), true, true);
            format.replace("#account_notes", account.getNotes(), true, true);
            format.replace("#start_date", start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), true, true);
            format.replace("#end_date", end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), true, true);
            format.replace("#notes", notes, true, true);
        }

        itemPrinter(format, sorted, path, ext);
    }

    public static void salesReport(boolean balance, String notes, LocalDate start, LocalDate end, String path, FileFormat ext) {
        ArrayList<SOAItem> items = new ArrayList<>();
        int invoiceCount = SalesDocument.getCount(SalesDocument.SalesDocumentType.INVOICE) + 1;
        for (int i = 100; i < invoiceCount; i++) {
            SalesDocument document = SalesDocument.load(i, SalesDocument.SalesDocumentType.INVOICE);
            if (!document.isDeleted()) {
                items.add(new SOAItem(document.getDate(), "Invoice " + document.getId(), "", document.getTotal()));
            }
        }

        ArrayList<SOAItem> sorted = cleanItems(items, start, end, balance);

        com.spire.doc.Document format = new com.spire.doc.Document();
        format.loadFromFile(SOA_NOMINAL_DOCX);
        format.replace("#account_name", "Sales", true, true);
        format.replace("#account_notes", "", true, true);
        format.replace("#start_date", start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), true, true);
        format.replace("#end_date", end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), true, true);
        format.replace("#notes", notes, true, true);

        itemPrinter(format, sorted, path, ext);
    }

    public static void purchaseReport(boolean balance, String notes, LocalDate start, LocalDate end, String path, FileFormat ext) {
        ArrayList<SOAItem> items = new ArrayList<>();
        int expenseCount = Expense.getCount() + 1;
        for (int i = 100; i < expenseCount; i++) {
            Expense expense = Expense.load(i);
            if (!expense.isDeleted() && expense.getAccount().getType().equals(Account.PersonalType.SUPPLIER)) {
                items.add(new SOAItem(expense.getDate(), "Purchase " + expense.getId() + ": " + expense
                        .getAccount().getCompany(), expense.getAmount(), ""));
            }
        }

        ArrayList<SOAItem> sorted = cleanItems(items, start, end, balance);

        com.spire.doc.Document format = new com.spire.doc.Document();
        format.loadFromFile(SOA_NOMINAL_DOCX);
        format.replace("#account_name", "Purchases", true, true);
        format.replace("#account_notes", "Expenses filled under all supplier accounts", true, true);
        format.replace("#start_date", start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), true, true);
        format.replace("#end_date", end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), true, true);
        format.replace("#notes", notes, true, true);

        itemPrinter(format, sorted, path, ext);
    }

    private static void itemPrinter(com.spire.doc.Document format, ArrayList<SOAItem> sorted, String path, FileFormat ext) {
        Table table = format.getSections().get(0).getTables().get(0);
        int rowsFromStart = 6;
        System.out.println(table.getRows().getCount());
        for (int i = 0; i < sorted.size() - 1; i++) {
            table.getRows().insert(rowsFromStart + i, table.getRows().get(6).deepClone());
        }
        double runningTotal = 0;
        for (int r = 0; r < sorted.size(); r++) {
            SOAItem item = sorted.get(r);
            if (!item.getDebit().isEmpty()) {
                runningTotal += Double.parseDouble(item.getDebit());
            }
            if (!item.getCredit().isEmpty()) {
                runningTotal -= Double.parseDouble(item.getCredit());
            }
            table.getRows().get(r + rowsFromStart).getCells().get(0).getParagraphs().get(0).setText(item.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            table.getRows().get(r + rowsFromStart).getCells().get(1).getParagraphs().get(0).setText(item.getDescription());
            if (!item.getDebit().isEmpty()) {
                table.getRows().get(r + rowsFromStart).getCells().get(2).getParagraphs().get(0).setText(new DecimalFormat(decimalFormat).format(Double.parseDouble(item.getDebit())));
            }
            if (!item.getCredit().isEmpty()) {
                table.getRows().get(r + rowsFromStart).getCells().get(3).getParagraphs().get(0).setText(new DecimalFormat(decimalFormat).format(Double.parseDouble(item.getCredit())));
            }
            table.getRows().get(r + rowsFromStart).getCells().get(4).getParagraphs().get(0).setText(new DecimalFormat(decimalFormat).format(runningTotal));
        }

        format.saveToFile(path, ext);
    }

    private static ArrayList<SOAItem> cleanItems(ArrayList<SOAItem> items, LocalDate start, LocalDate end, boolean balance) {
        // Add balance b/d record
        if (balance) {
            double credit = 0;
            double debit = 0;
            for (SOAItem item : items) {
                if (item.getDate().isBefore(start)) {
                    if (!item.getDebit().isEmpty()) {
                        debit += Double.parseDouble(item.getDebit());
                    }
                    if (!item.getCredit().isEmpty()) {
                        credit += Double.parseDouble(item.getCredit());
                    }
                }
            }
            items.add(0, new SOAItem(start, "Balance b/d", String.valueOf(debit), String.valueOf(credit)));
        }
        //Remove records outside date parameters
        ArrayList<SOAItem> remove = new ArrayList<>();
        for (SOAItem item : items) {
            if (item.getDate().isBefore(start)) {
                remove.add(item);
            }
            if (item.getDate().isAfter(end)) {
                remove.add(item);
            }
        }
        items.removeAll(remove);
        //Bubble sort by ascending date
        boolean sorted = false;
        if (items.size() > 1) {
            while (!sorted) {
                for (int i = 0; i < items.size() - 1; i++) {
                    sorted = true;
                    if (items.get(i).getDate().isAfter(items.get(i + 1).getDate())) {
                        SOAItem temp = items.get(i);
                        items.set(i, items.get(i + 1));
                        items.set(i + 1, temp);
                        sorted = false;
                    }
                }
            }
        }
        return items;
    }

    private static class SOAItem {
        private LocalDate date;
        private String description;
        private String debit;
        private String credit;

        SOAItem(LocalDate date, String description, String debit, String credit) {
            this.date = date;
            this.description = description;
            this.debit = debit;
            this.credit = credit;
        }

        LocalDate getDate() {
            return date;
        }

        String getDescription() {
            return description;
        }

        String getDebit() {
            return debit;
        }

        String getCredit() {
            return credit;
        }

    }
}
