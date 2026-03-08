package mehrin.loginpage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import mehrin.loginpage.Model.ClearanceRecord;
import mehrin.loginpage.Util.FileUtil;

import java.util.List;

public class StudentClearanceController {

    @FXML private TextField searchField;

    @FXML private TableView<ClearanceRecord>           clearanceTable;
    @FXML private TableColumn<ClearanceRecord, String> studentNameCol;
    @FXML private TableColumn<ClearanceRecord, String> bookNameCol;
    @FXML private TableColumn<ClearanceRecord, String> borrowedDateCol;
    @FXML private TableColumn<ClearanceRecord, String> dueDateCol;
    @FXML private TableColumn<ClearanceRecord, String> returnDateCol;
    @FXML private TableColumn<ClearanceRecord, String> lateFeeCol;

    private ObservableList<ClearanceRecord> myRecords = FXCollections.observableArrayList();

    // ─────────────────────────────────────────────────────────────
    //  INIT
    // ─────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {

        studentNameCol.setCellValueFactory(d  -> d.getValue().studentNameProperty());
        bookNameCol.setCellValueFactory(d     -> d.getValue().bookNameProperty());
        borrowedDateCol.setCellValueFactory(d -> d.getValue().borrowedDateProperty());
        dueDateCol.setCellValueFactory(d      -> d.getValue().dueDateProperty());
        returnDateCol.setCellValueFactory(d   -> d.getValue().returnDateProperty());
        lateFeeCol.setCellValueFactory(d      -> d.getValue().lateFeeProperty());

        loadMyRecords();
        clearanceTable.setItems(myRecords);

        // Live search by book name
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                clearanceTable.setItems(myRecords);
                return;
            }
            String q = newVal.trim().toLowerCase();
            ObservableList<ClearanceRecord> filtered = FXCollections.observableArrayList();
            for (ClearanceRecord r : myRecords) {
                if (r.getBookName().toLowerCase().contains(q)) {
                    filtered.add(r);
                }
            }
            clearanceTable.setItems(filtered);
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  LOAD
    //  Uses the name already stored in SessionManager — no file
    //  lookup needed, no risk of wrong column index.
    //
    //  clearance.csv columns:
    //  0=StudentName, 1=BookName, 2=BorrowedDate, 3=DueDate,
    //  4=ReturnDate,  5=IssuedTime, 6=ReturnTime,  7=LateFee
    // ─────────────────────────────────────────────────────────────
    private void loadMyRecords() {
        myRecords.clear();

        // Use the name stored in session at login time
        String myName = SessionManager.getInstance().getLoggedInStudentName();
        if (myName == null || myName.trim().isEmpty()) return;

        List<String> lines = FileUtil.readFile("clearance.csv");

        for (String line : lines) {
            String[] p = line.split(",", -1);
            if (p.length < 1) continue;

            // Only this student's rows
            if (!p[0].trim().equalsIgnoreCase(myName.trim())) continue;

            if (p.length >= 8) {
                // Full row: Name,Book,BorrowedDate,DueDate,ReturnDate,IssuedTime,ReturnTime,LateFee
                myRecords.add(new ClearanceRecord(
                        p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(),
                        p[4].trim(), p[5].trim(), p[6].trim(), p[7].trim()));

            } else if (p.length == 7) {
                // Missing LateFee
                myRecords.add(new ClearanceRecord(
                        p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(),
                        p[4].trim(), p[5].trim(), p[6].trim(), "0"));

            } else if (p.length == 6) {
                // Missing ReturnTime and LateFee
                myRecords.add(new ClearanceRecord(
                        p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(),
                        p[4].trim(), p[5].trim(), "", "0"));
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  NAVIGATION
    // ─────────────────────────────────────────────────────────────
    @FXML private void loadHomePanel(ActionEvent e)          { new LoadStage("/mehrin/loginpage/StudentDashboard.fxml",      (Node)e.getSource(), true); }
    @FXML private void loadBooksPanel(ActionEvent e)         { new LoadStage("/mehrin/loginpage/StudentBooks.fxml",          (Node)e.getSource(), true); }
    @FXML private void loadAddToCartBooksPanel(ActionEvent e){ new LoadStage("/mehrin/loginpage/StudentAddToCart.fxml",      (Node)e.getSource(), true); }
    @FXML private void loadAllIssuedBooks(ActionEvent e)     { new LoadStage("/mehrin/loginpage/StudentAllIssuedBooks.fxml", (Node)e.getSource(), true); }
    @FXML private void loadAnnouncementPanel(ActionEvent e)  { new LoadStage("/mehrin/loginpage/StudentAnnouncement.fxml",   (Node)e.getSource(), true); }
    @FXML private void loadClearancePanel(ActionEvent e)     { new LoadStage("/mehrin/loginpage/StudentClearance.fxml",      (Node)e.getSource(), true); }
    @FXML private void logout(ActionEvent e)                 {
        SessionManager.getInstance().clear();
        new LoadStage("/mehrin/loginpage/Login.fxml", (Node)e.getSource(), true);
    }
}