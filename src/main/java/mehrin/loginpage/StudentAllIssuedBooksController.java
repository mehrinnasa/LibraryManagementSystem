package mehrin.loginpage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import mehrin.loginpage.Model.IssuedBook;
import mehrin.loginpage.Util.AutoCompleteHelper;
import mehrin.loginpage.Util.FileUtil;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

public class StudentAllIssuedBooksController {

    // ── Search ─────────────────────────────────────────────────
    @FXML private TextField issuedIdField;

    // ── Table ──────────────────────────────────────────────────
    @FXML private TableView<IssuedBook>           returnTable;
    @FXML private TableColumn<IssuedBook, String> issuedIdCol;
    @FXML private TableColumn<IssuedBook, String> studentIdCol;
    @FXML private TableColumn<IssuedBook, String> studentNameCol;
    @FXML private TableColumn<IssuedBook, String> bookIdCol;
    @FXML private TableColumn<IssuedBook, String> issueDateCol;
    @FXML private TableColumn<IssuedBook, String> dueDateCol;
    @FXML private TableColumn<IssuedBook, String> lateFeeCol;

    // ── Info panel (read-only) ─────────────────────────────────
    @FXML private TextField issuedIdInfo;
    @FXML private TextField bookIdInfo;
    @FXML private TextField studentIdInfo;
    @FXML private TextField lateFeeField;

    private static final String ISSUED_FILE = "issueBooks.csv";
    private static final String CART_CSV    = "data/addToCart.csv";

    // Only this student's books are ever loaded
    private ObservableList<IssuedBook> myBooks;

    // ─────────────────────────────────────────────────────────────
    //  INIT
    // ─────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {

        // Purge expired cart entries on every load
        CartExpiryUtil.purgeExpiredCartEntries(CART_CSV, ISSUED_FILE);

        // ── Column bindings ────────────────────────────────────
        issuedIdCol.setCellValueFactory(d    -> d.getValue().issuedIdProperty());
        studentIdCol.setCellValueFactory(d   -> d.getValue().studentIdProperty());
        studentNameCol.setCellValueFactory(d -> d.getValue().studentNameProperty());
        bookIdCol.setCellValueFactory(d      -> d.getValue().bookIdProperty());
        issueDateCol.setCellValueFactory(d   -> d.getValue().issuedDateProperty());
        dueDateCol.setCellValueFactory(d     -> d.getValue().returnDateProperty());
        lateFeeCol.setCellValueFactory(d     -> d.getValue().lateFeeProperty());

        // ── Load ONLY this student's books ─────────────────────
        String myId = SessionManager.getInstance().getLoggedInStudentId();
        myBooks = loadMyBooks(myId);
        returnTable.setItems(myBooks);

        // ── Row click → fill info panel ────────────────────────
        returnTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSel, newSel) -> {
                    if (newSel != null) populateInfo(newSel);
                });

        // ── Live search (by Book ID or Issued ID within my list)
        issuedIdField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                returnTable.setItems(myBooks);
                clearInfo();
                return;
            }

            String q = newVal.trim().toLowerCase();
            ObservableList<IssuedBook> filtered = FXCollections.observableArrayList(
                    myBooks.stream()
                            .filter(b -> b.getIssuedId().toLowerCase().contains(q)
                                    || b.getBookId().toLowerCase().contains(q))
                            .collect(Collectors.toList())
            );

            returnTable.setItems(filtered);
            if (!filtered.isEmpty()) populateInfo(filtered.get(0));
            else clearInfo();
        });

        // ── Autocomplete (Book ID suggestions from my list) ────
        AutoCompleteHelper.setupAutoComplete(issuedIdField,
                text -> myBooks.stream()
                        .filter(b -> b.getBookId().toLowerCase().contains(text.toLowerCase())
                                || b.getIssuedId().toLowerCase().contains(text.toLowerCase()))
                        .map(b -> b.getBookId() + " (Issued ID: " + b.getIssuedId() + ")")
                        .distinct()
                        .collect(Collectors.toList()),
                chosen -> {
                    try {
                        // Extract Book ID from "BookID (Issued ID: X)"
                        String bookId = chosen.substring(0, chosen.indexOf(" ("));
                        issuedIdField.setText(bookId);
                    } catch (Exception ignored) {}
                });
    }

    // ─────────────────────────────────────────────────────────────
    //  LOAD ONLY THIS STUDENT'S BOOKS
    // ─────────────────────────────────────────────────────────────
    private ObservableList<IssuedBook> loadMyBooks(String studentId) {
        ObservableList<IssuedBook> list = FXCollections.observableArrayList();

        for (String line : FileUtil.readFile(ISSUED_FILE)) {
            String[] p = line.split(",", -1);
            if (p.length != 7) continue;

            // ── Filter: only rows belonging to this student ────
            if (!p[2].equalsIgnoreCase(studentId)) continue;

            boolean pending    = p[4].equalsIgnoreCase("N/A") || p[0].startsWith("CART-");
            String  currentFee = pending ? "0" : calculateLateFee(p[5]);

            list.add(new IssuedBook(p[0], p[1], p[2], p[3], p[4], p[5], currentFee));
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────────
    //  POPULATE INFO PANEL
    // ─────────────────────────────────────────────────────────────
    private void populateInfo(IssuedBook book) {
        issuedIdInfo.setText(book.getIssuedId());
        bookIdInfo.setText(book.getBookId());
        studentIdInfo.setText(book.getStudentId());

        if (isPending(book)) {
            lateFeeField.setText("0  (Pending – not issued yet)");
        } else {
            String fee = calculateLateFee(book.getReturnDate());
            book.lateFeeProperty().set(fee);
            lateFeeField.setText(fee + " Tk");
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────
    private boolean isPending(IssuedBook book) {
        return book.getIssuedDate().equalsIgnoreCase("N/A")
                || book.getReturnDate().equalsIgnoreCase("N/A")
                || book.getIssuedId().startsWith("CART-");
    }

    private String calculateLateFee(String dueDateStr) {
        if (dueDateStr == null || dueDateStr.equalsIgnoreCase("N/A")) return "0";
        try {
            LocalDate due   = LocalDate.parse(dueDateStr);
            LocalDate today = LocalDate.now();
            if (today.isAfter(due)) {
                long daysLate = ChronoUnit.DAYS.between(due, today);
                return String.valueOf(daysLate * 5);
            }
        } catch (Exception ignored) {}
        return "0";
    }

    // ─────────────────────────────────────────────────────────────
    //  CANCEL / CLEAR
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void handleCancel() {
        clearInfo();
        issuedIdField.clear();
        returnTable.setItems(myBooks);
        returnTable.getSelectionModel().clearSelection();
    }

    private void clearInfo() {
        issuedIdInfo.clear();
        bookIdInfo.clear();
        studentIdInfo.clear();
        lateFeeField.clear();
    }

    // ─────────────────────────────────────────────────────────────
    //  NAVIGATION
    // ─────────────────────────────────────────────────────────────
    @FXML private void loadHomePanel(ActionEvent e)          { new LoadStage("/mehrin/loginpage/StudentDashboard.fxml",      (Node)e.getSource(), true); }
    @FXML private void loadBooksPanel(ActionEvent e)         { new LoadStage("/mehrin/loginpage/StudentBooks.fxml",          (Node)e.getSource(), true); }
    @FXML private void loadAddToCartBooksPanel(ActionEvent e){ new LoadStage("/mehrin/loginpage/StudentAddToCart.fxml", (Node)e.getSource(), true); }
    @FXML private void loadAllIssuedBooks(ActionEvent e)     { new LoadStage("/mehrin/loginpage/StudentAllIssuedBooks.fxml", (Node)e.getSource(), true); }
    @FXML private void loadAnnouncementPanel(ActionEvent e)  { new LoadStage("/mehrin/loginpage/StudentAnnouncement.fxml",   (Node)e.getSource(), true); }
    @FXML private void loadClearancePanel(ActionEvent e)     { new LoadStage("/mehrin/loginpage/StudentClearance.fxml",      (Node)e.getSource(), true); }
    @FXML private void logout(ActionEvent e)                 {
        SessionManager.getInstance().clear();   // clear session on logout
        new LoadStage("/mehrin/loginpage/Login.fxml", (Node)e.getSource(), true);
    }
}