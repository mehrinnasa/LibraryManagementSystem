package mehrin.loginpage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import mehrin.loginpage.Model.Book;
import mehrin.loginpage.Model.Student;
import mehrin.loginpage.Util.AutoCompleteHelper;
import mehrin.loginpage.Util.FileUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StudentAddToCartController {

    // ── Book panel only ────────────────────────────────────────
    @FXML private TextField bookSearchField;
    @FXML private Text      bookName, bookAuthor, bookPublisher, availability;

    private static final String BOOKS_CSV   = "data/books.csv";
    private static final String STUDENT_CSV = "data/students.csv";
    private static final String CART_FILE   = "addToCart.csv";
    private static final String ISSUED_FILE = "issueBooks.csv";

    private Book    selectedBook;
    private Student loggedInStudent;

    // ─────────────────────────────────────────────────────────────
    //  INIT
    // ─────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        CartExpiryUtil.purgeExpiredCartEntries(CART_FILE, ISSUED_FILE);
        loadLoggedInStudent();

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
    }

    /** Load student object from session so we have it ready for cart logic */
    private void loadLoggedInStudent() {
        String myId = SessionManager.getInstance().getLoggedInStudentId();
        if (myId == null || myId.isEmpty()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(STUDENT_CSV))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                Student s = Student.fromCSV(line);
                if (s != null && s.getStudentId().equals(myId)) {
                    loggedInStudent = s;
                    return;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─────────────────────────────────────────────────────────────
    //  SEARCH BOOK
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void searchBook(KeyEvent event) {
        String isbn = bookSearchField.getText().trim();
        if (isbn.isEmpty()) { clearBookInfo(); return; }

        try (BufferedReader br = new BufferedReader(new FileReader(BOOKS_CSV))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                Book book = Book.fromCSV(line);
                if (book != null && (book.getIsbn().equals(isbn)
                        || book.getTitle().toLowerCase().contains(isbn.toLowerCase()))) {
                    selectedBook = book;
                    bookName.setText(book.getTitle());
                    bookAuthor.setText(book.getAuthor());
                    bookPublisher.setText(book.getPublisher());
                    availability.setText(book.getAvailability());
                    return;
                }
            }
            // No match yet — silently clear book info but DO NOT clear the search field
            clearBookInfo();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─────────────────────────────────────────────────────────────
    //  ADD TO CART
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void issueBook(ActionEvent event) {
        if (selectedBook == null) {
            showAlert("Missing Info", "Please search for a book first.",
                    Alert.AlertType.WARNING);
            return;
        }
        if (loggedInStudent == null) {
            showAlert("Session Error", "Student session not found. Please log in again.",
                    Alert.AlertType.ERROR);
            return;
        }

        // Check if this student is restricted from adding THIS book to cart
        java.time.LocalDate restrictedUntil =
                CartExpiryUtil.getRestrictionExpiry(loggedInStudent.getStudentId(), selectedBook.getIsbn());
        if (restrictedUntil != null) {
            showAlert("Restricted",
                    "You did not collect this book on time when it was reserved for you.\n"
                            + "You cannot add this book to cart until: " + restrictedUntil,
                    Alert.AlertType.WARNING);
            return;
        }

        // p[0]=Serial, p[1]=StudentID, p[3]=BookISBN, p[6]=ExpiryDate, p[7]=Status
        String myCartExpiry    = null;
        boolean waitingByOther = false; // someone else has it as "Waiting" (book not available yet)
        boolean readyByOther   = false; // someone else has it as "Ready" (book available, 2-day window)
        String  otherExpiry    = "";

        for (String line : FileUtil.readFile(CART_FILE)) {
            String[] p = line.split(",", -1);
            if (p.length < 8) continue;
            if (!p[3].equalsIgnoreCase(selectedBook.getIsbn())) continue;

            if (p[1].equalsIgnoreCase(loggedInStudent.getStudentId())) {
                myCartExpiry = p[6].trim();
            } else {
                String cartStatus = p[7].trim();
                if (cartStatus.equalsIgnoreCase("Waiting")) {
                    waitingByOther = true;
                } else if (cartStatus.equalsIgnoreCase("Ready")) {
                    readyByOther = true;
                    otherExpiry  = p[6].trim();
                }
            }
        }

        // This student already has it in cart
        if (myCartExpiry != null) {
            showAlert("Already Added",
                    "You already have this book in your cart.",
                    Alert.AlertType.INFORMATION);
            return;
        }

        // Book is unavailable AND another student is already waiting for it → block
        if (selectedBook.getRemaining() <= 0 && waitingByOther) {
            showAlert("Not Available",
                    "This book is currently unavailable and is already reserved by another student. " +
                            "Please check back later.",
                    Alert.AlertType.WARNING);
            return;
        }

        // Book just became available but another student has a "Ready" 2-day window → block until it expires
        if (selectedBook.getRemaining() <= 0 && readyByOther) {
            showAlert("Not Available",
                    "This book is reserved by another student. " +
                            "Their reservation expires on: " + otherExpiry,
                    Alert.AlertType.WARNING);
            return;
        }

        // Duplicate check in issued: p[1]=BookID, p[2]=StudentID, p[4]=IssuedDate
        boolean alreadyIssued = FileUtil.readFile(ISSUED_FILE).stream()
                .anyMatch(line -> {
                    String[] p = line.split(",", -1);
                    return p.length > 4
                            && p[1].equalsIgnoreCase(selectedBook.getIsbn())
                            && p[2].equalsIgnoreCase(loggedInStudent.getStudentId())
                            && !p[4].equalsIgnoreCase("N/A");
                });

        if (alreadyIssued) {
            showAlert("Already Issued", "You already have this book issued.",
                    Alert.AlertType.INFORMATION);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm");
        confirm.setHeaderText(null);
        confirm.setContentText("Add \"" + selectedBook.getTitle() + "\" to cart?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            addToCartFile();
            clearBookFields();
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  FILE LOGIC
    //  CSV: Serial,StudentID,StudentName,BookISBN,BookName,
    //       RequestDate,ExpiryDate,Status
    // ─────────────────────────────────────────────────────────────
    private void addToCartFile() {
        List<String> cartList = new ArrayList<>(FileUtil.readFile(CART_FILE));

        // Next serial ID
        int lastId = 0;
        for (String line : cartList) {
            String[] p = line.split(",", -1);
            if (p.length > 0) {
                try {
                    int id = Integer.parseInt(p[0].trim());
                    if (id > lastId) lastId = id;
                } catch (Exception ignored) {}
            }
        }
        int newId = lastId + 1;

        LocalDate today  = LocalDate.now();
        String    status = (selectedBook.getRemaining() > 0) ? "Ready" : "Waiting";
        // Expiry is only meaningful when book is Available (Ready)
        // For Waiting entries, expiry will be set when book becomes available
        String expiryStr = status.equals("Ready") ? today.plusDays(2).toString() : "N/A";

        cartList.add(String.join(",",
                String.valueOf(newId),
                loggedInStudent.getStudentId(),
                loggedInStudent.getName(),
                selectedBook.getIsbn(),
                selectedBook.getTitle(),
                today.toString(),
                expiryStr,
                status));

        FileUtil.writeFile(CART_FILE, cartList,
                "Serial,StudentID,StudentName,BookISBN,BookName,RequestDate,ExpiryDate,Status");

        // Pending row in issueBooks.csv
        List<String> issuedList = new ArrayList<>(FileUtil.readFile(ISSUED_FILE));
        issuedList.add(String.join(",",
                "CART-" + newId,
                selectedBook.getIsbn(),
                loggedInStudent.getStudentId(),
                loggedInStudent.getName(),
                "N/A", "N/A", "0"));

        FileUtil.writeFile(ISSUED_FILE, issuedList,
                "IssuedID,BookID,StudentID,StudentName,IssuedDate,ReturnDate,LateFee");

        String alertMsg;
        if (status.equals("Ready")) {
            alertMsg = "Book added to cart!\n"
                    + "Serial No: CART-" + newId + "\n"
                    + "Must be collected by: " + expiryStr;
        } else {
            alertMsg = "Book added to cart!\n"
                    + "Serial No: CART-" + newId + "\n"
                    + "Status: Waiting — expiry will be set when the book becomes available.";
        }

        showAlert("Success", alertMsg, Alert.AlertType.INFORMATION);
    }

    // ─────────────────────────────────────────────────────────────
    //  CLEAR
    // ─────────────────────────────────────────────────────────────
    // Clears only the book info display — does NOT touch the search field
    private void clearBookInfo() {
        selectedBook = null;
        bookName.setText("-");
        bookAuthor.setText("-");
        bookPublisher.setText("-");
        availability.setText("-");
    }

    // Full reset including the search field — used on Cancel and after successful add
    private void clearBookFields() {
        bookSearchField.clear();
        clearBookInfo();
    }

    @FXML private void cancel(ActionEvent event) { clearBookFields(); }

    // ─────────────────────────────────────────────────────────────
    //  NAVIGATION
    // ─────────────────────────────────────────────────────────────
    @FXML private void loadHomePanel(ActionEvent e)          { new LoadStage("/mehrin/loginpage/StudentDashboard.fxml",      (Node)e.getSource(), true); }
    @FXML private void loadBooksPanel(ActionEvent e)         { new LoadStage("/mehrin/loginpage/StudentBooks.fxml",          (Node)e.getSource(), true); }
    @FXML private void loadAllIssuedBooks(ActionEvent e)     { new LoadStage("/mehrin/loginpage/StudentAllIssuedBooks.fxml", (Node)e.getSource(), true); }
    @FXML private void loadAnnouncementPanel(ActionEvent e)  { new LoadStage("/mehrin/loginpage/StudentAnnouncement.fxml",   (Node)e.getSource(), true); }
    @FXML private void loadAddToCartBooksPanel(ActionEvent e){ new LoadStage("/mehrin/loginpage/StudentAddToCart.fxml",      (Node)e.getSource(), true); }
    @FXML private void loadClearancePanel(ActionEvent e)     { new LoadStage("/mehrin/loginpage/StudentClearance.fxml",      (Node)e.getSource(), true); }
    @FXML private void logout(ActionEvent e)                 {
        SessionManager.getInstance().clear();
        new LoadStage("/mehrin/loginpage/Login.fxml", (Node)e.getSource(), true);
    }

    // ─────────────────────────────────────────────────────────────
    //  ALERT
    // ─────────────────────────────────────────────────────────────
    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setHeaderText(null);
        alert.setContentText(msg); alert.showAndWait();
    }
}