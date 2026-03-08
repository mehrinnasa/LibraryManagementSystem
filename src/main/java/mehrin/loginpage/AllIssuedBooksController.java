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
    private static final String CART_CSV    = "data/addToCart.csv";

    private IssuedBook selectedBook;

    // ─────────────────────────────────────────────────────────────
    //  INIT
    // ─────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {

        // Purge expired cart entries on every load
        CartExpiryUtil.purgeExpiredCartEntries(CART_CSV, ISSUED_FILE);

        // ── Table column bindings ──────────────────────────────
        issuedIdCol.setCellValueFactory(d -> d.getValue().issuedIdProperty());
        bookIdCol.setCellValueFactory(d -> d.getValue().bookIdProperty());
        studentIdCol.setCellValueFactory(d -> d.getValue().studentIdProperty());
        studentNameCol.setCellValueFactory(d -> d.getValue().studentNameProperty());
        issueDateCol.setCellValueFactory(d -> d.getValue().issuedDateProperty());
        dueDateCol.setCellValueFactory(d -> d.getValue().returnDateProperty());
        lateFeeCol.setCellValueFactory(d -> d.getValue().lateFeeProperty());

        // ── Load all books (including pending cart rows) ───────
        ObservableList<IssuedBook> allBooks = loadIssuedBooks();
        returnTable.setItems(allBooks);

        // ── Row selection ──────────────────────────────────────
        returnTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            selectedBook = newSel;
            if (newSel != null) populateInfo(newSel);
        });

        // ── Live search by student name ────────────────────────
        issuedIdField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                returnTable.setItems(allBooks);
                clearInfo();
                selectedBook = null;
                return;
            }

            String query = newVal.trim().toLowerCase();
            ObservableList<IssuedBook> filtered = FXCollections.observableArrayList();

            for (IssuedBook book : allBooks) {
                if (book.getStudentName().toLowerCase().contains(query)) {
                    filtered.add(book);
                }
            }

            if (!filtered.isEmpty()) {
                IssuedBook first = filtered.get(0);
                populateInfo(first);
                selectedBook = first;
            } else {
                clearInfo();
                selectedBook = null;
            }

            returnTable.setItems(filtered);
        });

        // ── Autocomplete ───────────────────────────────────────
        AutoCompleteHelper.setupAutoComplete(issuedIdField, text ->
                        FileUtil.readFile("issueBooks.csv").stream()
                                .skip(1)
                                .filter(line -> {
                                    String[] p = line.split(",", -1);
                                    return p.length > 3 && p[3].toLowerCase().contains(text.toLowerCase());
                                })
                                .map(line -> {
                                    String[] p = line.split(",", -1);
                                    return p.length > 3
                                            ? p[3] + " (" + p[2] + ") - Issued ID: " + p[0]
                                            : "";
                                })
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList()),
                chosen -> {
                    try {
                        String name = chosen.substring(0, chosen.indexOf(" ("));
                        issuedIdField.setText(name);
                    } catch (Exception ignored) {}
                });
    }

    // ─────────────────────────────────────────────────────────────
    //  LOAD ISSUED BOOK DETAILS  (search button / Enter)
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void loadIssuedBookDetails() {
        String issuedId = issuedIdField.getText().trim();
        if (issuedId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing", "Enter Issued ID");
            return;
        }

        ObservableList<IssuedBook> filtered = FXCollections.observableArrayList();
        for (IssuedBook book : loadIssuedBooks()) {
            if (book.getIssuedId().equalsIgnoreCase(issuedId)) {
                if (!isPending(book)) {
                    book.lateFeeProperty().set(calculateLateFee(book.getReturnDate()));
                }
                filtered.add(book);
            }
        }

        if (filtered.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Not Found", "No record found");
        }
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
        if (lateFeeField != null) lateFeeField.setText(fee);
        book.lateFeeProperty().set(fee);
    }

    // ─────────────────────────────────────────────────────────────
    //  SUBMIT RETURN
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void submitBook(ActionEvent event) {
        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "Error", "Select a book first");
            return;
        }

        // Prevent returning a book that hasn't been issued yet (pending cart)
        if (isPending(selectedBook)) {
            showAlert(Alert.AlertType.WARNING, "Not Issued Yet",
                    "This book is still a pending cart request and has not been issued. "
                            + "It cannot be returned until the librarian issues it.");
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

                returnTable.getItems().remove(selectedBook);
                clearInfo();

                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Book returned successfully. Late fee: " + fee + " Tk");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update files");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  CLEARANCE RECORD
    // ─────────────────────────────────────────────────────────────
    private void recordClearance(IssuedBook book) throws IOException {
        String bookTitle = getBookTitle(book.getBookId());
        String entry = String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                book.getStudentName(),
                bookTitle,
                book.getIssuedDate(),
                book.getReturnDate(),
                LocalDate.now().toString(),
                "N/A",
                java.time.LocalTime.now().toString(),
                book.getLateFee());

        List<String> lines = FileUtil.readFile("clearance.csv");
        lines.add(entry);
        FileUtil.writeFile("clearance.csv", lines,
                "StudentName,BookName,BorrowedDate,DueDate,ReturnDate,IssuedTime,ReturnTime,LateFee");
    }

    private String getBookTitle(String isbn) {
        if (isbn == null) return "Unknown Book";
        List<String> books = FileUtil.readFile("books.csv");
        for (String line : books) {
            String[] p = line.split(",", -1);
            if (p.length > 1 && p[0].trim().equalsIgnoreCase(isbn.trim())) return p[1].trim();
        }
        return "Unknown Book";
    }

    private void updateLateFeeInCSV(IssuedBook book) throws IOException {
        List<String> lines   = FileUtil.readFile(ISSUED_FILE);
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
        List<String> lines   = FileUtil.readFile(ISSUED_FILE);
        List<String> updated = new ArrayList<>();

        for (String line : lines) {
            if (!line.startsWith(selectedBook.getIssuedId() + ",")) updated.add(line);
        }

        FileUtil.writeFile(ISSUED_FILE, updated,
                "IssuedID,BookID,StudentID,StudentName,IssuedDate,ReturnDate,LateFee");
    }

    private void increaseRemainingBook() throws IOException {
        List<String> books   = FileUtil.readFile("books.csv");
        List<String> updated = new ArrayList<>();

        for (String line : books) {
            String[] p = line.split(",", -1);
            if (p[0].equalsIgnoreCase(selectedBook.getBookId())) {
                int remaining = Integer.parseInt(p[6]);
                remaining++;
                p[6] = String.valueOf(remaining);
                p[8] = "Available";
            }
            updated.add(String.join(",", p));
        }

        FileUtil.writeFile("books.csv", updated,
                "ISBN,Title,Author,Publisher,Edition,Quantity,Remaining,Section,Availability");
    }

    // ─────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────

    /** Returns true if this row is a pending cart reservation (dates = N/A) */
    private boolean isPending(IssuedBook book) {
        return book.getIssuedDate().equalsIgnoreCase("N/A")
                || book.getReturnDate().equalsIgnoreCase("N/A")
                || book.getIssuedId().startsWith("CART-");
    }

    /** Calculates late fee; returns "0" for pending rows or unparseable dates */
    private String calculateLateFee(String dueDateStr) {
        if (dueDateStr == null || dueDateStr.equalsIgnoreCase("N/A")) return "0";
        try {
            LocalDate dueDate = LocalDate.parse(dueDateStr);
            LocalDate today   = LocalDate.now();
            if (today.isAfter(dueDate)) {
                long daysLate = ChronoUnit.DAYS.between(dueDate, today);
                return String.valueOf(daysLate * 5);   // 5 taka per day
            }
        } catch (Exception ignored) {}
        return "0";
    }

    // ─────────────────────────────────────────────────────────────
    //  LOAD ALL ISSUED BOOKS  (including pending cart rows)
    // ─────────────────────────────────────────────────────────────
    private ObservableList<IssuedBook> loadIssuedBooks() {
        ObservableList<IssuedBook> list = FXCollections.observableArrayList();

        for (String line : FileUtil.readFile(ISSUED_FILE)) {
            String[] p = line.split(",", -1);
            if (p.length != 7) continue;

            // For pending rows show "N/A" in the fee column, not a computed value
            boolean pending    = p[4].equalsIgnoreCase("N/A") || p[0].startsWith("CART-");
            String  currentFee = pending ? "0" : calculateLateFee(p[5]);

            list.add(new IssuedBook(p[0], p[1], p[2], p[3], p[4], p[5], currentFee));
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────────
    //  CLEAR / CANCEL
    // ─────────────────────────────────────────────────────────────
    @FXML private void handleCancel() { clearInfo(); }

    private void clearInfo() {
        if (issuedIdField    != null) issuedIdField.clear();
        if (issuedIdInfo     != null) issuedIdInfo.clear();
        if (bookIdInfo       != null) bookIdInfo.clear();
        if (studentIdInfo    != null) studentIdInfo.clear();
        if (studentNameInfo  != null) studentNameInfo.clear();
        if (lateFeeField     != null) lateFeeField.clear();
        selectedBook = null;
        if (returnTable != null) returnTable.getSelectionModel().clearSelection();
    }

    // ─────────────────────────────────────────────────────────────
    //  NAVIGATION
    // ─────────────────────────────────────────────────────────────
    @FXML private void handleHome(ActionEvent e)          { new LoadStage("/mehrin/loginpage/Dashboard.fxml",       (Node)e.getSource(), true); }
    @FXML private void handleBooks(ActionEvent e)         { new LoadStage("/mehrin/loginpage/Books.fxml",           (Node)e.getSource(), true); }
    @FXML private void handleStudents(ActionEvent e)      { new LoadStage("/mehrin/loginpage/Students.fxml",        (Node)e.getSource(), true); }
    @FXML private void handleIssueBook(ActionEvent e)     { new LoadStage("/mehrin/loginpage/IssueBooks.fxml",      (Node)e.getSource(), true); }
    @FXML private void handleAllIssuedBooks(ActionEvent e){ new LoadStage("/mehrin/loginpage/AllIssuedBooks.fxml",  (Node)e.getSource(), true); }
    @FXML private void handleAnnouncement(ActionEvent e)  { new LoadStage("/mehrin/loginpage/Announcements.fxml",   (Node)e.getSource(), true); }
    @FXML private void handleExport(ActionEvent e)        { new LoadStage("/mehrin/loginpage/Export.fxml",          (Node)e.getSource(), true); }
    @FXML private void handleClearance(ActionEvent e)     { new LoadStage("/mehrin/loginpage/Clearance.fxml",       (Node)e.getSource(), true); }
    @FXML private void logout(ActionEvent e)              { new LoadStage("/mehrin/loginpage/Login.fxml",           (Node)e.getSource(), true); }

    // ─────────────────────────────────────────────────────────────
    //  ALERT
    // ─────────────────────────────────────────────────────────────
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}