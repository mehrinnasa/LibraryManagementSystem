package mehrin.loginpage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import mehrin.loginpage.Model.Book;
import mehrin.loginpage.Model.Student;
import mehrin.loginpage.Util.FileUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import mehrin.loginpage.Util.AutoCompleteHelper;

public class IssueBookController {

    @FXML private TextField serialField;
    @FXML private TextField bookSearchField, studentSearchTextField;
    @FXML private Text bookName, bookAuthor, bookPublisher, availability;
    @FXML private Text studentName, studentEmail, contact;

    private String boName  = "";
    private String stuName = "";

    // FileUtil prepends "data/" — use filename only
    private static final String ISSUED_FILE = "issueBooks.csv";
    private static final String CART_FILE   = "addToCart.csv";
    private static final String BOOKS_CSV   = "data/books.csv";
    private static final String STUDENT_CSV = "data/students.csv";

    private Book    selectedBook;
    private Student selectedStudent;

    // ─────────────────────────────────────────────────────────────
    //  INIT
    // ─────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        CartExpiryUtil.purgeExpiredCartEntries(CART_FILE, ISSUED_FILE);

        AutoCompleteHelper.setupAutoComplete(bookSearchField,
                text -> FileUtil.readFile("books.csv").stream()
                        .skip(1)
                        .filter(line -> line.toLowerCase().contains(text.toLowerCase()))
                        .map(line -> {
                            String[] p = line.split(",", -1);
                            return p.length > 1 ? p[1] + " (" + p[0] + ")" : "";
                        })
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()),
                chosen -> {
                    try {
                        String isbn = chosen.substring(
                                chosen.lastIndexOf("(") + 1, chosen.lastIndexOf(")"));
                        bookSearchField.setText(isbn);
                        searchBook(null);
                    } catch (Exception ignored) {}
                });

        AutoCompleteHelper.setupAutoComplete(studentSearchTextField,
                text -> FileUtil.readFile("students.csv").stream()
                        .skip(1)
                        .filter(line -> line.toLowerCase().contains(text.toLowerCase()))
                        .map(line -> {
                            String[] p = line.split(",", -1);
                            return p.length > 1 ? p[1] + " (" + p[0] + ")" : "";
                        })
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()),
                chosen -> {
                    try {
                        String id = chosen.substring(
                                chosen.lastIndexOf("(") + 1, chosen.lastIndexOf(")"));
                        studentSearchTextField.setText(id);
                        searchStudent(null);
                    } catch (Exception ignored) {}
                });
    }

    // ─────────────────────────────────────────────────────────────
    //  CART SERIAL SEARCH
    //  Fills panels silently while typing.
    //  Shows "Not Found" ONLY when Enter is pressed.
    //
    //  addToCart.csv columns:
    //  0=Serial, 1=StudentID, 2=StudentName, 3=BookISBN,
    //  4=BookName, 5=RequestDate, 6=ExpiryDate, 7=Status
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void searchCartRequest(KeyEvent event) {
        String serial = serialField.getText().trim();

        if (serial.isEmpty()) { clearAllFields(); return; }

        // Strip "CART-" prefix if librarian typed it (student sees "CART-1")
        String normalised = serial.toUpperCase().startsWith("CART-")
                ? serial.substring(5).trim() : serial;

        boolean found = tryFillFromCart(normalised);

        // Alert only on Enter
        if (!found && event != null && event.getCode() == KeyCode.ENTER) {
            showAlert("Not Found",
                    "No cart request found for serial: " + serial,
                    Alert.AlertType.WARNING);
        }
    }

    /**
     * Reads addToCart.csv and fills book/student panels for the given serial.
     * Returns true if a matching row was found.
     */
    private boolean tryFillFromCart(String serial) {
        List<String> cartLines = FileUtil.readFile(CART_FILE);

        for (String line : cartLines) {
            String[] p = line.split(",", -1);
            // col 0 = Serial
            if (p.length >= 8 && p[0].trim().equals(serial)) {
                // col 3 = BookISBN,  col 1 = StudentID
                bookSearchField.setText(p[3].trim());
                searchBook(null);

                studentSearchTextField.setText(p[1].trim());
                searchStudent(null);
                return true;
            }
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────────
    //  SEARCH BOOK
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void searchBook(KeyEvent event) {
        String isbn = bookSearchField.getText().trim();
        if (isbn.isEmpty()) { clearBookFields(); return; }

        try (BufferedReader br = new BufferedReader(new FileReader(BOOKS_CSV))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                Book book = Book.fromCSV(line);
                if (book != null && book.getIsbn().equals(isbn)) {
                    selectedBook = book;
                    boName = book.getTitle();
                    bookName.setText(book.getTitle());
                    bookAuthor.setText(book.getAuthor());
                    bookPublisher.setText(book.getPublisher());
                    availability.setText(book.getAvailability());
                    return;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        clearBookFields();
    }

    // ─────────────────────────────────────────────────────────────
    //  SEARCH STUDENT
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void searchStudent(KeyEvent event) {
        String studentId = studentSearchTextField.getText().trim();
        if (studentId.isEmpty()) { clearStudentFields(); return; }

        try (BufferedReader br = new BufferedReader(new FileReader(STUDENT_CSV))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                Student student = Student.fromCSV(line);
                if (student != null && student.getStudentId().equals(studentId)) {
                    selectedStudent = student;
                    stuName = student.getName();
                    studentName.setText(student.getName());
                    contact.setText(student.getPhone());
                    studentEmail.setText(student.getEmail());
                    return;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        clearStudentFields();
    }

    // ─────────────────────────────────────────────────────────────
    //  ISSUE BOOK
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void issueBook(ActionEvent event) {
        if (boName.isEmpty() || stuName.isEmpty()) {
            showAlert("Missing Info", "Please search both book and student",
                    Alert.AlertType.WARNING);
            return;
        }

        boolean alreadyIssued = FileUtil.readFile(ISSUED_FILE).stream()
                .anyMatch(line -> {
                    String[] p = line.split(",", -1);
                    return p.length > 4
                            && p[1].equalsIgnoreCase(bookSearchField.getText().trim())
                            && p[2].equalsIgnoreCase(studentSearchTextField.getText().trim())
                            && !p[4].equalsIgnoreCase("N/A");
                });

        if (alreadyIssued) {
            showAlert("Already Issued", stuName + " already has this book issued",
                    Alert.AlertType.INFORMATION);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Issue");
        confirm.setHeaderText(null);
        confirm.setContentText("Issue \"" + boName + "\" to \"" + stuName + "\"?");
        Optional<ButtonType> res = confirm.showAndWait();

        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                issueBookInFile();
                if (serialField != null) serialField.clear();
                clearAllFields();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to issue book", Alert.AlertType.ERROR);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  FILE UPDATE
    // ─────────────────────────────────────────────────────────────
    private void issueBookInFile() throws IOException {

        // 1. Decrease remaining in books.csv
        List<String> books        = FileUtil.readFile("books.csv");
        List<String> updatedBooks = new ArrayList<>();
        boolean bookFound = false;

        for (String line : books) {
            String[] p = line.split(",", -1);
            if (p[0].equalsIgnoreCase(bookSearchField.getText().trim())) {
                bookFound = true;
                int remaining = Integer.parseInt(p[6].trim());
                if (remaining <= 0) {
                    showAlert("Not Available", "This book is not available",
                            Alert.AlertType.WARNING);
                    return;
                }
                remaining--;
                p[6] = String.valueOf(remaining);
                p[7] = (remaining == 0) ? "Not Available" : "Available";
            }
            updatedBooks.add(String.join(",", p));
        }

        if (!bookFound) {
            showAlert("Error", "Book not found in records", Alert.AlertType.ERROR);
            return;
        }

        FileUtil.writeFile("books.csv", updatedBooks,
                "ISBN,Title,Author,Publisher,Edition,Quantity,Remaining,Section,Availability");

        // 2. Update issueBooks.csv
        LocalDate today   = LocalDate.now();
        LocalDate dueDate = today.plusDays(14);

        List<String> issuedLines   = new ArrayList<>(FileUtil.readFile(ISSUED_FILE));
        List<String> updatedIssued = new ArrayList<>();

        // Normalise serial – strip "CART-" prefix if present
        String cartSerial = (serialField != null) ? serialField.getText().trim() : "";
        if (cartSerial.toUpperCase().startsWith("CART-")) {
            cartSerial = cartSerial.substring(5).trim();
        }
        String cartKey = "CART-" + cartSerial;

        // Find next numeric ID
        int lastId = -1;
        for (String line : issuedLines) {
            String[] p = line.split(",", -1);
            if (p.length >= 1 && !p[0].equalsIgnoreCase("IssuedID")
                    && !p[0].startsWith("CART-")) {
                try {
                    int id = Integer.parseInt(p[0]);
                    if (id > lastId) lastId = id;
                } catch (Exception ignored) {}
            }
        }
        int newIssuedId = lastId + 1;

        boolean pendingUpdated = false;

        for (String line : issuedLines) {
            String[] p = line.split(",", -1);
            if (!cartSerial.isEmpty() && p.length >= 7
                    && p[0].equalsIgnoreCase(cartKey)) {
                // Replace pending CART- row with real issue data
                p[0] = String.valueOf(newIssuedId);
                p[4] = today.toString();
                p[5] = dueDate.toString();
                p[6] = "0";
                updatedIssued.add(String.join(",", p));
                pendingUpdated = true;
            } else {
                updatedIssued.add(line);
            }
        }

        // No pending row (direct issue without cart)
        if (!pendingUpdated) {
            updatedIssued.add(String.join(",",
                    String.valueOf(newIssuedId),
                    bookSearchField.getText().trim(),
                    studentSearchTextField.getText().trim(),
                    stuName,
                    today.toString(),
                    dueDate.toString(),
                    "0"));
        }

        FileUtil.writeFile(ISSUED_FILE, updatedIssued,
                "IssuedID,BookID,StudentID,StudentName,IssuedDate,ReturnDate,LateFee");

        // 3. Remove from addToCart.csv
        if (!cartSerial.isEmpty()) {
            removeFromCart(cartSerial);
        }

        showAlert("Success", "Book issued successfully!", Alert.AlertType.INFORMATION);
    }

    /** Remove row by Serial number from addToCart.csv */
    private void removeFromCart(String serial) throws IOException {
        // Already normalised (no CART- prefix) when called from issueBookInFile
        List<String> cartLines = FileUtil.readFile(CART_FILE);
        List<String> updated   = new ArrayList<>();

        for (String line : cartLines) {
            String[] p = line.split(",", -1);
            // Keep everything except the matched serial row
            if (p.length == 0 || !p[0].trim().equals(serial)) {
                updated.add(line);
            }
        }

        FileUtil.writeFile(CART_FILE, updated,
                "Serial,StudentID,StudentName,BookISBN,BookName,RequestDate,ExpiryDate,Status");
    }

    // ─────────────────────────────────────────────────────────────
    //  CLEAR HELPERS
    // ─────────────────────────────────────────────────────────────
    private void clearBookFields() {
        selectedBook = null; boName = "";
        bookName.setText("-"); bookAuthor.setText("-");
        bookPublisher.setText("-"); availability.setText("-");
    }

    private void clearStudentFields() {
        selectedStudent = null; stuName = "";
        studentName.setText("-"); studentEmail.setText("-"); contact.setText("-");
    }

    private void clearAllFields() {
        clearBookFields(); clearStudentFields();
        if (bookSearchField != null)        bookSearchField.clear();
        if (studentSearchTextField != null) studentSearchTextField.clear();
    }

    @FXML
    private void cancel(ActionEvent event) {
        if (serialField != null) serialField.clear();
        clearAllFields();
    }

    // ─────────────────────────────────────────────────────────────
    //  NAVIGATION
    // ─────────────────────────────────────────────────────────────
    @FXML private void loadHomePanel(ActionEvent e)             { new LoadStage("/mehrin/loginpage/Dashboard.fxml",       (Node)e.getSource(), true); }
    @FXML private void loadBooksPanel(ActionEvent e)            { new LoadStage("/mehrin/loginpage/Books.fxml",           (Node)e.getSource(), true); }
    @FXML private void loadStudentPanel(ActionEvent e)          { new LoadStage("/mehrin/loginpage/Students.fxml",        (Node)e.getSource(), true); }
    @FXML private void loadIssueBooksPanel(ActionEvent e)       { new LoadStage("/mehrin/loginpage/IssueBooks.fxml",      (Node)e.getSource(), true); }
    @FXML private void viewAllIssuedBooks(ActionEvent e)        { new LoadStage("/mehrin/loginpage/AllIssuedBooks.fxml",  (Node)e.getSource(), true); }
    @FXML private void loadSendAnnouncementsPanel(ActionEvent e){ new LoadStage("/mehrin/loginpage/Announcements.fxml",   (Node)e.getSource(), true); }
    @FXML private void loadExportDataPanel(ActionEvent e)       { new LoadStage("/mehrin/loginpage/Export.fxml",          (Node)e.getSource(), true); }
    @FXML private void loadClearancePanel(ActionEvent e)        { new LoadStage("/mehrin/loginpage/Clearance.fxml",       (Node)e.getSource(), true); }
    @FXML private void logout(ActionEvent e)                    { new LoadStage("/mehrin/loginpage/Login.fxml",           (Node)e.getSource(), true); }

    // ─────────────────────────────────────────────────────────────
    //  ALERT
    // ─────────────────────────────────────────────────────────────
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setHeaderText(null);
        alert.setContentText(message); alert.showAndWait();
    }
}