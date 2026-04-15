package mehrin.loginpage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import mehrin.loginpage.Model.IssuedBook;
import mehrin.loginpage.Util.FileUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import mehrin.loginpage.Util.AutoCompleteHelper;

public class AllIssuedBooksController {

    @FXML private TextField issuedIdField;
    @FXML private TextField issuedIdInfo;
    @FXML private TextField bookIdInfo;
    @FXML private TextField studentIdInfo;
    @FXML private TextField studentNameInfo;
    @FXML private TextField lateFeeField;

    @FXML private TableView<IssuedBook> returnTable;
    @FXML private TableColumn<IssuedBook, String> issuedIdCol;
    @FXML private TableColumn<IssuedBook, String> bookIdCol;
    @FXML private TableColumn<IssuedBook, String> studentIdCol;
    @FXML private TableColumn<IssuedBook, String> studentNameCol;
    @FXML private TableColumn<IssuedBook, String> issueDateCol;
    @FXML private TableColumn<IssuedBook, String> dueDateCol;
    @FXML private TableColumn<IssuedBook, String> lateFeeCol;

    private static final String ISSUED_FILE = "issueBooks.csv";
    private static final String CART_CSV    = "addToCart.csv";

    private IssuedBook selectedBook;
    private ObservableList<IssuedBook> allBooks; // field so submitBook can reload it

    // ─────────────────────────────────────────────────────────────
    //  INIT
    // ─────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        CartExpiryUtil.purgeExpiredCartEntries(CART_CSV, ISSUED_FILE);

        // ── KEY FIX: write up-to-date fees to CSV before loading ──
        persistLateFees();

        issuedIdCol.setCellValueFactory(d -> d.getValue().issuedIdProperty());
        bookIdCol.setCellValueFactory(d -> d.getValue().bookIdProperty());
        studentIdCol.setCellValueFactory(d -> d.getValue().studentIdProperty());
        studentNameCol.setCellValueFactory(d -> d.getValue().studentNameProperty());
        issueDateCol.setCellValueFactory(d -> d.getValue().issuedDateProperty());
        dueDateCol.setCellValueFactory(d -> d.getValue().returnDateProperty());
        lateFeeCol.setCellValueFactory(d -> d.getValue().lateFeeProperty());

        allBooks = loadIssuedBooks();
        returnTable.setItems(allBooks);

        returnTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSel, newSel) -> {
                    selectedBook = newSel;
                    if (newSel != null) populateInfo(newSel);
                });

        issuedIdField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                returnTable.setItems(allBooks);
                clearInfo(); selectedBook = null;
                return;
            }
            String query = newVal.trim().toLowerCase();
            ObservableList<IssuedBook> filtered = FXCollections.observableArrayList();
            for (IssuedBook book : allBooks) {
                if (book.getStudentName().toLowerCase().contains(query)
                        || book.getStudentId().toLowerCase().contains(query)
                        || book.getBookId().toLowerCase().contains(query)
                        || getBookTitle(book.getBookId()).toLowerCase().contains(query)) {
                    filtered.add(book);
                }
            }
            if (!filtered.isEmpty()) { populateInfo(filtered.get(0)); selectedBook = filtered.get(0); }
            else { clearInfo(); selectedBook = null; }
            returnTable.setItems(filtered);
        });

        AutoCompleteHelper.setupAutoComplete(issuedIdField, text ->
                        FileUtil.readFile("issueBooks.csv").stream()
                                .filter(line -> {
                                    String[] p = line.split(",", -1);
                                    return p.length > 3 && p[3].toLowerCase().contains(text.toLowerCase());
                                })
                                .map(line -> {
                                    String[] p = line.split(",", -1);
                                    return p.length > 3 ? p[3] + " (" + p[2] + ") - Issued ID: " + p[0] : "";
                                })
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList()),
                chosen -> {
                    try { issuedIdField.setText(chosen.substring(0, chosen.indexOf(" ("))); }
                    catch (Exception ignored) {}
                });
    }

    // ─────────────────────────────────────────────────────────────
    //  PERSIST LATE FEES → CSV
    //  Called on every screen load. Recalculates fee for all
    //  non-pending rows and writes back only changed values.
    // ─────────────────────────────────────────────────────────────
    private void persistLateFees() {
        List<String> lines   = FileUtil.readFile(ISSUED_FILE);
        List<String> updated = new ArrayList<>();
        boolean changed = false;

        for (String line : lines) {
            String[] p = line.split(",", -1);
            if (p.length != 7) { updated.add(line); continue; }

            boolean pending = p[4].equalsIgnoreCase("N/A") || p[0].startsWith("CART-");

            if (!pending) {
                String correctFee = calculateLateFee(p[5]); // p[5] = due date
                if (!p[6].trim().equals(correctFee)) {
                    p[6] = correctFee;
                    changed = true;
                }
            }
            updated.add(String.join(",", p));
        }

        if (changed) {
            FileUtil.writeFile(ISSUED_FILE, updated,
                    "IssuedID,BookID,StudentID,StudentName,IssuedDate,ReturnDate,LateFee");
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  LOAD ISSUED BOOKS
    //  For CART- rows: pull expiry date from addToCart.csv so the
    //  "Return Date" column shows the cart expiry, not "N/A"
    // ─────────────────────────────────────────────────────────────
    private ObservableList<IssuedBook> loadIssuedBooks() {
        ObservableList<IssuedBook> list = FXCollections.observableArrayList();
        for (String line : FileUtil.readFile(ISSUED_FILE)) {
            String[] p = line.split(",", -1);
            if (p.length != 7) continue;

            boolean pending    = p[4].equalsIgnoreCase("N/A") || p[0].startsWith("CART-");
            String  returnDate = p[5];

            if (pending) {
                String serial = p[0].startsWith("CART-") ? p[0].substring(5) : "";
                if (!serial.isEmpty()) {
                    for (String cartLine : FileUtil.readFile(CART_CSV)) {
                        String[] c = cartLine.split(",", -1);
                        // addToCart: p[0]=Serial, p[6]=ExpiryDate, p[7]=Status
                        if (c.length > 7 && c[0].trim().equals(serial)) {
                            String cartStatus = c[7].trim();
                            if (cartStatus.equalsIgnoreCase("Ready")) {
                                returnDate = c[6].trim(); // 2-day window active
                            } else {
                                returnDate = "Waiting";   // book not available yet
                            }
                            break;
                        }
                    }
                }
            }

            String fee = pending ? "0" : calculateLateFee(returnDate);
            list.add(new IssuedBook(p[0], p[1], p[2], p[3], p[4], returnDate, fee));
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────────
    //  SEARCH
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void loadIssuedBookDetails() {
        String issuedId = issuedIdField.getText().trim();
        if (issuedId.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Missing", "Enter Issued ID"); return; }
        ObservableList<IssuedBook> filtered = FXCollections.observableArrayList();
        for (IssuedBook book : loadIssuedBooks()) {
            if (book.getIssuedId().equalsIgnoreCase(issuedId)) {
                if (!isPending(book)) book.lateFeeProperty().set(calculateLateFee(book.getReturnDate()));
                filtered.add(book);
            }
        }
        if (filtered.isEmpty()) showAlert(Alert.AlertType.INFORMATION, "Not Found", "No record found");
        returnTable.setItems(filtered);
    }

    // ─────────────────────────────────────────────────────────────
    //  POPULATE INFO PANEL
    // ─────────────────────────────────────────────────────────────
    private void populateInfo(IssuedBook book) {
        if (issuedIdInfo    != null) issuedIdInfo.setText(book.getIssuedId());
        if (bookIdInfo      != null) bookIdInfo.setText(book.getBookId());
        if (studentIdInfo   != null) studentIdInfo.setText(book.getStudentId());
        if (studentNameInfo != null) studentNameInfo.setText(book.getStudentName());
        String fee = isPending(book) ? "0" : calculateLateFee(book.getReturnDate());
        if (lateFeeField    != null) lateFeeField.setText(fee.equals("0") ? "No late fee" : fee + " Tk");
        book.lateFeeProperty().set(fee);
    }

    // ─────────────────────────────────────────────────────────────
    //  SUBMIT RETURN
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void submitBook(ActionEvent event) {
        if (selectedBook == null) { showAlert(Alert.AlertType.WARNING, "Error", "Select a book first"); return; }
        if (isPending(selectedBook)) {
            showAlert(Alert.AlertType.WARNING, "Not Issued Yet",
                    "This book is a pending cart request and has not been issued yet.");
            return;
        }
        String fee = calculateLateFee(selectedBook.getReturnDate());
        selectedBook.lateFeeProperty().set(fee);
        if (lateFeeField != null) lateFeeField.setText(fee);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Confirm return?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                recordClearance(selectedBook);
                updateLateFeeInCSV(selectedBook);
                removeIssuedEntry();
                increaseRemainingBook();

                // If late fee >= 150 Tk, restrict student for 2 months
                long feeAmount = 0;
                try { feeAmount = Long.parseLong(fee); } catch (Exception ignored) {}
                if (feeAmount >= 150) {
                    CartExpiryUtil.saveLateRestriction(selectedBook.getStudentId(),
                            LocalDate.now().plusMonths(2));
                }

                // Reload from disk so Waiting→Ready change shows immediately
                allBooks = loadIssuedBooks();
                returnTable.setItems(allBooks);
                clearInfo();
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Book returned. Late fee: " + fee + " Tk"
                                + (feeAmount >= 150 ? "\n⚠ Student restricted for 2 months due to excessive late fee." : ""));
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update files");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  CLEARANCE
    // ─────────────────────────────────────────────────────────────
    private void recordClearance(IssuedBook book) throws IOException {
        String entry = String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                book.getStudentName(), getBookTitle(book.getBookId()),
                book.getIssuedDate(), book.getReturnDate(),
                LocalDate.now(), "N/A",
                java.time.LocalTime.now(), book.getLateFee());
        List<String> lines = FileUtil.readFile("clearance.csv");
        lines.add(entry);
        FileUtil.writeFile("clearance.csv", lines,
                "StudentName,BookName,BorrowedDate,DueDate,ReturnDate,IssuedTime,ReturnTime,LateFee");
    }

    private String getBookTitle(String isbn) {
        if (isbn == null) return "Unknown Book";
        for (String line : FileUtil.readFile("books.csv")) {
            String[] p = line.split(",", -1);
            if (p.length > 1 && p[0].trim().equalsIgnoreCase(isbn.trim())) return p[1].trim();
        }
        return "Unknown Book";
    }

    private void updateLateFeeInCSV(IssuedBook book) throws IOException {
        List<String> lines = FileUtil.readFile(ISSUED_FILE);
        List<String> updated = new ArrayList<>();
        for (String line : lines) {
            String[] p = line.split(",", -1);
            if (p.length != 7) continue;
            if (p[0].equalsIgnoreCase(book.getIssuedId())) p[6] = book.getLateFee();
            updated.add(String.join(",", p));
        }
        FileUtil.writeFile(ISSUED_FILE, updated,
                "IssuedID,BookID,StudentID,StudentName,IssuedDate,ReturnDate,LateFee");
    }

    private void removeIssuedEntry() throws IOException {
        List<String> lines = FileUtil.readFile(ISSUED_FILE);
        List<String> updated = new ArrayList<>();
        for (String line : lines) {
            if (!line.startsWith(selectedBook.getIssuedId() + ",")) updated.add(line);
        }
        FileUtil.writeFile(ISSUED_FILE, updated,
                "IssuedID,BookID,StudentID,StudentName,IssuedDate,ReturnDate,LateFee");
    }

    private void increaseRemainingBook() throws IOException {
        List<String> books = FileUtil.readFile("books.csv");
        List<String> updated = new ArrayList<>();
        for (String line : books) {
            String[] p = line.split(",", -1);
            if (p.length >= 8 && p[0].equalsIgnoreCase(selectedBook.getBookId())) {
                p[6] = String.valueOf(Integer.parseInt(p[6].trim()) + 1);
                p[7] = "Available";
            }
            updated.add(String.join(",", p));
        }
        FileUtil.writeFile("books.csv", updated,
                "ISBN,Title,Author,Publisher,Edition,Quantity,Remaining,Availability,PDF");

        // If any student is Waiting for this book, set them to Ready with 2-day expiry
        activateWaitingCartEntry(selectedBook.getBookId());
    }

    /**
     * Finds the first "Waiting" cart entry for the given bookIsbn,
     * sets its status to "Ready" and expiry to today + 2 days.
     * addToCart.csv: Serial,StudentID,StudentName,BookISBN,BookName,RequestDate,ExpiryDate,Status
     */
    private void activateWaitingCartEntry(String bookIsbn) {
        java.io.File cartFile = new java.io.File("data/" + CART_CSV);
        if (!cartFile.exists()) return;

        List<String> lines = new ArrayList<>();
        boolean updated = false;

        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(cartFile))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { lines.add(line); firstLine = false; continue; }
                String[] c = line.split(",", -1);
                // p[3]=BookISBN, p[7]=Status — only update first Waiting entry
                if (!updated && c.length > 7
                        && c[3].trim().equalsIgnoreCase(bookIsbn)
                        && c[7].trim().equalsIgnoreCase("Waiting")) {
                    c[6] = LocalDate.now().plusDays(2).toString(); // new expiry
                    c[7] = "Ready";
                    updated = true;
                }
                lines.add(String.join(",", c));
            }
        } catch (Exception e) { return; }

        if (!updated) return;

        try (java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(cartFile, false))) {
            for (String l : lines) { bw.write(l); bw.newLine(); }
            bw.flush();
        } catch (Exception ignored) {}
    }

    // ─────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────
    /** Writes/updates a 2-month late restriction for the student. */
    private void writeLateRestriction(String studentId, LocalDate until) {
        java.io.File f = new java.io.File("data/lateRestrictions.csv");
        List<String> lines = new ArrayList<>();
        boolean found = false;
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(f))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { lines.add(line); first = false; continue; }
                String[] p = line.split(",", -1);
                if (p.length > 0 && p[0].trim().equalsIgnoreCase(studentId)) {
                    lines.add(studentId + "," + until);
                    found = true;
                } else { lines.add(line); }
            }
        } catch (Exception ignored) {
            lines.add("StudentID,RestrictedUntil");
        }
        if (!found) lines.add(studentId + "," + until);
        try (java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(f, false))) {
            for (String l : lines) { bw.write(l); bw.newLine(); }
        } catch (Exception ignored) {}
    }

    private boolean isPending(IssuedBook book) {
        return book.getIssuedDate().equalsIgnoreCase("N/A")
                || book.getReturnDate().equalsIgnoreCase("N/A")
                || book.getIssuedId().startsWith("CART-");
    }

    private String calculateLateFee(String dueDateStr) {
        if (dueDateStr == null || dueDateStr.equalsIgnoreCase("N/A")) return "0";
        try {
            LocalDate dueDate = LocalDate.parse(dueDateStr);
            LocalDate today   = LocalDate.now();
            if (today.isAfter(dueDate)) {
                long fee = ChronoUnit.DAYS.between(dueDate, today) * 5;
                if (fee >= 150) fee += 300; // excess penalty for 30+ days overdue
                return String.valueOf(fee);
            }
        } catch (Exception ignored) {}
        return "0";
    }

    // ─────────────────────────────────────────────────────────────
    //  CLEAR / CANCEL
    // ─────────────────────────────────────────────────────────────
    @FXML private void handleCancel() { clearInfo(); }

    private void clearInfo() {
        if (issuedIdField   != null) issuedIdField.clear();
        if (issuedIdInfo    != null) issuedIdInfo.clear();
        if (bookIdInfo      != null) bookIdInfo.clear();
        if (studentIdInfo   != null) studentIdInfo.clear();
        if (studentNameInfo != null) studentNameInfo.clear();
        if (lateFeeField    != null) lateFeeField.clear();
        selectedBook = null;
        if (returnTable != null) returnTable.getSelectionModel().clearSelection();
    }

    // ─────────────────────────────────────────────────────────────
    //  NAVIGATION
    // ─────────────────────────────────────────────────────────────
    @FXML private void handleHome(ActionEvent e)          { new LoadStage("/mehrin/loginpage/Dashboard.fxml",      (Node)e.getSource(), true); }
    @FXML private void handleBooks(ActionEvent e)         { new LoadStage("/mehrin/loginpage/Books.fxml",          (Node)e.getSource(), true); }
    @FXML private void handleStudents(ActionEvent e)      { new LoadStage("/mehrin/loginpage/Students.fxml",       (Node)e.getSource(), true); }
    @FXML private void handleIssueBook(ActionEvent e)     { new LoadStage("/mehrin/loginpage/IssueBooks.fxml",     (Node)e.getSource(), true); }
    @FXML private void handleAllIssuedBooks(ActionEvent e){ new LoadStage("/mehrin/loginpage/AllIssuedBooks.fxml", (Node)e.getSource(), true); }
    @FXML private void handleAnnouncement(ActionEvent e)  { new LoadStage("/mehrin/loginpage/Announcements.fxml",  (Node)e.getSource(), true); }
    @FXML private void handleExport(ActionEvent e)        { new LoadStage("/mehrin/loginpage/Export.fxml",         (Node)e.getSource(), true); }
    @FXML private void handleClearance(ActionEvent e)     { new LoadStage("/mehrin/loginpage/Clearance.fxml",      (Node)e.getSource(), true); }
    @FXML private void logout(ActionEvent e)              { new LoadStage("/mehrin/loginpage/Login.fxml",          (Node)e.getSource(), true); }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setHeaderText(null);
        alert.setContentText(msg); alert.showAndWait();
    }
}