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

public class ClearanceController {

    @FXML
    private TextField searchField;
    @FXML
    private TableView<ClearanceRecord> clearanceTable;
    @FXML
    private TableColumn<ClearanceRecord, String> studentNameCol;
    @FXML
    private TableColumn<ClearanceRecord, String> bookNameCol;
    @FXML
    private TableColumn<ClearanceRecord, String> borrowedDateCol;
    @FXML
    private TableColumn<ClearanceRecord, String> dueDateCol;
    @FXML
    private TableColumn<ClearanceRecord, String> returnDateCol;
    @FXML
    private TableColumn<ClearanceRecord, String> lateFeeCol;

    private ObservableList<ClearanceRecord> allRecords = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        studentNameCol.setCellValueFactory(d -> d.getValue().studentNameProperty());
        bookNameCol.setCellValueFactory(d -> d.getValue().bookNameProperty());
        borrowedDateCol.setCellValueFactory(d -> d.getValue().borrowedDateProperty());
        dueDateCol.setCellValueFactory(d -> d.getValue().dueDateProperty());
        returnDateCol.setCellValueFactory(d -> d.getValue().returnDateProperty());
        lateFeeCol.setCellValueFactory(d -> d.getValue().lateFeeProperty());

        loadClearanceData();
        clearanceTable.setItems(allRecords);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                clearanceTable.setItems(allRecords);
                return;
            }
            String query = newVal.toLowerCase().trim();
            ObservableList<ClearanceRecord> filtered = FXCollections.observableArrayList();
            for (ClearanceRecord r : allRecords) {
                if (r.getStudentName().toLowerCase().contains(query) ||
                        r.getBookName().toLowerCase().contains(query)) {
                    filtered.add(r);
                }
            }
            clearanceTable.setItems(filtered);
        });
    }

    private void loadClearanceData() {
        allRecords.clear();
        List<String> lines = FileUtil.readFile("clearance.csv");
        if (lines.isEmpty())
            return;

        for (int i = 0; i < lines.size(); i++) {
            String[] p = lines.get(i).split(",", -1);
            if (p.length >= 8) {
                // p[0]:Name, p[1]:Book, p[2]:BorrowedDate, p[3]:DueDate, p[4]:ReturnDate,
                // p[5]:IssTime, p[6]:RetTime, p[7]:Fee
                allRecords.add(new ClearanceRecord(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7]));
            } else if (p.length >= 7) {
                // Backward compatibility for 7 columns (no DueDate)
                allRecords.add(new ClearanceRecord(p[0], p[1], p[2], "N/A", p[3], p[4], p[5], p[6]));
            } else if (p.length >= 6) {
                // Backward compatibility for 6 columns
                allRecords.add(new ClearanceRecord(p[0], p[1], p[2], "N/A", p[3], p[4], p[5], "0"));
            }
        }
    }

    @FXML
    private void handleHome(ActionEvent e) {
        new LoadStage("/mehrin/loginpage/Dashboard.fxml", (Node) e.getSource(), true);
    }

    @FXML
    private void handleBooks(ActionEvent e) {
        new LoadStage("/mehrin/loginpage/Books.fxml", (Node) e.getSource(), true);
    }

    @FXML
    private void handleStudents(ActionEvent e) {
        new LoadStage("/mehrin/loginpage/Students.fxml", (Node) e.getSource(), true);
    }

    @FXML
    private void handleIssueBook(ActionEvent e) {
        new LoadStage("/mehrin/loginpage/IssueBooks.fxml", (Node) e.getSource(), true);
    }

    @FXML
    private void handleAllIssuedBooks(ActionEvent e) {
        new LoadStage("/mehrin/loginpage/AllIssuedBooks.fxml", (Node) e.getSource(), true);
    }

    @FXML
    private void handleAnnouncement(ActionEvent e) {
        new LoadStage("/mehrin/loginpage/Announcements.fxml", (Node) e.getSource(), true);
    }

    @FXML
    private void handleExport(ActionEvent e) {
        new LoadStage("/mehrin/loginpage/Export.fxml", (Node) e.getSource(), true);
    }

    @FXML
    private void handleClearance(ActionEvent e) {
        new LoadStage("/mehrin/loginpage/Clearance.fxml", (Node) e.getSource(), true);
    }

    @FXML
    private void logout(ActionEvent e) {
        new LoadStage("/mehrin/loginpage/Login.fxml", (Node) e.getSource(), true);
    }
}
