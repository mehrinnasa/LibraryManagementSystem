package mehrin.loginpage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import mehrin.loginpage.Model.IssuedBook;
import mehrin.loginpage.Util.FileUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
public class AllIssuedBooksController {

    @FXML private TextField issuedIdField;
    @FXML private TextField issuedIdInfo;
    @FXML private TextField bookIdInfo;
    @FXML private TextField studentIdInfo;
    @FXML private TextField lateFeeField;

    @FXML private TableView<IssuedBook> returnTable;
    @FXML private TableColumn<IssuedBook, String> issuedIdCol;
    @FXML private TableColumn<IssuedBook, String> studentIdCol;
    @FXML private TableColumn<IssuedBook, String> studentNameCol;
    @FXML private TableColumn<IssuedBook, String> bookIdCol;
    @FXML private TableColumn<IssuedBook, String> issueDateCol;
    @FXML private TableColumn<IssuedBook, String> dueDateCol;
    @FXML private TableColumn<IssuedBook, String> lateFeeCol;

    private static final String ISSUED_FILE = "issueBooks.csv";

    private IssuedBook selectedBook;

    @FXML
    public void initialize() {

        issuedIdCol.setCellValueFactory(d -> d.getValue().issuedIdProperty());
        bookIdCol.setCellValueFactory(d -> d.getValue().bookIdProperty());
        studentIdCol.setCellValueFactory(d -> d.getValue().studentIdProperty());
        studentNameCol.setCellValueFactory(d -> d.getValue().studentNameProperty());
        issueDateCol.setCellValueFactory(d -> d.getValue().issuedDateProperty());
        dueDateCol.setCellValueFactory(d -> d.getValue().returnDateProperty());
        lateFeeCol.setCellValueFactory(d -> d.getValue().lateFeeProperty());

        returnTable.setItems(loadIssuedBooks());
    }

    // ================= LOAD DETAILS =================
    @FXML
    private void loadIssuedBookDetails() {

        String issuedId = issuedIdField.getText().trim();

        if (issuedId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing", "Enter Issued ID");
            return;
        }

        ObservableList<IssuedBook> list = FXCollections.observableArrayList();

        for (String line : FileUtil.readFile(ISSUED_FILE)) {

            String[] parts = line.split(",");

            if (parts.length >= 6 && parts[0].equalsIgnoreCase(issuedId)) {

                String dueDate = parts[5];
                String lateFee = calculateLateFee(dueDate);

                list.add(new IssuedBook(
                        parts[0], // IssuedID
                        parts[1], // BookID
                        parts[3], // StudentID
                        parts[4], // StudentName
                        parts[5], // IssuedDate
                        parts[6], // ReturnDate
                        parts[7]  // LateFee
                ));
            }
        }

        if (list.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Not Found", "No record found");
        }

        returnTable.setItems(list);
    }

    // ================= SUBMIT RETURN =================
    @FXML
    private void submitBook(ActionEvent event) {

        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "Error", "Select a book first");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Confirm Return?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {

            try {
                removeIssuedEntry();
                increaseRemainingBook();
                returnTable.getItems().remove(selectedBook);
                clearInfo();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book Returned Successfully");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update files");
            }
        }
    }

    // ================= REMOVE ISSUE =================
    private void removeIssuedEntry() throws IOException {

        List<String> lines = FileUtil.readFile(ISSUED_FILE);
        List<String> updated = new ArrayList<>();

        for (String line : lines) {
            if (!line.startsWith(selectedBook.getIssuedId() + ",")) {
                updated.add(line);
            }
        }

        FileUtil.writeFile(ISSUED_FILE, updated,
                "IssuedID,BookID,StudentID,StudentName,IssueDate,DueDate");
    }

    // ================= INCREASE BOOK =================
    private void increaseRemainingBook() throws IOException {

        List<String> books = FileUtil.readFile("books.csv");
        List<String> updated = new ArrayList<>();

        for (String line : books) {

            String[] parts = line.split(",");

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

    // ================= LATE FEE =================
    private String calculateLateFee(String dueDateStr) {

        LocalDate dueDate = LocalDate.parse(dueDateStr);
        LocalDate today = LocalDate.now();

        if (today.isAfter(dueDate)) {
            long daysLate = ChronoUnit.DAYS.between(dueDate, today);
            return String.valueOf(daysLate * 5); // 5 taka per day
        }
        return "0";
    }

    private void clearInfo() {
        issuedIdInfo.clear();
        bookIdInfo.clear();
        studentIdInfo.clear();
        lateFeeField.clear();
        selectedBook = null;
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

    @FXML private void handleCancel(){ clearInfo(); }

    private void showAlert(Alert.AlertType type, String title, String msg){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    private ObservableList<IssuedBook> loadIssuedBooks() {

        ObservableList<IssuedBook> list = FXCollections.observableArrayList();

        List<String> lines = FileUtil.readFile("issueBooks.csv");

        for (String line : lines) {
            String[] p = line.split(",", -1);

            if (p.length != 8) continue;

            list.add(new IssuedBook(
                    p[0], // IssuedID
                    p[1], // BookID
                    p[3], // StudentID
                    p[4], // StudentName
                    p[5], // IssuedDate
                    p[6], // ReturnDate
                    p[7]  // LateFee
            ));
        }
        return list;
    }
}