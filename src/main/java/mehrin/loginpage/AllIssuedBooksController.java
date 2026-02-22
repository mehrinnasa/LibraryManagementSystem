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
    private IssuedBook selectedBook;

    @FXML
    public void initialize() {
        // ================= TABLE COLUMNS =================
        issuedIdCol.setCellValueFactory(d -> d.getValue().issuedIdProperty());
        bookIdCol.setCellValueFactory(d -> d.getValue().bookIdProperty());
        studentIdCol.setCellValueFactory(d -> d.getValue().studentIdProperty());
        studentNameCol.setCellValueFactory(d -> d.getValue().studentNameProperty());
        issueDateCol.setCellValueFactory(d -> d.getValue().issuedDateProperty());
        dueDateCol.setCellValueFactory(d -> d.getValue().returnDateProperty());
        lateFeeCol.setCellValueFactory(d -> d.getValue().lateFeeProperty());

        // ================= LOAD ALL ISSUED BOOKS =================
        ObservableList<IssuedBook> allBooks = loadIssuedBooks();
        returnTable.setItems(allBooks);

        // ================= ROW SELECTION =================
        returnTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            selectedBook = newSel;
            if (newSel != null) populateInfo(newSel);
        });

        // ================= LIVE SEARCH =================
        issuedIdField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                // show all
                returnTable.setItems(allBooks);
                clearInfo();
                selectedBook = null;
                return;
            }

            ObservableList<IssuedBook> filtered = FXCollections.observableArrayList();
            String query = newVal.trim();

            for (IssuedBook book : allBooks) {
                // match if IssuedID starts with query
                if (book.getIssuedId().startsWith(query)) {
                    filtered.add(book);
                }
            }

            // Update info fields with the first match
            if (!filtered.isEmpty()) {
                IssuedBook first = filtered.get(0);
                populateInfo(first); // use your existing populateInfo method
                selectedBook = first;
            } else {
                clearInfo();
                selectedBook = null;
            }

            returnTable.setItems(filtered);
        });
    }

    // ================= LOAD BOOK DETAILS =================
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
                // Update late fee
                String fee = calculateLateFee(book.getReturnDate());
                book.lateFeeProperty().set(fee);
                filtered.add(book);
            }
        }

        if (filtered.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Not Found", "No record found");
        }

        returnTable.setItems(filtered);
    }

    // ================= POPULATE INFO =================
    private void populateInfo(IssuedBook book) {
        issuedIdInfo.setText(book.getIssuedId());
        bookIdInfo.setText(book.getBookId());
        studentIdInfo.setText(book.getStudentId());
        studentNameInfo.setText(book.getStudentName());

        // Calculate LateFee dynamically, even if 0
        String fee = calculateLateFee(book.getReturnDate());
        lateFeeField.setText(fee);

        // Also update the table's lateFee property so column shows it
        book.lateFeeProperty().set(fee);
    }

    // ================= SUBMIT RETURN =================
    @FXML
    private void submitBook(ActionEvent event) {
        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "Error", "Select a book first");
            return;
        }

        // Calculate LateFee before return
        String fee = calculateLateFee(selectedBook.getReturnDate());
        selectedBook.lateFeeProperty().set(fee);
        lateFeeField.setText(fee);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Confirm return?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Update LateFee in CSV first
                updateLateFeeInCSV(selectedBook);

                // Remove issued entry
                removeIssuedEntry();

                // Increase Remaining in books.csv
                increaseRemainingBook();

                // Remove from TableView
                returnTable.getItems().remove(selectedBook);

                // Clear info panel
                clearInfo();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Book returned successfully. Late fee: " + fee + " Tk");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update files");
            }
        }
    }

    /**
     * Update LateFee for the selected book in issueBooks.csv
     */
    private void updateLateFeeInCSV(IssuedBook book) throws IOException {
        List<String> lines = FileUtil.readFile(ISSUED_FILE);
        List<String> updated = new ArrayList<>();

        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length != 7) continue;

            if (parts[0].equalsIgnoreCase(book.getIssuedId())) {
                // Update LateFee column (index 6)
                parts[6] = book.getLateFee();
            }

            updated.add(String.join(",", parts));
        }

        FileUtil.writeFile(ISSUED_FILE, updated,
                "IssuedID,BookID,StudentID,StudentName,IssuedDate,ReturnDate,LateFee");
    }

    // ================= REMOVE ISSUED ENTRY =================
    private void removeIssuedEntry() throws IOException {
        List<String> lines = FileUtil.readFile(ISSUED_FILE);
        List<String> updated = new ArrayList<>();

        for (String line : lines) {
            if (!line.startsWith(selectedBook.getIssuedId() + ",")) {
                updated.add(line);
            }
        }

        FileUtil.writeFile(ISSUED_FILE, updated,
                "IssuedID,BookID,StudentID,StudentName,IssuedDate,ReturnDate,LateFee");
    }

    // ================= INCREASE BOOK QUANTITY =================
    private void increaseRemainingBook() throws IOException {
        List<String> books = FileUtil.readFile("books.csv");
        List<String> updated = new ArrayList<>();

        for (String line : books) {
            String[] parts = line.split(",", -1);
            if (parts[0].equalsIgnoreCase(selectedBook.getBookId())) {
                int remaining = Integer.parseInt(parts[6]);
                remaining++;
                parts[6] = String.valueOf(remaining);
                parts[8] = "Available";
            }
            updated.add(String.join(",", parts));
        }

        FileUtil.writeFile("books.csv", updated,
                "ISBN,Title,Author,Publisher,Edition,Quantity,Remaining,Section,Availability");
    }

    // ================= CALCULATE LATE FEE =================
    private String calculateLateFee(String dueDateStr) {
        LocalDate dueDate = LocalDate.parse(dueDateStr);
        LocalDate today = LocalDate.now();
        if (today.isAfter(dueDate)) {
            long daysLate = ChronoUnit.DAYS.between(dueDate, today);
            return String.valueOf(daysLate * 5); // 5 taka per day
        }
        return "0";
    }

    // ================= CLEAR FORM =================
    @FXML private void handleCancel() { clearInfo(); }
    private void clearInfo() {
        issuedIdField.clear();
        issuedIdInfo.clear();
        bookIdInfo.clear();
        studentIdInfo.clear();
        studentNameInfo.clear();
        lateFeeField.clear();
        selectedBook = null;
        returnTable.getSelectionModel().clearSelection();
    }

    // ================= LOAD ALL ISSUED BOOKS =================
    private ObservableList<IssuedBook> loadIssuedBooks() {
        ObservableList<IssuedBook> list = FXCollections.observableArrayList();
        List<String> lines = FileUtil.readFile(ISSUED_FILE);

        for (String line : lines) {
            String[] p = line.split(",", -1);
            if (p.length != 7) continue;
            list.add(new IssuedBook(
                    p[0], // IssuedID
                    p[1], // BookID
                    p[2], // StudentID
                    p[3], // StudentName
                    p[4], // IssuedDate
                    p[5], // ReturnDate
                    p[6]  // LateFee
            ));
        }
        return list;
    }

    // ================= NAVIGATION =================
    @FXML private void handleHome(ActionEvent e){ new LoadStage("/mehrin/loginpage/Dashboard.fxml",(Node)e.getSource(),true); }
    @FXML private void handleBooks(ActionEvent e){ new LoadStage("/mehrin/loginpage/Books.fxml",(Node)e.getSource(),true); }
    @FXML private void handleStudents(ActionEvent e){ new LoadStage("/mehrin/loginpage/Students.fxml",(Node)e.getSource(),true); }
    @FXML private void handleIssueBook(ActionEvent e){ new LoadStage("/mehrin/loginpage/IssueBooks.fxml",(Node)e.getSource(),true); }
    @FXML private void handleReturnBook(ActionEvent e){ }
    @FXML private void handleAllIssuedBooks(ActionEvent e){ new LoadStage("/mehrin/loginpage/AllIssuedBooks.fxml",(Node)e.getSource(),true); }
    @FXML private void handleAnnouncement(ActionEvent e){ new LoadStage("/mehrin/loginpage/Announcements.fxml",(Node)e.getSource(),true); }
    @FXML private void handleExport(ActionEvent e){ new LoadStage("/mehrin/loginpage/Export.fxml",(Node)e.getSource(),true); }
    @FXML private void handleClearance(ActionEvent e){ new LoadStage("/mehrin/loginpage/Clearance.fxml",(Node)e.getSource(),true); }
    @FXML private void logout(ActionEvent e){ new LoadStage("/mehrin/loginpage/Login.fxml",(Node)e.getSource(),true); }

    private void showAlert(Alert.AlertType type, String title, String msg){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}